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
 * Experimento con gallinas y un dispensador compartido.
 *
 * Paso a paso:
 * 1. Un dispensador coloca alimentos en el comedero hasta su capacidad.
 * 2. Tres gallinas toman alimento del comedero de forma concurrente.
 * 3. Se usan condiciones para esperar cuando el comedero está lleno o vacío.
 */
public class ChickensExperiment {
    private static final int FEEDER_CAPACITY = 3;
    private static final int DISP_MIN_MS = 0;
    private static final int DISP_MAX_MS = 100;

    private final List<String> feeder = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private final Random rng = new Random();

    private final AtomicInteger maizeDropped = new AtomicInteger(0);
    private final AtomicInteger wheatDropped = new AtomicInteger(0);

    private final AtomicInteger caponataEaten = new AtomicInteger(0);
    private final AtomicInteger turuletaEaten = new AtomicInteger(0);
    private final AtomicInteger kikoEaten = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        ChickensExperiment exp = new ChickensExperiment();
        exp.start();
    }

    public void start() throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();

        // Iniciar el dispensador y los tres hilos de las gallinas.
        exec.submit(this::dispenserLoop);

        exec.submit(() -> chickenLoop("Caponata", 5));
        exec.submit(() -> chickenLoop("Turuleta", 5));
        exec.submit(() -> chickenLoop("Kiko", 7));

        exec.shutdown();
        exec.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("\n--- Chickens Experiment Summary ---");
        System.out.println("Dropped maize=" + maizeDropped.get() + ", wheat=" + wheatDropped.get());
        System.out.println("Feeder remaining: " + feeder);
        System.out.println("Caponata ate " + caponataEaten.get());
        System.out.println("Turuleta ate " + turuletaEaten.get());
        System.out.println("Kiko ate " + kikoEaten.get());
    }

    private void dispenserLoop() {
        // El dispensador sigue agregando alimento cuando hay espacio.
        while (true) {
            lock.lock();
            try {
                // stop condition: if all done -> there will be no waiting threads; but we can't detect directamente here
                // We'll check if no active eaters by trying to see si executor still running, but simpler: we keep producing
                while (feeder.size() >= FEEDER_CAPACITY) {
                    try { notFull.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

                boolean isMaize = rng.nextBoolean();
                if (isMaize) { feeder.add("maize"); maizeDropped.incrementAndGet(); }
                else { feeder.add("wheat"); wheatDropped.incrementAndGet(); }
                System.out.println("Dispenser dropped " + feeder.get(feeder.size()-1) + ". Feeder: " + feeder);
                notEmpty.signalAll();
            } finally {
                lock.unlock();
            }

            // sleep 0..100 ms
            try { Thread.sleep(DISP_MIN_MS + rng.nextInt(DISP_MAX_MS - DISP_MIN_MS + 1)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    private void chickenLoop(String name, int goal) {
        int eaten = 0;
        while (eaten < goal) {
            String item = null;
            lock.lock();
            try {
                while (feeder.isEmpty()) {
                    try { notEmpty.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

                // Prefiere consumir maíz si está disponible, de lo contrario toma el primer alimento.
                int maizeIdx = -1;
                for (int i = 0; i < feeder.size(); i++) if ("maize".equals(feeder.get(i))) { maizeIdx = i; break; }
                if (maizeIdx != -1) item = feeder.remove(maizeIdx);
                else item = feeder.remove(0);

                // Avisar al dispensador que hay espacio libre en el comedero.
                notFull.signalAll();
            } finally {
                lock.unlock();
            }

            if (item != null) {
                // eating 0..200 ms
                try { Thread.sleep(rng.nextInt(201)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                eaten++;
                if (name.equals("Caponata")) caponataEaten.incrementAndGet();
                else if (name.equals("Turuleta")) turuletaEaten.incrementAndGet();
                else kikoEaten.incrementAndGet();
                System.out.println(name + " ate " + item + ". total eaten=" + eaten);

                // after eating wait 0..500 ms
                try { Thread.sleep(rng.nextInt(501)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }

        System.out.println(name + " finished after eating " + eaten + " doses.");
    }
}
