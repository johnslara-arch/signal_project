package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This class implements the PatientDataGenerator interface to generate alerts
 * for patients.
 */
public class AlertGenerator implements PatientDataGenerator {

    // google java style (s5.2.5): non-constant field names should be in
    // lowerCamelCase.
    // Changed field name to lowerCamelCase.
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Constructor to initialise the alert states for each patient.
     * 
     * @param patientCount the total number of patients to generate alerts for.
     */
    public AlertGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    // google java style (s4.8.2.2): variables should be declared when needed.
    // Moved randomGenerator declaration closer to the point it is first used as per
    public static final Random randomGenerator = new Random();

    /**
     * Generates alert data for a given patient. If the patient currently has an
     * active alert, it will be
     * resolved 90% of the time. If there is no active alert, there is a probability
     * that a new alert will
     * be triggered.
     * 
     * @param patientId      the ID of the patient for whom to generate alert data.
     * @param outputStrategy the strategy to use for outputting the generated alert
     *                       data.
     * @throws Exception if an error occurs during alert generation or output.
     */
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
                // google java style (s5.2.7): local variable names should be in lowerCamelCase.
                // Changed variable names to lowerCamelCase.
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency

                // Changed variable name from p to probabilityAtLeastOneAlert to make code
                // clearer.
                double probabilityAtLeastOneAlert = -Math.expm1(-lambda); // Probability of at least one alert in the
                                                                          // period
                boolean alertTriggered = randomGenerator.nextDouble() < probabilityAtLeastOneAlert;

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
