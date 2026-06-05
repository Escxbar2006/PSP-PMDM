/**
 * Ejercicio: El baile de Hogwarts.
 *
 * Paso a paso:
 * 1. Se define un director de baile que decide cuántas rotaciones habrá.
 * 2. Cuatro bailarinas eligen su pareja en cada rotación.
 * 3. Las parejas cambian, no se repite el compañero anterior y no puede haber
 *    emparejamientos entre Gryffindor y Slytherin.
 * 4. Si no hay otra opción válida, la bailarina descansa una rotación.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HogwartsDance {
    // Constantes de configuración del baile
    private static final int MIN_ROTATIONS = 4; // mínimo de rotaciones
    private static final int MAX_ROTATIONS = 8; // máximo de rotaciones
    private static final int MIN_ROTATION_SECONDS = 3; // duración mínima por rotación (s)
    private static final int MAX_ROTATION_SECONDS = 6; // duración máxima por rotación (s)
    private static final int DANCE_TIMEOUT_MINUTES = 1; // tope de espera al finalizar executor

    // Sincronización:
    // - `lock` protege todas las variables compartidas de la clase.
    // - `rotationSignal` despierta a las bailarinas cuando empieza una nueva rotación.
    // - `rotationComplete` permite al director esperar hasta que todas hayan decidido.
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition rotationSignal = lock.newCondition();
    private final Condition rotationComplete = lock.newCondition();

    // Generador aleatorio compartido (para decisiones y duraciones)
    private final Random rng = new Random();

    // Listas de participantes (inmutables en tiempo de ejecución)
    private final List<String> females = Arrays.asList("Hermione", "Pansy", "Tamsin", "Luna");
    private final List<String> males = Arrays.asList("Harry", "Draco", "Cedric", "Terry");

    // Mapas que indican la casa de cada participante (usados para reglas de emparejamiento)
    private final Map<String, String> femaleHouse = new HashMap<>();
    private final Map<String, String> maleHouse = new HashMap<>();

    // Estado del baile (compartido entre hilos):
    // - currentPartner: pareja asignada en la rotación actual (por chica)
    // - lastPartner: compañero anterior para evitar repetir
    // - assignedMales: chicos ya asignados en la rotación actual (evita duplicados)
    private final Map<String, String> currentPartner = new HashMap<>();
    private final Map<String, String> lastPartner = new HashMap<>();
    private final Set<String> assignedMales = new HashSet<>();

    // Contadores y flags de control
    private int currentRotation = 0; // número de rotación que está en curso
    private int totalRotations = 0; // número total decidido por el director
    private int femaleDecisionsRemaining = 0; // cuántas chicas faltan por decidir en la rotación
    private boolean danceFinished = false; // indicador para terminar los hilos

    public HogwartsDance() {
        // Inicializar mapa de casas para aplicar la regla Gryffindor<->Slytherin
        femaleHouse.put("Hermione", "Gryffindor");
        femaleHouse.put("Pansy", "Slytherin");
        femaleHouse.put("Tamsin", "Hufflepuff");
        femaleHouse.put("Luna", "Ravenclaw");

        maleHouse.put("Harry", "Gryffindor");
        maleHouse.put("Draco", "Slytherin");
        maleHouse.put("Cedric", "Hufflepuff");
        maleHouse.put("Terry", "Ravenclaw");

        // Parejas iniciales: cada chica empieza con el chico de su misma casa.
        currentPartner.put("Hermione", "Harry");
        currentPartner.put("Pansy", "Draco");
        currentPartner.put("Tamsin", "Cedric");
        currentPartner.put("Luna", "Terry");

        // lastPartner guarda el compañero previo para evitar repetirlo en la siguiente rotación.
        lastPartner.putAll(currentPartner);
    }

    public static void main(String[] args) throws InterruptedException {
        HogwartsDance dance = new HogwartsDance();
        dance.startDance();
    }

    public void startDance() throws InterruptedException {
        // Crear un ExecutorService con 5 hilos: 4 bailarinas + 1 director.
        // Usamos ExecutorService porque el enunciado requiere su uso en lugar de hilos directos.
        ExecutorService executor = Executors.newFixedThreadPool(5);

        // Lanzar una tarea por cada bailarina. Cada tarea esperará las señales
        // del director para decidir su nueva pareja en cada rotación.
        for (String female : females) {
            executor.submit(() -> femaleDanceLoop(female));
        }

        // Lanzar la tarea del director que coordina las rotaciones.
        executor.submit(this::directorLoop);

        // No aceptar más tareas y esperar a que terminen (con timeout prudente).
        executor.shutdown();
        executor.awaitTermination(DANCE_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // Asegurar cierre: marcar finished y despertar a cualquier hilo bloqueado.
        lock.lock();
        try {
            danceFinished = true;
            rotationSignal.signalAll();
        } finally {
            lock.unlock();
        }

        System.out.println("\nEl baile de Hogwarts ha terminado.");
    }

    private void directorLoop() {
        // Decide aleatoriamente el número de rotaciones entre MIN y MAX.
        totalRotations = rng.nextInt(MAX_ROTATIONS - MIN_ROTATIONS + 1) + MIN_ROTATIONS;
        System.out.println("El director de baile ha decidido que habrá " + totalRotations + " rotaciones.\n");

        // Mostrar las parejas iniciales antes de empezar cualquier rotación.
        printCurrentPairs("Parejas iniciales");

        // Bucle principal del director: por cada rotación ordena el inicio,
        // espera a que todas las chicas decidan y reproduce la música.
        for (int rotation = 1; rotation <= totalRotations; rotation++) {
            // Señalar a las bailarinas que comienza una nueva rotación.
            startNewRotation(rotation);

            // Esperar a que todas las bailarinas hayan hecho su elección.
            waitForFemaleDecisions();

            // Mostrar el resultado de las elecciones de esta rotación.
            printCurrentPairs("Resultado de rotación " + rotation);

            // Reproducir la música durante la duración aleatoria de la rotación.
            playMusicForRotation();
        }

        // Finalizar: marcar la bandera y despertar a los hilos por si alguno sigue esperando.
        lock.lock();
        try {
            danceFinished = true;
            rotationSignal.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void startNewRotation(int rotation) {
        // Preparar las variables de estado para una nueva rotación y despertar a las chicas.
        lock.lock();
        try {
            // Actualizar número de rotación y resetear contador de decisiones.
            currentRotation = rotation;
            femaleDecisionsRemaining = females.size();

            // Limpiar las asignaciones previas para evitar duplicados en la nueva rotación.
            assignedMales.clear();

            // Despertar a todas las bailarinas para que elijan su pareja.
            rotationSignal.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void waitForFemaleDecisions() {
        // El director espera hasta que todas las bailarinas reduzcan
        // `femaleDecisionsRemaining` a 0. La última en decidir hará `signal`.
        lock.lock();
        try {
            while (femaleDecisionsRemaining > 0) {
                try {
                    rotationComplete.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void femaleDanceLoop(String female) {
        int observedRotation = 0;

        while (true) {
            lock.lock();
            try {
                // Esperar a la señal del director para la nueva rotación.
                while (!danceFinished && observedRotation == currentRotation) {
                    try {
                        rotationSignal.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Si el baile terminó mientras estaba esperando, salir.
                if (danceFinished) {
                    return;
                }

                // Ya hay una nueva rotación: recordar el número y elegir pareja.
                observedRotation = currentRotation;
                chooseNewPartner(female);

                // Registrar que esta bailarina ha tomado su decisión.
                femaleDecisionsRemaining--;

                // La última en decidir despierta al director (por eso solo signal una vez).
                if (femaleDecisionsRemaining == 0) {
                    rotationComplete.signal();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private void chooseNewPartner(String female) {
        // Determinar compañero previo para evitar repetir.
        String previous = lastPartner.get(female);

        // Construir conjunto de chicos disponibles (los que no han sido asignados aún).
        Set<String> availableMales = new HashSet<>(males);
        availableMales.removeAll(assignedMales);

        // Filtrar según reglas: no repetir anterior y no emparejar Gryffindor<->Slytherin.
        List<String> valid = new ArrayList<>();
        for (String male : availableMales) {
            if (male.equals(previous)) {
                // No repetir compañero anterior.
                continue;
            }
            if (forbiddenPair(female, male)) {
                // Regla no escrita: no Gryffindor con Slytherin.
                continue;
            }
            valid.add(male);
        }

        // Si no hay opciones válidas, la chica descansa esta rotación.
        if (valid.isEmpty()) {
            // Si la única opción restante es el anterior o no hay disponibles, descansan.
            if ((availableMales.size() == 1 && availableMales.contains(previous))
                    || availableMales.isEmpty()) {
                System.out.println(female + " no puede cambiar y descansa esta rotación.");
                currentPartner.put(female, "Descanso");
                return;
            }
            System.out.println(female + " no encontró pareja válida y descansa esta rotación.");
            currentPartner.put(female, "Descanso");
            return;
        }

        // Elegir aleatoriamente una opción válida, actualizar estado y marcar al chico asignado.
        String selected = valid.get(rng.nextInt(valid.size()));
        currentPartner.put(female, selected);
        lastPartner.put(female, selected);
        assignedMales.add(selected);
        System.out.println(female + " elige a " + selected + " como nueva pareja.");
    }

    private boolean forbiddenPair(String female, String male) {
        // Comprueba la regla que prohíbe emparejar Gryffindor con Slytherin.
        String femaleHouseName = femaleHouse.get(female);
        String maleHouseName = maleHouse.get(male);
        return ("Gryffindor".equals(femaleHouseName) && "Slytherin".equals(maleHouseName))
            || ("Slytherin".equals(femaleHouseName) && "Gryffindor".equals(maleHouseName));
    }

    private void playMusicForRotation() {
        // Elegir duración aleatoria para la rotación y simular el paso del tiempo
        int duration = rng.nextInt(MAX_ROTATION_SECONDS - MIN_ROTATION_SECONDS + 1) + MIN_ROTATION_SECONDS;
        System.out.println("La rotación durará " + duration + " segundos.\n");

        // Cada segundo imprimimos que suena la música y dormimos 1s.
        for (int second = 1; second <= duration; second++) {
            System.out.println("La música suena: segundo " + second + "...");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // Si se interrumpe, restaurar la señal y salir.
                Thread.currentThread().interrupt();
                return;
            }
        }

        System.out.println("\nFin de la rotación.\n");
    }

    private void printCurrentPairs(String title) {
        // Imprimir el estado actual de las parejas (o descanso si aplica).
        System.out.println("--- " + title + " ---");
        for (String female : females) {
            String partner = currentPartner.get(female);
            if (partner == null || "Descanso".equals(partner)) {
                System.out.println(female + " (" + femaleHouse.get(female) + ") - descansa");
            } else {
                System.out.println(female + " (" + femaleHouse.get(female) + ") - " + partner + " (" + maleHouse.get(partner) + ")");
            }
        }
        System.out.println();
    }
}
