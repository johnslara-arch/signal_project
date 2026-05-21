package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * This class implements the OutputStrategy interface to send generated data to
 * a chosen WebSocket server.
 * 
 * Clients can connect to view the generated data sent to this server. Once the
 * server is created it waits for a successful client connection to send the
 * data to.
 */
public class WebSocketOutputStrategy implements OutputStrategy {

    private WebSocketServer server;

    /**
     * Constructor for WebSocketOutputStrategy. Creates a WebSocket server at the
     * given port.
     * 
     * @param port the unique port used to create the WebSocket server.
     */
    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port + ", listening for connections...");
        server.start();
    }

    /**
     * Sends the generated data to all clients currently connected to the server.
     * Data is written in the format "patientId, timestamp, label, data".
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label     the label for the generated data.
     * @param data      the generated data.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        // Broadcast the message to all connected clients
        for (WebSocket conn : server.getConnections()) {
            conn.send(message);
        }
    }

    private static class SimpleWebSocketServer extends WebSocketServer {

        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used in this context
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}
