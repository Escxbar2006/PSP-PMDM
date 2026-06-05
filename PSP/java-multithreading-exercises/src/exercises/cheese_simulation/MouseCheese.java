import java.util.Random;

/**
 * Ratón que toma quesos de la bandeja hasta que no queden (o productor termine).
 */
public class MouseCheese implements Runnable {
    private final CheeseTray tray;
    private final String name;
    private final Random rng = new Random();

    public MouseCheese(CheeseTray tray, String name) {
        this.tray = tray;
        this.name = name;
    }

    @Override
    public void run() {
        while (true) {
            String cheese;
            try {
                cheese = tray.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (cheese == null) {
                System.out.println(name + ": no more cheeses, finishing.");
                return;
            }

            System.out.println(name + " ate " + cheese + ".");

            // small eating delay
            try { Thread.sleep(rng.nextInt(201)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }
}
