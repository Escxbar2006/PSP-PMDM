import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulación de la operación Dynamo con grupos de botes y lanchas.
 *
 * Paso a paso:
 * 1. Cada grupo solicita acceso a la playa para embarcar soldados.
 * 2. Luego cruzan al mar y desembarcan en un área segura.
 * 3. Se coordina la playa y el mar para que no haya conflictos de tipo.
 */
public class OperationDynamo {
    // Time mapping
    private static final int HOUR_MS = 1000; // 1 hour = 1000 ms
    private static final int HOURS_PER_DAY = 24;
    private static final int TOTAL_DAYS = 6;
    private static final int TOTAL_HOURS = HOURS_PER_DAY * TOTAL_DAYS; // 144 hours

    // Timing
    private static final int BOARDING_MS = 400; // 24 minutes -> 400 ms (improved plan)
    private static final int DISEMBARK_MS = 500; // 30 minutes -> 500 ms

    // Groups
    private static final int BOAT_GROUPS = 5; // barcas
    private static final int LAUNCH_GROUPS = 5; // lanchas

    // Capacities per group (student hypothesis)
    private static final int BOAT_CAP_MIN = 300;
    private static final int BOAT_CAP_MAX = 400;
    private static final int LAUNCH_CAP_MIN = 100;
    private static final int LAUNCH_CAP_MAX = 200;

    // Beach constraints
    private static final int MAX_BEACH_GROUPS = 7; // max groups allowed on beach simultaneously

    // Initial soldiers on the beach
    private final AtomicInteger soldiersOnBeach = new AtomicInteger(400_000);

    // Tracking
    private final AtomicInteger[] rescuedBoats = new AtomicInteger[BOAT_GROUPS];
    private final AtomicInteger[] rescuedLaunches = new AtomicInteger[LAUNCH_GROUPS];
    private final AtomicInteger[] inTransitBoats = new AtomicInteger[BOAT_GROUPS];
    private final AtomicInteger[] inTransitLaunches = new AtomicInteger[LAUNCH_GROUPS];

    // Beach coordination
    private final Object beachLock = new Object();
    // 0 = none, 1 = boats, 2 = launches
    private int beachType = 0;
    private int boatsWaiting = 0;
    private int launchesWaiting = 0;
    private final Semaphore beachSlots = new Semaphore(MAX_BEACH_GROUPS);

    // Sea coordination (disembark): only one type at a time, but many groups of same type can disembark concurrently
    private final Object seaLock = new Object();
    private int seaType = 0;
    private int seaActiveCount = 0;

    // Clock
    private volatile boolean finished = false;

    private final Random rng = new Random();

    public static void main(String[] args) {
        OperationDynamo sim = new OperationDynamo();
        sim.runSimulation();
    }

    public OperationDynamo() {
        for (int i = 0; i < BOAT_GROUPS; i++) {
            rescuedBoats[i] = new AtomicInteger(0);
            inTransitBoats[i] = new AtomicInteger(0);
        }
        for (int i = 0; i < LAUNCH_GROUPS; i++) {
            rescuedLaunches[i] = new AtomicInteger(0);
            inTransitLaunches[i] = new AtomicInteger(0);
        }
    }

