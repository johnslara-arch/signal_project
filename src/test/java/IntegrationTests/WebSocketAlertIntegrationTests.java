package IntegrationTests;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.alerts.AlertGenerator;

@DisplayName("WebSocket Alert Integration")
public class WebSocketAlertIntegrationTests {

    // Integration testing - WARNING this requires a live server.
    // Tested by starting the signal generator with websocket:2121.

    private DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
    }

    // This spy class is used to capture all the created alerts without having them
    // printed to the console.
    private static class AlertGeneratorSpy extends AlertGenerator {

        private final List<Alert> triggeredAlerts = new ArrayList<>();

        public AlertGeneratorSpy(DataStorage storage) {
            super(storage);
        }

        @Override
        protected void triggerAlert(Alert alert) {
            triggeredAlerts.add(alert);
        }

        public List<Alert> getTriggeredAlerts() {
            return triggeredAlerts;
        }
    }

    @Test
    @DisplayName("Checks connection to a live server and correct data reception. Also checks that alert generation works.")
    @Disabled("Only run manually once the server on ws://localhost:2121 has been started.")
    void integrationWithServerTest() throws Exception {
        com.data_management.DataReaderWebSocket reader = new com.data_management.DataReaderWebSocket(
                URI.create("ws://localhost:2121"));
        AlertGeneratorSpy alertGenerator = new AlertGeneratorSpy(storage);
        reader.readData(storage);

        Thread.sleep(5000L); // Leave the connection open for a short time to receive some data
        reader.stopConnectionToServer();

        assertFalse(storage.getAllPatients().isEmpty()); // Some records should be stored after 3 seconds of
                                                         // connection to the server
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }

        assertFalse(alertGenerator.getTriggeredAlerts().isEmpty()); // Check at least one alert has been triggered.
    }
}
