# Java Multithreading Exercises

This project contains multiple exercises demonstrating the use of multithreading in Java. Each exercise simulates a different real-world scenario, showcasing how threads can be used to manage concurrent tasks effectively.

## Exercises Overview

1. **Bomb Countdown**
   - This exercise simulates a bomb countdown from 9 to 0. A defuser attempts to deactivate the bomb with a random delay. The countdown can be interrupted based on the outcome of the defusal attempt.

2. **Airport Takeoff**
   - This simulation models the takeoff of airplanes and light aircraft at an airport. Aircraft arrive every 2000 milliseconds, with airplanes prioritized over light aircraft. The simulation includes a countdown timer to manage the duration of the exercise.

3. **Supermarket Checkout**
   - This exercise simulates a supermarket checkout line where customers arrive every 1000 to 1500 milliseconds. It manages customer arrivals, checkout processing, and conditions for customer abandonment based on queue length. The simulation concludes when either 100 customers are served or 3 customers abandon the queue.

4. **Traffic Jam**
   - This simulation models a traffic jam scenario where cars arrive at an accident site every 200 to 400 milliseconds. It tracks the number of cars waiting and determines when the road becomes congested, managing the time taken for cars to pass through the accident site.

## Running the Simulations

To run the simulations, follow these steps:

1. Ensure you have Java and Maven installed on your machine.
2. Clone the repository or download the project files.
3. Navigate to the project directory in your terminal.
4. Use the following command to compile the project:
   ```
   mvn clean install
   ```
5. Run the main class to start all simulations:
   ```
   mvn exec:java -Dexec.mainClass="exercises.Main"
   ```

## Additional Notes

- Each exercise is implemented in its own Java file located in the `src/exercises` directory.
- Utility methods for thread management are provided in the `src/utils/ThreadHelper.java` file.
- The project is configured using Maven, and the `pom.xml` file contains all necessary dependencies and build settings.

Feel free to explore each exercise and modify the code to enhance your understanding of multithreading in Java!