package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * This class implements the PatientDataGenerator interface to generate blood
 * level data for patients including cholesterol, white cell, and red cell
 * levels.
 * 
 * The generator stores a random baseline level for each patient based on
 * realistic values for each data type. It then applies small variations to each
 * baseline blood level value recorded so the simulated values over time emulate
 * realistic changes to patient levels.
 */
public class BloodLevelsDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private final double[] baselineCholesterol;
    private final double[] baselineWhiteCells;
    private final double[] baselineRedCells;

    /**
     * Constructor to initialise the blood level data generator for a given
     * number of patients. It initialises a random baseline value for each
     * patient's cholesterol, white cell and red cell count.
     * 
     * Baseline cholesterol values range between 150 and 200.
     * Baseline white cell values range between 4 and 10.
     * Baseline red cell values range between 4.5 and 6.
     * 
     * @param patientCount the number of patients for whom to generate data.
     */
    public BloodLevelsDataGenerator(int patientCount) {
        // Initialize arrays to store baseline values for each patient
        baselineCholesterol = new double[patientCount + 1];
        baselineWhiteCells = new double[patientCount + 1];
        baselineRedCells = new double[patientCount + 1];

        // Generate baseline values for each patient
        for (int i = 1; i <= patientCount; i++) {
            baselineCholesterol[i] = 150 + random.nextDouble() * 50; // Initial random baseline
            baselineWhiteCells[i] = 4 + random.nextDouble() * 6; // Initial random baseline
            baselineRedCells[i] = 4.5 + random.nextDouble() * 1.5; // Initial random baseline
        }
    }

    /**
     * Generates blood level data for a given patient and outputs it using the
     * provided OutputStrategy. It simulates small fluctuations in baseline values
     * while maintaining them within a realistic range.
     * 
     * Fluctuations for cholesterol values range from -5 to +5.
     * Fluctuations for white cell values range from -0.5 to +0.5.
     * Fluctuations for cholesterol values range from -0.1 to +0.1.
     * 
     * @param patientId      the ID of the patient for whom to generate data.
     * @param outputStrategy the strategy to use for outputting the generated data.
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Generate values around the baseline for realism
            double cholesterol = baselineCholesterol[patientId] + (random.nextDouble() - 0.5) * 10; // Small variation
            double whiteCells = baselineWhiteCells[patientId] + (random.nextDouble() - 0.5) * 1; // Small variation
            double redCells = baselineRedCells[patientId] + (random.nextDouble() - 0.5) * 0.2; // Small variation

            // Output the generated values
            outputStrategy.output(patientId, System.currentTimeMillis(), "Cholesterol", Double.toString(cholesterol));
            outputStrategy.output(patientId, System.currentTimeMillis(), "WhiteBloodCells",
                    Double.toString(whiteCells));
            outputStrategy.output(patientId, System.currentTimeMillis(), "RedBloodCells", Double.toString(redCells));
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood levels data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
