import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Experimento con ratones que compiten por alimento usando prioridades.
 *
 * Paso a paso:
 * 1. Dos dispensadores depositan comida en un comedero compartido.
 * 2. Cuatro ratones intentan acceder al comedero según su prioridad.
 * 3. Se usan condiciones para controlar acceso exclusivo y evitar bloqueos.
 */
public class MiceExperiment {
    private static final int FEEDER_CAPACITY = 3;
    private static final int DISPENSER_MIN_MS = 100;
    private static final int DISPENSER_MAX_MS = 300;

    private final List<String> feeder = new LinkedList<>();
    private final ReentrantLock feederLock = new ReentrantLock();
    private final Condition notFull = feederLock.newCondition();
    private final Condition feederFree = feederLock.newCondition();

    // Priority conditions
    private final Condition mickeyCond = feederLock.newCondition();
    private final Condition jerryCond = feederLock.newCondition();
    private final Condition othersCond = feederLock.newCondition();

    private int waitingMickey = 0;
    private int waitingJerry = 0;
    private int waitingOthers = 0;
    private boolean feederOccupied = false;

    private final AtomicInteger activeMice = new AtomicInteger(4);

    private final Random rng = new Random();

    // Counters
    private final AtomicInteger gruyereDropped = new AtomicInteger(0);
    private final AtomicInteger frescoDropped = new AtomicInteger(0);

