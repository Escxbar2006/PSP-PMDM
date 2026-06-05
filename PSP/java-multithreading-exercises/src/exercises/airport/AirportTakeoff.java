/**
 * Simulación sencilla de despegue de aeronaves.
 *
 * Paso a paso:
 * 1. Se crea un hilo que simula la llegada de aeronaves cada cierto intervalo.
 * 2. Cada vez que llega una aeronave se registra que despegó.
 * 3. La simulación termina después de un tiempo fijo.
 */
public class AirportTakeoff {
    private static final int AIRCRAFT_ARRIVAL_INTERVAL = 2000; // milliseconds
    private static final int SIMULATION_DURATION = 30000; // milliseconds
    private static final int MAX_AIRCRAFT = 10;

    private int aircraftCount = 0;

    public static void main(String[] args) {
        AirportTakeoff airportTakeoff = new AirportTakeoff();
        airportTakeoff.startSimulation();
    }

    public void startSimulation() {
        // Iniciar el hilo que simula las llegadas de aeronaves.
        Thread arrivalThread = new Thread(this::aircraftArrival);
        arrivalThread.start();

        try {
            // Mantener la simulación activa durante el tiempo definido.
            Thread.sleep(SIMULATION_DURATION);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Simulation ended.");
    }

    private void aircraftArrival() {
        // Simular la llegada de aeronaves mientras no se alcance el máximo.
        while (aircraftCount < MAX_AIRCRAFT) {
            try {
                Thread.sleep(AIRCRAFT_ARRIVAL_INTERVAL);
                takeOffAircraft();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private synchronized void takeOffAircraft() {
        // Método sincronizado para actualizar el conteo de forma segura.
        if (aircraftCount < MAX_AIRCRAFT) {
            aircraftCount++;
            System.out.println("Aircraft " + aircraftCount + " is taking off.");
        }
    }
}
