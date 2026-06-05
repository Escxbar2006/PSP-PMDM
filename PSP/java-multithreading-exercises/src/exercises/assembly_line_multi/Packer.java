/**
 * Clase `Packer` que representa a un empacador dedicado a un tipo específico.
 *
 * Paso a paso:
 * - En `run()` solicita al `Belt` un producto de su tipo mediante `take(myType)`.
 * - Si `take` devuelve -1 significa que no hay más productos y el colocador terminó.
 * - Simula tiempos de recogida y embalaje y actualiza contadores atómicos.
 */

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Packer implements Runnable {
    private final Belt belt;
    private final int myType;
    private final AtomicInteger totalPacked;
    private final AtomicInteger packedByPacker;
    private final Random rng = new Random();

    public Packer(Belt belt, int myType, AtomicInteger totalPacked, AtomicInteger packedByPacker) {
        this.belt = belt;
        this.myType = myType;
        this.totalPacked = totalPacked;
        this.packedByPacker = packedByPacker;
    }

    @Override
    public void run() {
        while (true) {
            int item;
            try {
                item = belt.take(myType);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (item == -1) return; // no hay más productos y colocador terminó

            // Simular tiempo de recogida (100 ms) y embalaje (0..500 ms).
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            try { Thread.sleep(rng.nextInt(501)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }

            int total = totalPacked.incrementAndGet();
            packedByPacker.incrementAndGet();
            System.out.println("Packer " + myType + " processed a product. Total packed=" + total);
        }
    }
}
