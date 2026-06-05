/**
 * Implementación multi-clase de la cadena de montaje.
 *
 * Clases involucradas:
 * - `Belt`: recurso compartido (ya implementada en Belt.java)
 * - `Placer`: hilo productor
 * - `Packer`: hilos consumidores por tipo
 * - `AssemblyLineMulti`: clase principal que coordina y muestra estadísticas
 *
 * Paso a paso:
 * 1. Crear `Belt` compartida con capacidad.
 * 2. Crear contadores atómicos y lanzar `Placer` + 3 `Packer` con ExecutorService.
 * 3. Esperar finalización y mostrar resumen.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AssemblyLineMulti {
    private static final int BELT_CAPACITY = 3;
    private static final int PLACER_ITERATIONS = 20;

    public static void main(String[] args) throws InterruptedException {
        Belt belt = new Belt(BELT_CAPACITY);

        AtomicInteger totalPlaced = new AtomicInteger(0);
        AtomicInteger totalPacked = new AtomicInteger(0);
        AtomicInteger[] packedByPacker = new AtomicInteger[3];
        for (int i = 0; i < 3; i++) packedByPacker[i] = new AtomicInteger(0);

        ExecutorService exec = Executors.newFixedThreadPool(4); // 1 placer + 3 packers

        exec.submit(new Placer(belt, PLACER_ITERATIONS, totalPlaced));
        exec.submit(new Packer(belt, 1, totalPacked, packedByPacker[0]));
        exec.submit(new Packer(belt, 2, totalPacked, packedByPacker[1]));
        exec.submit(new Packer(belt, 3, totalPacked, packedByPacker[2]));

        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.MINUTES);

        // Resumen
        System.out.println("\n--- Assembly Line Multi Summary ---");
        System.out.println("Total placed: " + totalPlaced.get());
        System.out.println("Total packed: " + totalPacked.get());
        for (int i = 0; i < 3; i++) {
            System.out.println("Packer type " + (i+1) + " packed: " + packedByPacker[i].get());
        }
    }
}
