/**
 * Clase `Placer` que representa al hilo productor (colocador) de la línea.
 *
 * Paso a paso:
 * - En `run()` itera N veces generando un tipo de producto aleatorio (1..3).
 * - Llama a `belt.put(type)` para colocarlo en la cinta (espera si está llena).
 * - Actualiza el contador atómico de colocados y simula un retardo entre colocaciones.
 */

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Placer implements Runnable {
    private final Belt belt;
    private final int iterations;
    private final AtomicInteger totalPlaced;
    private final Random rng = new Random();

    public Placer(Belt belt, int iterations, AtomicInteger totalPlaced) {
        this.belt = belt;
        this.iterations = iterations;
        this.totalPlaced = totalPlaced;
    }

    @Override
    public void run() {
        for (int i = 0; i < iterations; i++) {
            int prodType = rng.nextInt(3) + 1; // 1..3
            try {
                belt.put(prodType);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            int placed = totalPlaced.incrementAndGet();
            System.out.println("Placer placed product type " + prodType + " (placed count=" + placed + ")");

            // Retardo aleatorio 0..250 ms para simular trabajo de producción.
            try { Thread.sleep(rng.nextInt(251)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }

        // Señalar que ya no habrá más productos.
        belt.setDonePlacing();
        System.out.println("Placer finished after " + iterations + " placements.");
    }
}
