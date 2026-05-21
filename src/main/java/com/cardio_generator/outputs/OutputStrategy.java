package com.cardio_generator.outputs;

/**
 * This interface defines the strategy used to output patient data.
 * 
 * Implementations of this interface define how the simulated health data is
 * stored or shared. Supported strategies include console output, TCP sockets,
 * file-based storage, and WebSocket connections.
 * 
 * The Strategy design pattern is used, which allows output strategies to be
 * exchanged at runtime (depending on user choice) independently from data
 * generation logic.
 */
public interface OutputStrategy {

    /**
     * Sends the patient data to the output destination, depending on the chosen
     * strategy.
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label     the label for the generated data.
     * @param data      the generated data.
     */
    void output(int patientId, long timestamp, String label, String data);
}
