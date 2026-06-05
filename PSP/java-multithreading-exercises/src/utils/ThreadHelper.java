/**
 * Utilidades simples para manejar hilos.
 *
 * Paso a paso:
 * 1. sleepQuietly pausa el hilo actual sin propagar InterruptedException.
 * 2. interruptThread interrumpe un hilo de forma segura si está vivo.
 */
public class ThreadHelper {
    public static void sleepQuietly(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void interruptThread(Thread thread) {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}