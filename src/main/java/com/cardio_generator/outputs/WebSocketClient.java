package com.cardio_generator.outputs;

import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import com.data_management.DataStorage;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {

    private final DataStorage storage;
    private boolean manuallyClosed = false;

    // Constructor takes a URI
    public WebSocketClient(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.storage = dataStorage;
    }

    // Called when the connection is opened
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server at: " + getURI());
    }

    // Called when a message is received
    @Override
    public void onMessage(String message) {
        try {
            parseLine(message, storage);
        } catch (Exception e) {
            System.err.println("Unexpected format found, skipping message: " + message + " - " + e.getMessage());
        }
    }

    // Called when connection is closed. Tries to
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from WebSocket server.");

        // If the server disconnected unexpectedly try to reconnect.
        if (!manuallyClosed) {
            tryToReconnect();
        }
    }

    // Notifies client object that connection was closed manually.
    public void shutdown() {
        manuallyClosed = true;
        close();
    }

    // Attempts to reconnect client to server if unexpectedly disconnected.
    public void tryToReconnect() {
        System.out.println("Trying to reconnect to server...");

        try {
            Thread.sleep(3000);
            reconnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("WebSocket reconnection was interrupted: " + e.getMessage());
        }
    }

    // Called on error
    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket error: " + ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Parses one line at a time with the format
     * "PatientId,timestamp,label,measurementValue". Splits parts of the String on
     * "," and trims extra spaces.
     * 
     * @param line        the line to parse through.
     * @param dataStorage the storage space to add the obtained data to.
     * 
     * @throws IllegalArgumentException if line has wrong format so returns more or
     *                                  less than 4 fields.
     */
    private void parseLine(String line, DataStorage dataStorage) {

        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("4 fields were expected, however only found " + parts.length);
        }

        int patientID = Integer.parseInt(parts[0].trim());
        long timestamp = Long.parseLong(parts[1].trim());
        String label = parts[2].trim();
        String data = parts[3].trim();

        double measurementValue = parseDataValue(data);
        dataStorage.addPatientData(patientID, measurementValue, label, timestamp);

    }

    /**
     * Used to convert the string of the data value to a double. Multiple scenarios
     * are possible. For example, blood saturation values need to have "%" stripped.
     * An assumption was made to convert "triggered" values of alerts to 1.0 and
     * "resolved" values to 0.0.
     * 
     * @param data the string value obtained from the file.
     * 
     * @return the data value as a double.
     */
    private double parseDataValue(String data) {
        if (data.endsWith("%")) {
            return Double.parseDouble(data.substring(0, data.length() - 1));
        }
        switch (data.toLowerCase()) {
            case "triggered":
                return 1.0;
            case "resolved":
                return 0.0;
            default:
                return Double.parseDouble(data);
        }
    }
}
