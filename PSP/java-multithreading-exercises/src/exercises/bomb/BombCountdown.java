/**
 * Simulación de una cuenta regresiva con posibilidad de desactivación.
 *
 * Paso a paso:
 * 1. Un hilo hace la cuenta regresiva de 9 a 0.
 * 2. Otro hilo intenta desactivar la bomba después de un tiempo aleatorio.
 * 3. Si la desactivación ocurre antes de llegar a cero, la bomba no explota.
 */
public class BombCountdown {
    private volatile boolean defused = false;

    public void startCountdown() {
        Thread countdownThread = new Thread(() -> {
            // Este hilo realiza la cuenta regresiva segundo a segundo.
            for (int i = 9; i >= 0; i--) {
                if (defused) {
                    System.out.println("Bomb defused!");
                    return;
                }
                System.out.println("Countdown: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println("Boom! The bomb exploded.");
        });

        countdownThread.start();
    }

    public void defuseBomb() {
        Thread defuserThread = new Thread(() -> {
            try {
                // Simular el tiempo que tarda el desactivador en intentar detener la bomba.
                int delay = (int) (Math.random() * 5000);
                Thread.sleep(delay);
                defused = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        defuserThread.start();
    }

    public static void main(String[] args) {
        BombCountdown bombCountdown = new BombCountdown();
        bombCountdown.startCountdown();
        bombCountdown.defuseBomb();
    }
}
