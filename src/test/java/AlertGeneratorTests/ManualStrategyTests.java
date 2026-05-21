package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.ManualStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

@DisplayName("ManualStrategy")
public class ManualStrategyTests {

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
    ManualStrategy strategy;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
        this.strategy = new ManualStrategy();
    }

    @Test
    @DisplayName("Creates a manual alert when Alert record has value 1.0 (triggered).")
    void triggeredAlertFound() {
        storage.addPatientData(1, 1.0, "Alert", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertTrue(containsCondition(alerts, "Manual Alert Triggered"));
    }

    @Test
    @DisplayName("Does not create a manual alert when Alert record has value 0.0 (resolved).")
    void resolvedAlertFound() {
        storage.addPatientData(1, 0.0, "Alert", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 2000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Manual Alert Triggered"));
    }

}
