package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

public class AlertGenerator implements PatientDataGenerator {

    // Changed variable name to lowerCamelCase.
    private boolean[] alertStates; // false = resolved, true = pressed

    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    // Moved randomGenerator declaration closer to the point it is first used as per 4.8.2.2 in Guide.
    public static final Random randomGenerator = new Random();

    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (randomGenerator.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // Changed variable names to UPPER_CASE as CONSTANT.
                double LAMBDA = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double P = -Math.expm1(-LAMBDA); // Probability of at least one alert in the period
                boolean alertTriggered = randomGenerator.nextDouble() < P;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
