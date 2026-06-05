/**
 * Clase principal que ejecuta varias simulaciones de hilos en paralelo.
 *
 * Paso a paso:
 * 1. Inicia cada simulación en su propio hilo.
 * 2. Espera a que todos los hilos terminen antes de salir.
 */
public class Main {
    public static void main(String[] args) {
        // Start Bomb Countdown
        BombCountdown bombCountdown = new BombCountdown();
        Thread bombThread = new Thread(() -> bombCountdown.startCountdown());
        bombThread.start();

        // Start Airport Takeoff Simulation
        AirportTakeoff airportTakeoff = new AirportTakeoff();
        Thread airportThread = new Thread(() -> airportTakeoff.startSimulation());
        airportThread.start();

        // Start Supermarket Checkout Simulation
        SupermarketCheckout supermarketCheckout = new SupermarketCheckout();
        Thread checkoutThread = new Thread(() -> supermarketCheckout.startSimulation());
        checkoutThread.start();

        // Start Traffic Jam Simulation
        TrafficJam trafficJam = new TrafficJam();
        Thread trafficThread = new Thread(() -> trafficJam.main(new String[0]));
        trafficThread.start();
        
        try {
            // Wait for all threads to finish
            bombThread.join();
            airportThread.join();
            checkoutThread.join();
            trafficThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
