package com.data_management;

import com.cardio_generator.outputs.WebSocketClient;

import java.io.IOException;
import java.net.URI;

public class DataReaderWebSocket implements DataReader {

    private final URI serverUri;
    private WebSocketClient client;

    public DataReaderWebSocket(URI serverUri) {
        this.serverUri = serverUri;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        client = new WebSocketClient(serverUri, dataStorage);
        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("WebSocket connection was interrupted: " + e.getMessage(), e);
        }
    }

    public void stopConnectionToServer() {
        if (client != null) {
            client.shutdown();
        }
    }

}
