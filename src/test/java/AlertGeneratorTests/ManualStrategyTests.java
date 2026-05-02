package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.ManualStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

@DisplayName("ManualStrategy")
public class ManualStrategyTests {

    /**
     * The following subclass of the concrete strategy class overrides the
     * triggerAlert method so that alerts produced by the following tests can be
     * checked.
     */
    static class SpyStrategy extends ManualStrategy {
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
        storage = new DataStorage();
        spy = new SpyStrategy();
    }

    @Test
    @DisplayName("Creates a manual alert when Alert record has value 1.0 (triggered).")
    void triggeredAlertFound() {
        storage.addPatientData(1, 1.0, "Alert", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        spy.checkAlert(records);
        assertTrue(containsCondition(spy, "Manual Alert Triggered"));
    }

    @Test
    @DisplayName("Does not create a manual alert when Alert record has value 0.0 (resolved).")
    void resolvedAlertFound() {
        storage.addPatientData(1, 0.0, "Alert", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Manual Alert Triggered"));
    }

}
