package com.cardio_generator.outputs;

/**
 * This interface defines the strategy used to output data.
 */
public interface OutputStrategy {

    /**
     * Outputs the generated data.
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label the label for the generated data.
     * @param data the generated data.
     */
    void output(int patientId, long timestamp, String label, String data);
}
