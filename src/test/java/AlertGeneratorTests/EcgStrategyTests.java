package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.EcgStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.ArrayList;

@DisplayName("EcgStrategy")
public class EcgStrategyTests {

    /**
     * The following subclass of the concrete strategy class overrides the
     * triggerAlert method so that alerts produced by the following tests can be
     * checked.
     */
    static class SpyStrategy extends EcgStrategy {
        final List<Alert> captured = new ArrayList<>();

        @Override
        protected void triggerAlert(Alert alert) {
            captured.add(alert);
        }
    }

    /**
     * Case insensitive check that examines the kind of alerts the strategy
     * has created.
     * 
     * @param spy       the SpyStrategy that created the alert.
     * @param substring the kind of alert which should be found.
     * 
     * @return true if the expected substring is found in the alert created.
     */
    private boolean containsCondition(SpyStrategy spy, String substring) {
        return spy.captured.stream().anyMatch(a -> a.getCondition().toLowerCase().contains(substring.toLowerCase()));
    }

    DataStorage storage;
    SpyStrategy spy;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
        this.spy = new SpyStrategy();
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
        spy.checkAlert(records);
        assertTrue(containsCondition(spy, "Abnormal ECG Peak"));
    }

    @Test
    @DisplayName("Does not create an alert if all readings are consistent with sliding window average.")
    void consistentEcgReadings() {
        for (int i = 0; i < 25; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
        }
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Abnormal ECG Peak"));
    }

    @Test
    @DisplayName("Does not create an alert if there aren't enough records to calculate an average for given sliding window size.")
    void tooFewReadings() {
        for (int i = 0; i < 5; i++) {
            storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
        }
        storage.addPatientData(1, 10.0, "ECG", 1000L + 5 * 100L); // abnormal peak
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Abnormal ECG Peak"));
    }
}
