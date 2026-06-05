import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * Simulación simple de un checkout de supermercado con llegada y servicio de clientes.
 *
 * Paso a paso:
 * 1. Un hilo genera la llegada de clientes a intervalos aleatorios.
 * 2. Otro hilo atiende a los clientes en orden de llegada.
 * 3. Los clientes pueden abandonar si la cola se hace muy larga.
 */
public class SupermarketCheckout {
    private static final int MAX_CUSTOMERS = 100;
    private static final int MAX_ABANDONED = 3;
    private static final int CHECKOUT_TIME = 2000; // milliseconds
    private static final int MIN_ARRIVAL_TIME = 1000; // milliseconds
    private static final int MAX_ARRIVAL_TIME = 1500; // milliseconds

    private Queue<Integer> queue = new LinkedList<>();
    private int servedCustomers = 0;
    private int abandonedCustomers = 0;

    public static void main(String[] args) {
        SupermarketCheckout checkout = new SupermarketCheckout();
        checkout.startSimulation();
    }

    public void startSimulation() {
        // Iniciar hilos para llegada de clientes y proceso de caja.
        Thread customerArrivalThread = new Thread(this::manageCustomerArrivals);
        Thread checkoutThread = new Thread(this::processCheckout);
        
        customerArrivalThread.start();
        checkoutThread.start();
        
        try {
            customerArrivalThread.join();
            checkoutThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Simulation ended. Total served: " + servedCustomers + ", Total abandoned: " + abandonedCustomers);
    }

    private void manageCustomerArrivals() {
        Random random = new Random();
        while (servedCustomers < MAX_CUSTOMERS && abandonedCustomers < MAX_ABANDONED) {
            try {
                Thread.sleep(random.nextInt(MAX_ARRIVAL_TIME - MIN_ARRIVAL_TIME) + MIN_ARRIVAL_TIME);
                queue.add(servedCustomers);
                System.out.println("Customer " + servedCustomers + " arrived.");
                
                if (queue.size() > 5) {
                    abandonedCustomers++;
                    System.out.println("Customer " + servedCustomers + " abandoned the queue.");
                    queue.poll(); // Remove the first customer in the queue
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processCheckout() {
        while (servedCustomers < MAX_CUSTOMERS && abandonedCustomers < MAX_ABANDONED) {
            if (!queue.isEmpty()) {
                try {
                    Thread.sleep(CHECKOUT_TIME);
                    queue.poll(); // Serve the customer
                    servedCustomers++;
                    System.out.println("Customer " + servedCustomers + " has been served.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
