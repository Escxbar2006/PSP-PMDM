/**
 * Simulación de un atasco de tráfico en un punto de accidente.
 *
 * Paso a paso:
 * 1. Se simulan coches que llegan al sitio del accidente de forma aleatoria.
 * 2. Cada coche espera a cruzar el tramo afectado durante un tiempo fijo.
 * 3. Se cuenta el número de coches esperando y aliviando el atasco.
 */
public class TrafficJam {
    private static final int MAX_WAITING_CARS = 10;
    private static final int ACCIDENT_SITE_PASS_TIME = 3000; // Time to pass the accident site
    private static int waitingCars = 0;

    public static void main(String[] args) {
        Thread trafficJamThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep((int) (Math.random() * (400 - 200 + 1) + 200)); // Random delay between 200 to 400 ms
                    arriveAtAccidentSite();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        trafficJamThread.start();
    }

    private static synchronized void arriveAtAccidentSite() {
        // Sincronizar para que la cuenta de coches sea correcta y no haya condiciones de carrera.
        waitingCars++;
        System.out.println("A car has arrived at the accident site. Waiting cars: " + waitingCars);

        if (waitingCars >= MAX_WAITING_CARS) {
            System.out.println("Traffic jam is congested with " + waitingCars + " cars waiting.");
        }

        try {
            Thread.sleep(ACCIDENT_SITE_PASS_TIME); // Simulate time taken to pass the accident site
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            waitingCars--;
            System.out.println("A car has passed the accident site. Waiting cars: " + waitingCars);
        }
    }
}