    private final AtomicInteger mickeyGruyere = new AtomicInteger(0);
    private final AtomicInteger mickeyFresco = new AtomicInteger(0);
    private final AtomicInteger jerryGruyere = new AtomicInteger(0);
    private final AtomicInteger jerryFresco = new AtomicInteger(0);
    private final AtomicInteger pixieGruyere = new AtomicInteger(0);
    private final AtomicInteger pixieFresco = new AtomicInteger(0);
    private final AtomicInteger dixieGruyere = new AtomicInteger(0);
    private final AtomicInteger dixieFresco = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        MiceExperiment exp = new MiceExperiment();
        exp.start();
    }

    public void start() throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();

        // Iniciar dos dispensadores y los cuatro ratones.
        exec.submit(() -> dispenserLoop("Dispenser-1"));
        exec.submit(() -> dispenserLoop("Dispenser-2"));

        // Mice
        exec.submit(() -> mouseLoop("Mickey"));
        exec.submit(() -> mouseLoop("Jerry"));
        exec.submit(() -> mouseLoop("Pixie"));
        exec.submit(() -> mouseLoop("Dixie"));

        exec.shutdown();
        exec.awaitTermination(10, TimeUnit.MINUTES);

        // Summary
        System.out.println("\n--- Mice Experiment Summary ---");
        System.out.println("Dropped Gruyere: " + gruyereDropped.get() + ", Dropped Fresco: " + frescoDropped.get());
        System.out.println("Feeder remaining: " + feeder);
        System.out.println("Mickey ate Gruyere=" + mickeyGruyere.get() + ", Fresco=" + mickeyFresco.get());
        System.out.println("Jerry ate Gruyere=" + jerryGruyere.get() + ", Fresco=" + jerryFresco.get());
        System.out.println("Pixie ate Gruyere=" + pixieGruyere.get() + ", Fresco=" + pixieFresco.get());
        System.out.println("Dixie ate Gruyere=" + dixieGruyere.get() + ", Fresco=" + dixieFresco.get());
        System.out.println("All mice finished. Main ends.");
    }

    private void dispenserLoop(String name) {
        while (activeMice.get() > 0) {
            // El dispensador espera si el comedero está lleno.
            feederLock.lock();
            try {
                while (feeder.size() >= FEEDER_CAPACITY) {
                    try { notFull.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

                // produce random type
                boolean isGruyere = rng.nextBoolean();
                if (isGruyere) {
                    feeder.add("Gruyere");
                    gruyereDropped.incrementAndGet();
                } else {
                    feeder.add("Fresco");
                    frescoDropped.incrementAndGet();
                }
                System.out.println(name + " dropped " + feeder.get(feeder.size()-1) + ". Feeder now: " + feeder);

                // signal waiting mice that feeder has items
                // wake highest priority first
                mickeyCond.signal();
                jerryCond.signal();
                othersCond.signalAll();
            } finally {
                feederLock.unlock();
            }

            // random delay 100..300 ms
            try { Thread.sleep(DISPENSER_MIN_MS + rng.nextInt(DISPENSER_MAX_MS - DISPENSER_MIN_MS + 1)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }

        System.out.println(name + " stopping as all mice have finished.");
    }

    private void mouseLoop(String mouseName) {
        int weight = 100;
        int gruyereEaten = 0;
        int frescoEaten = 0;

        while (true) {
            // Cada ratón come hasta alcanzar el peso objetivo.
            if (mouseName.equals("Mickey") && weight >= 105) break;
            if (!mouseName.equals("Mickey") && weight >= 110) break;

            // Intentar entrar al comedero de forma segura.
            feederLock.lock();
            try {
                // increment waiting counters
                if (mouseName.equals("Mickey")) waitingMickey++;
                else if (mouseName.equals("Jerry")) waitingJerry++;
                else waitingOthers++;

                try {
                    while (feederOccupied || !canEnterAccordingPriority(mouseName)) {
                        // wait on respective condition
                        try {
                            if (mouseName.equals("Mickey")) mickeyCond.await();
                            else if (mouseName.equals("Jerry")) jerryCond.await();
                            else othersCond.await();
                        } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                    }
                } finally {
                    // when woke up, decrement waiting count
                    if (mouseName.equals("Mickey")) waitingMickey--;
                    else if (mouseName.equals("Jerry")) waitingJerry--;
                    else waitingOthers--;
                }

                // occupy feeder
                feederOccupied = true;

                // Now inside feeder: check for food
                String ate = null;
                if (mouseName.equals("Mickey")) {
                    // only Gruyere
                    int idx = feeder.indexOf("Gruyere");
                    if (idx != -1) ate = feeder.remove(idx);
                } else if (mouseName.equals("Jerry")) {
                    int idx = feeder.indexOf("Fresco");
                    if (idx != -1) ate = feeder.remove(idx);
                } else {
                    // Pixie and Dixie: eat any (prefer Gruyere? spec says both types, no preference)
                    if (!feeder.isEmpty()) ate = feeder.remove(0);
                }

                // free feeder occupancy for next mouse
                feederOccupied = false;
                feederFree.signalAll();

                // signal dispensers there's space
                notFull.signalAll();
                // signal waiting mice to allow next (following priority rules)
                if (waitingMickey > 0) mickeyCond.signal();
                else if (waitingJerry > 0) jerryCond.signal();
                else othersCond.signal();

                // if ate is null, leave immediately
                if (ate == null) {
                    // leave and sleep 100..500 ms before next attempt
                    try { feederLock.unlock(); } finally { /* ensure unlocked below */ }
                    int sleep = 100 + rng.nextInt(401);
                    try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                } else {
                    // ate something
                    int eatTime = rng.nextInt(101); // 0..100 ms eating
                    feederLock.unlock();
                    try { Thread.sleep(eatTime); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

                    if ("Gruyere".equals(ate)) {
                        gruyereEaten++;
                        weight += 1;
                        if (mouseName.equals("Mickey")) mickeyGruyere.incrementAndGet();
                        else if (mouseName.equals("Jerry")) jerryGruyere.incrementAndGet();
                        else if (mouseName.equals("Pixie")) pixieGruyere.incrementAndGet();
                        else dixieGruyere.incrementAndGet();
                    } else {
                        frescoEaten++;
                        weight += 2;
                        if (mouseName.equals("Mickey")) mickeyFresco.incrementAndGet();
                        else if (mouseName.equals("Jerry")) jerryFresco.incrementAndGet();
                        else if (mouseName.equals("Pixie")) pixieFresco.incrementAndGet();
                        else dixieFresco.incrementAndGet();
                    }

                    System.out.println(mouseName + " ate " + ate + ". Weight=" + weight);

                    // after eating, wait 100..500 ms before next
                    int sleep = 100 + rng.nextInt(401);
                    try { Thread.sleep(sleep); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

            } finally {
                // ensure feederLock unlocked if still held
                if (feederLock.isHeldByCurrentThread()) feederLock.unlock();
            }

            // check stopping condition loop will break
        }

        System.out.println(mouseName + " stops eating. Final weight recorded.");
        activeMice.decrementAndGet();
    }

    private boolean canEnterAccordingPriority(String mouseName) {
        // allow entry if feeder not occupied and there is no higher priority waiting
        if (feederOccupied) return false;
        if (mouseName.equals("Mickey")) return true; // highest priority
        if (mouseName.equals("Jerry")) {
            return waitingMickey == 0; // Jerry waits if Mickey waiting
        }
        // Pixie/Dixie wait if Mickey or Jerry waiting
        return waitingMickey == 0 && waitingJerry == 0;
    }
}
