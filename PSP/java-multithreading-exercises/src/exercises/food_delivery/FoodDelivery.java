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
 * Simulación de cocina y reparto de pedidos con múltiples cocineros y repartidores.
 *
 * Paso a paso:
 * 1. Dos cocineros preparan hamburguesas y pizzas en un almacenamiento compartido.
 * 2. Cuatro repartidores toman pedidos del almacenamiento y los entregan.
 * 3. Se usan condiciones para esperar cuando el almacenamiento está lleno o vacío.
 */
public class FoodDelivery {
    private static final int STORAGE_CAPACITY = 5;
    private static final int COOKS_GOAL = 15; // each cook makes 15 items
    private static final int DELIVERERS = 4;

    private final List<String> storage = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private volatile boolean burgersDone = false;
    private volatile boolean pizzasDone = false;

    private final AtomicInteger[] burgersDeliveredBy = new AtomicInteger[DELIVERERS];
    private final AtomicInteger[] pizzasDeliveredBy = new AtomicInteger[DELIVERERS];

    private final Random rng = new Random();

    public FoodDelivery() {
        for (int i = 0; i < DELIVERERS; i++) {
            burgersDeliveredBy[i] = new AtomicInteger(0);
            pizzasDeliveredBy[i] = new AtomicInteger(0);
        }
    }

    public void startSimulation() throws InterruptedException {
        // Crear un pool de hilos con dos cocineros y cuatro repartidores.
        ExecutorService exec = Executors.newFixedThreadPool(6); // 2 cooks + 4 deliverers

        exec.submit(this::burgerCook);
        exec.submit(this::pizzaCook);

        for (int i = 0; i < DELIVERERS; i++) {
            final int id = i;
            exec.submit(() -> deliverer(id));
        }

        exec.shutdown();
        exec.awaitTermination(10, TimeUnit.MINUTES);

        System.out.println("\n--- Food Delivery Summary ---");
        for (int i = 0; i < DELIVERERS; i++) {
            System.out.println("Deliverer " + i + ": burgers=" + burgersDeliveredBy[i].get() + ", pizzas=" + pizzasDeliveredBy[i].get());
        }
    }

    private void burgerCook() {
        for (int i = 0; i < COOKS_GOAL; i++) {
            // Simular la preparación de una hamburguesa.
            try { Thread.sleep(rng.nextInt(301)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            lock.lock();
            try {
                // Esperar si el almacenamiento está lleno.
                while (storage.size() >= STORAGE_CAPACITY) {
                    try { notFull.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                storage.add("burger");
                System.out.println("Burger cooked. Storage size=" + storage.size());
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        burgersDone = true;
        lock.lock();
        try { notEmpty.signalAll(); } finally { lock.unlock(); }
        System.out.println("Burger cook finished.");
    }

    private void pizzaCook() {
        for (int i = 0; i < COOKS_GOAL; i++) {
            // Simular la preparación de una pizza.
            try { Thread.sleep(rng.nextInt(201)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            lock.lock();
            try {
                while (storage.size() >= STORAGE_CAPACITY) {
                    try { notFull.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                storage.add("pizza");
                System.out.println("Pizza cooked. Storage size=" + storage.size());
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        pizzasDone = true;
        lock.lock();
        try { notEmpty.signalAll(); } finally { lock.unlock(); }
        System.out.println("Pizza cook finished.");
    }

    private void deliverer(int id) {
        while (true) {
            String item = null;
            lock.lock();
            try {
                while (storage.isEmpty()) {
                    if (burgersDone && pizzasDone) return; // nothing left to deliver
                    try { notEmpty.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

                // El repartidor prefiere llevar hamburguesas antes que pizzas.
                int burgerIndex = -1;
                for (int i = 0; i < storage.size(); i++) {
                    if ("burger".equals(storage.get(i))) { burgerIndex = i; break; }
                }
                if (burgerIndex != -1) {
                    item = storage.remove(burgerIndex);
                } else {
                    item = storage.remove(0);
                }
                notFull.signal();
            } finally {
                lock.unlock();
            }

            if (item != null) {
                // deliver time 300..600 ms
                try { Thread.sleep(300 + rng.nextInt(301)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

                if ("burger".equals(item)) burgersDeliveredBy[id].incrementAndGet();
                else pizzasDeliveredBy[id].incrementAndGet();
                System.out.println("Deliverer " + id + " delivered " + item + ".");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FoodDelivery sim = new FoodDelivery();
        sim.startSimulation();
    }
}
