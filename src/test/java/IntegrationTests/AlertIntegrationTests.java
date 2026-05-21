package IntegrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.alerts.AlertGenerator;
import com.alerts.Alerts.Alert;
import com.data_management.DataReader;
import com.data_management.DataReaderOutputFile;
import com.data_management.DataStorage;
import com.data_management.Patient;

@DisplayName("AlertGenerator Integration")
public class AlertIntegrationTests {

    @TempDir
    Path tempDir;

    DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
    }

    /**
     * Case insensitive check that examines the kind of alerts the strategy
     * has created.
     * 
     * @param alerts    the list of alerts created by the strategy.
     * @param substring the kind of alert which should be found.
     * 
     * @return true if the expected substring is found in the alert created.
     */
    private boolean containsCondition(List<Alert> alerts, String substring) {
        return alerts.stream().anyMatch(a -> a.getCondition().toLowerCase().contains(substring.toLowerCase()));
    }

    @Test
    @DisplayName("Can read patient data from a file, store records, and generate an appropriate alert when the condition is met in those records.")
    void fileInputGeneratesAlert() throws IOException {
        Path file = tempDir.resolve("ecg_data.txt"); // Will use ECG data as an example.

        try (PrintWriter pw = new PrintWriter(file.toFile())) {
            for (int i = 0; i < 20; i++) {
                pw.println("Patient ID: 1, Timestamp: " + (1000 + i * 100) + ", Label: ECG, Data: 1.0");
            }
            pw.println("Patient ID: 1, Timestamp: 4000, Label: ECG, Data: 10.0"); // Generate abnormal peak.
        }

        DataReader reader = new DataReaderOutputFile(tempDir.toString());
        reader.readData(storage);

        Patient patient = storage.getAllPatients().get(0);
        AlertGenerator generator = new AlertGenerator(storage);
        List<Alert> alerts = generator.evaluateData(patient);

        assertEquals(1, alerts.size());
        assertTrue(containsCondition(alerts, "Abnormal ECG Peak"));
    }

}