    private void runSimulation() {
        // Iniciar el reloj que controla la duración total de la operación.
        Thread clock = new Thread(this::clockRun, "ClockThread");
        clock.setPriority(Thread.MAX_PRIORITY);
        clock.start();

        // Start boat groups
        Thread[] boatThreads = new Thread[BOAT_GROUPS];
        for (int i = 0; i < BOAT_GROUPS; i++) {
            final int id = i;
            boatThreads[i] = new Thread(() -> groupLoop(true, id), "BoatGroup-" + id);
            boatThreads[i].start();
        }

        // Start launch groups
        Thread[] launchThreads = new Thread[LAUNCH_GROUPS];
        for (int i = 0; i < LAUNCH_GROUPS; i++) {
            final int id = i;
            launchThreads[i] = new Thread(() -> groupLoop(false, id), "LaunchGroup-" + id);
            launchThreads[i].start();
        }

        // Wait for clock to finish
        try {
            clock.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // After clock finished, signal groups to stop boarding (they observe finished flag)

        // Wait briefly for groups to settle (allow in-transit groups to either disembark or be counted)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Summarize
        int totalRescued = 0;
        System.out.println("\n--- Operation summary ---");
        System.out.println("Soldiers remaining on beach (surrendered): " + soldiersOnBeach.get());

        System.out.println("Boats (barcas) per-group rescued:");
        for (int i = 0; i < BOAT_GROUPS; i++) {
            int rescued = rescuedBoats[i].get() + inTransitBoats[i].get();
            System.out.println("  BoatGroup-" + i + ": " + rescued);
            totalRescued += rescued;
        }

        System.out.println("Launches (lanchas) per-group rescued:");
        for (int i = 0; i < LAUNCH_GROUPS; i++) {
            int rescued = rescuedLaunches[i].get() + inTransitLaunches[i].get();
            System.out.println("  LaunchGroup-" + i + ": " + rescued);
            totalRescued += rescued;
        }

        System.out.println("Total rescued: " + totalRescued);
        System.out.println("Operation time limit reached or no more soldiers to rescue.");
    }

    private void clockRun() {
        int hours = 0;
        while (hours < TOTAL_HOURS) {
            try {
                Thread.sleep(HOUR_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            hours++;
            // Provide periodic status updates every 6 hours to avoid too much output
            if (hours % 6 == 0 || hours == 1) {
                int rescuedSoFar = computeTotalRescued();
                System.out.println("[Clock] Hour " + hours + " / " + TOTAL_HOURS + ", soldiers left on beach: " + soldiersOnBeach.get() + ", rescued so far: " + rescuedSoFar);
            }

            // Early stop if no soldiers on beach and none in transit
            if (soldiersOnBeach.get() <= 0 && totalInTransit() == 0) {
                System.out.println("[Clock] All soldiers evacuated before time limit (hour " + hours + ").");
                finished = true;
                return;
            }
        }
        finished = true;
        System.out.println("[Clock] Time limit reached (" + TOTAL_DAYS + " days). Ending boarding operations.");
    }

    private int computeTotalRescued() {
        int total = 0;
        for (int i = 0; i < BOAT_GROUPS; i++) total += rescuedBoats[i].get();
        for (int i = 0; i < LAUNCH_GROUPS; i++) total += rescuedLaunches[i].get();
        return total;
    }

    private int totalInTransit() {
        int tot = 0;
        for (int i = 0; i < BOAT_GROUPS; i++) tot += inTransitBoats[i].get();
        for (int i = 0; i < LAUNCH_GROUPS; i++) tot += inTransitLaunches[i].get();
        return tot;
    }

    private void groupLoop(boolean isBoat, int id) {
        while (true) {
            if (finished) break; // stop attempting new boardings

            // Request beach access
            boolean allowed = requestBeach(isBoat);
            if (!allowed) break; // simulation ended while waiting

            // Simulate boarding: determine capacity and take from beach atomically
            int capacity = isBoat ? randBetween(BOAT_CAP_MIN, BOAT_CAP_MAX) : randBetween(LAUNCH_CAP_MIN, LAUNCH_CAP_MAX);
            int taken = takeFromBeach(capacity);
            if (taken <= 0) {
                // No more soldiers to board
                releaseBeachSlot();
                break;
            }

            // Mark in-transit
            if (isBoat) inTransitBoats[id].addAndGet(taken);
            else inTransitLaunches[id].addAndGet(taken);

            // Boarding time
            try {
                Thread.sleep(BOARDING_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Leave beach
            releaseBeachSlot();

            // Request sea disembark (ensure only same type disembark concurrently)
            requestSea(isBoat);

            // Disembark time
            try {
                Thread.sleep(DISEMBARK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Finish disembark: move from inTransit to rescued
            if (isBoat) {
                int moved = inTransitBoats[id].getAndSet(0);
                rescuedBoats[id].addAndGet(moved);
            } else {
                int moved = inTransitLaunches[id].getAndSet(0);
                rescuedLaunches[id].addAndGet(moved);
            }

            releaseSea();

            // Short pause before next trip
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean requestBeach(boolean isBoat) {
        synchronized (beachLock) {
            if (isBoat) boatsWaiting++; else launchesWaiting++;
            try {
                while (true) {
                    if (finished) return false;
                    // If beach currently used by other type, wait
                    if (beachType != 0 && ((isBoat && beachType != 1) || (!isBoat && beachType != 2))) {
                        try { beachLock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
                        continue;
                    }

                    // If beach free, enforce priority: launches must wait if boats waiting
                    if (beachType == 0) {
                        if (!isBoat && boatsWaiting > 0) {
                            try { beachLock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return false; }
                            continue;
                        }
                        beachType = isBoat ? 1 : 2;
                    }

                    break;
                }
            } finally {
                if (isBoat) boatsWaiting--; else launchesWaiting--;
            }
        }

        // Acquire a slot on the beach (max MAX_BEACH_GROUPS concurrently)
        try {
            beachSlots.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    private void releaseBeachSlot() {
        beachSlots.release();
        synchronized (beachLock) {
            if (beachSlots.availablePermits() == MAX_BEACH_GROUPS) {
                beachType = 0;
                beachLock.notifyAll();
            }
        }
    }

    private void requestSea(boolean isBoat) {
        synchronized (seaLock) {
            while (true) {
                if (finished) break; // allow disembark even after finished
                if (seaType == 0) {
                    seaType = isBoat ? 1 : 2;
                    seaActiveCount++;
                    break;
                } else if ((isBoat && seaType == 1) || (!isBoat && seaType == 2)) {
                    seaActiveCount++;
                    break;
                } else {
                    try { seaLock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                }
            }
        }
    }

    private void releaseSea() {
        synchronized (seaLock) {
            seaActiveCount--;
            if (seaActiveCount <= 0) {
                seaType = 0;
                seaLock.notifyAll();
            }
        }
    }

    private int takeFromBeach(int wanted) {
        synchronized (soldiersOnBeach) {
            int available = soldiersOnBeach.get();
            int taken = Math.min(wanted, available);
            if (taken > 0) soldiersOnBeach.addAndGet(-taken);
            return taken;
        }
    }

    private int randBetween(int a, int b) {
        return a + rng.nextInt(b - a + 1);
    }
}
