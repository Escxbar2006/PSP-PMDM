/**
 * Clase `Belt` que encapsula la cinta transportadora compartida.
 *
 * Paso a paso:
 * - Mantiene la lista de productos en la cinta y la sincronización (lock + conditions).
 * - Proporciona `put(int type)` para el colocador y `take(int type)` para los empacadores.
 * - Permite marcar `donePlacing` cuando el colocador termina, de modo que los empacadores
 *   puedan terminar si no quedan más productos.
 */

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Belt {
    private final int capacity;
    private final List<Integer> belt = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull;
    private final Condition[] typeAvailable;

    private volatile boolean donePlacing = false;

    public Belt(int capacity) {
        this.capacity = capacity;
        this.notFull = lock.newCondition();
        this.typeAvailable = new Condition[3];
        for (int i = 0; i < 3; i++) typeAvailable[i] = lock.newCondition();
    }

    // Colocar un producto en la cinta; espera si la cinta está llena.
    public void put(int prodType) throws InterruptedException {
        lock.lock();
        try {
            while (belt.size() >= capacity) {
                notFull.await();
            }
            belt.add(prodType);
            // Avisar a los empacadores interesados en este tipo.
            typeAvailable[prodType - 1].signal();
        } finally {
            lock.unlock();
        }
    }

    // Tomar un producto de un tipo específico. Devuelve -1 si no quedan productos y el colocador terminó.
    public int take(int myType) throws InterruptedException {
        lock.lock();
        try {
            while (!containsType(myType)) {
                if (donePlacing && belt.isEmpty()) return -1;
                typeAvailable[myType - 1].await();
            }

            int item = -1;
            for (int i = 0; i < belt.size(); i++) {
                if (belt.get(i) == myType) {
                    item = belt.remove(i);
                    break;
                }
            }
            // Avisar al colocador que hay espacio.
            notFull.signal();
            return item;
        } finally {
            lock.unlock();
        }
    }

    private boolean containsType(int t) {
        for (Integer x : belt) if (x == t) return true;
        return false;
    }

    // Marcar que el colocador ha terminado de producir
    public void setDonePlacing() {
        lock.lock();
        try {
            donePlacing = true;
            // Despertar a todos los empacadores para que terminen si corresponde.
            for (Condition c : typeAvailable) c.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
