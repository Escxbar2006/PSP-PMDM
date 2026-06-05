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
 * Simulación de una línea de ensamblaje con un colocador y tres empacadores.
 *
 * Paso a paso:
 * 1. El colocador genera productos aleatorios y los pone en la cinta si hay espacio.
 * 2. Cada empacador procesa solo el tipo de producto que le corresponde.
 * 3. Se usan condiciones para coordinar cuando la cinta está llena o cuando un tipo
 *    específico está disponible.
 */
public class AssemblyLine {
    private static final int BELT_CAPACITY = 3;
    private static final int PLACER_ITERATIONS = 20;

    private final List<Integer> belt = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition[] typeAvailable = new Condition[3];

    private final AtomicInteger totalPlaced = new AtomicInteger(0);
    private final AtomicInteger totalPacked = new AtomicInteger(0);
    private final AtomicInteger[] packedByPacker = new AtomicInteger[3];

    private volatile boolean donePlacing = false;
    private final Random rng = new Random();

    public AssemblyLine() {
        for (int i = 0; i < 3; i++) {
            typeAvailable[i] = lock.newCondition();
            packedByPacker[i] = new AtomicInteger(0);
        }
    }

    public void startSimulation() throws InterruptedException {
        // Crear un grupo fijo de 4 hilos: un colocador y tres empacadores.
        ExecutorService exec = Executors.newFixedThreadPool(4); // 1 placer + 3 packers

        // Ejecuta el hilo productor y los tres hilos consumidores.
        exec.submit(this::placer);
        exec.submit(() -> packer(1));
        exec.submit(() -> packer(2));
        exec.submit(() -> packer(3));

        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.MINUTES);

        // Ensure nothing remains on the belt
        lock.lock();
        try {
            if (!belt.isEmpty()) {
                System.out.println("Warning: belt not empty after shutdown. Remaining: " + belt);
            }
        } finally {
            lock.unlock();
        }

        System.out.println("\n--- Assembly Line Summary ---");
        System.out.println("Total placed: " + totalPlaced.get());
        System.out.println("Total packed: " + totalPacked.get());
        for (int i = 0; i < 3; i++) {
            System.out.println("Packer type " + (i+1) + " packed: " + packedByPacker[i].get());
        }
    }

    private void placer() {
        // El hilo colocador repite un número fijo de veces.
        for (int i = 0; i < PLACER_ITERATIONS; i++) {
            int prodType = rng.nextInt(3) + 1; // 1..3

            lock.lock();
            try {
                // Espera si la cinta está llena antes de colocar el siguiente producto.
                while (belt.size() >= BELT_CAPACITY) {
                    try { notFull.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }
                // Añade el producto a la cinta y avisa a los empacadores.
                belt.add(prodType);
                totalPlaced.incrementAndGet();
                System.out.println("Placer placed product type " + prodType + " (placed count=" + totalPlaced.get() + ")");
                typeAvailable[prodType-1].signal();
            } finally {
                lock.unlock();
            }

            // random delay 0..250 ms
            try {
                Thread.sleep(rng.nextInt(251));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // done placing
        lock.lock();
        try {
            donePlacing = true;
            // wake all packers so they can finish remaining items
            for (Condition c : typeAvailable) c.signalAll();
        } finally {
            lock.unlock();
        }

        System.out.println("Placer finished after " + PLACER_ITERATIONS + " placements.");
    }

    private void packer(int myType) {
        // Cada empacador procesa solo su tipo de producto.
        while (true) {
            int item = -1;
            lock.lock();
            try {
                // Espera hasta que exista un producto de su tipo o hasta que se haya terminado de colocar.
                while (!containsType(myType)) {
                    if (donePlacing && belt.isEmpty()) return; // nothing left to do
                    try { typeAvailable[myType-1].await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                }

                // remove first occurrence of myType
                for (int i = 0; i < belt.size(); i++) {
                    if (belt.get(i) == myType) {
                        item = belt.remove(i);
                        break;
                    }
                }
                // notify placer there is space
                notFull.signal();
            } finally {
                lock.unlock();
            }

            if (item != -1) {
                // picking up takes 100 ms
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

                // packaging takes 0..500 ms
                try { Thread.sleep(rng.nextInt(501)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

                totalPacked.incrementAndGet();
                packedByPacker[myType-1].incrementAndGet();
                System.out.println("Packer " + myType + " processed a product. Total packed=" + totalPacked.get());

                // after processing, signal other packers (in case they were waiting for their types)
                lock.lock();
                try {
                    for (Condition c : typeAvailable) c.signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private boolean containsType(int t) {
        for (Integer x : belt) if (x == t) return true;
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        AssemblyLine sim = new AssemblyLine();
        sim.startSimulation();
    }
}
