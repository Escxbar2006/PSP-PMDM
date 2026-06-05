import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Bandeja de queso (recurso compartido) con capacidad limitada.
 *
 * Paso a paso:
 * - `put(String cheese)` agrega un queso si hay espacio, esperando si está llena.
 * - `take()` retira un queso disponible o devuelve null si el productor terminó.
 */
public class CheeseTray {
    private final int capacity;
    private final List<String> tray = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private volatile boolean done = false;

    public CheeseTray(int capacity) {
        this.capacity = capacity;
    }

    public void put(String cheese) throws InterruptedException {
        lock.lock();
        try {
            while (tray.size() >= capacity) notFull.await();
            tray.add(cheese);
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String take() throws InterruptedException {
        lock.lock();
        try {
            while (tray.isEmpty()) {
                if (done) return null;
                notEmpty.await();
            }
            String c = tray.remove(0);
            notFull.signal();
            return c;
        } finally {
            lock.unlock();
        }
    }

    public void setDone() {
        lock.lock();
        try {
            done = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
