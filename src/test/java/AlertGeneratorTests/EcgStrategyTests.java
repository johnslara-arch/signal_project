package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.EcgStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

@DisplayName("EcgStrategy")
public class EcgStrategyTests {

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

    DataStorage storage;
    EcgStrategy strategy;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
        this.strategy = new EcgStrategy();
    }

    @Test
    @DisplayName("Creates ECG peak alert when condition is met.")
    void abnormalPeakAlertCreated() {
        // Fill 20 baseline readings to create valid sliding window for average
        // calculation.
        for (int i = 0; i < 20; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
        }
        storage.addPatientData(1, 10.0, "ECG", 1000L + 20 * 100L); // abnormal peak
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);

        List<Alert> alerts = strategy.checkAlert(records);
        assertTrue(containsCondition(alerts, "Abnormal ECG Peak"));
    }

    @Test
    @DisplayName("Does not create an alert if all readings are consistent with sliding window average.")
    void consistentEcgReadings() {
        for (int i = 0; i < 25; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
        }
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Abnormal ECG Peak"));
    }

    @Test
    @DisplayName("Does not create an alert if there aren't enough records to calculate an average for given sliding window size.")
    void tooFewReadings() {
        for (int i = 0; i < 5; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
        }
        storage.addPatientData(1, 10.0, "ECG", 1000L + 5 * 100L); // abnormal peak
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Abnormal ECG Peak"));
    }
}
