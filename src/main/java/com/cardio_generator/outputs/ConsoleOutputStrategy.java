package com.cardio_generator.outputs;

/**
 * This class implements the OutputStrategy interface to write generated data to
 * a console.
 */
public class ConsoleOutputStrategy implements OutputStrategy {

    /**
     * Prints the generated data to the console. Data is
     * written in the format "Patient ID: id, Timestamp: time, Label: label, Data:
     * data".
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label     the label for the generated data.
     * @param data      the generated data.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        System.out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
    }
}
