import java.util.Random;

/**
 * Productor de quesos que deposita en la `CheeseTray`.
 */
public class Producer implements Runnable {
    private final CheeseTray tray;
    private final int totalToProduce;
    private final Random rng = new Random();

    public Producer(CheeseTray tray, int totalToProduce) {
        this.tray = tray;
        this.totalToProduce = totalToProduce;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalToProduce; i++) {
            String cheese = (i % 2 == 0) ? "Gruyere" : "Fresco";
            try {
                tray.put(cheese);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            System.out.println("Producer placed " + cheese + " (" + (i+1) + "/" + totalToProduce + ")");

            // small random delay between productions
            try { Thread.sleep(rng.nextInt(201)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }

        tray.setDone();
        System.out.println("Producer finished producing.");
    }
}
