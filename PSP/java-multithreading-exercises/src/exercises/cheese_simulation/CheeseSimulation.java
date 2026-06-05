import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simulación completa del problema ratones-queso.
 *
 * Paso a paso:
 * 1. Crear una bandeja de capacidad limitada.
 * 2. Lanzar un productor y N ratones con ExecutorService.
 * 3. Esperar finalización y mostrar resumen mínimo.
 */
public class CheeseSimulation {
    public static void main(String[] args) throws InterruptedException {
        CheeseTray tray = new CheeseTray(3);

        ExecutorService exec = Executors.newCachedThreadPool();

        exec.submit(new Producer(tray, 20));
        exec.submit(new MouseCheese(tray, "Mickey"));
        exec.submit(new MouseCheese(tray, "Jerry"));
        exec.submit(new MouseCheese(tray, "Pixie"));
        exec.submit(new MouseCheese(tray, "Dixie"));

        exec.shutdown();
        exec.awaitTermination(5, TimeUnit.MINUTES);

        System.out.println("Cheese simulation finished.");
    }
}
