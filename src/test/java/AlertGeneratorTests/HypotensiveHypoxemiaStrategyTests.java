package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.HypotensiveHypoxemiaStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

@DisplayName("HypotensiveHypoxemiaStrategy")
public class HypotensiveHypoxemiaStrategyTests {

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
    HypotensiveHypoxemiaStrategy strategy;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
        this.strategy = new HypotensiveHypoxemiaStrategy();
    }

    @Test
    @DisplayName("Creates hypotensive hypoxemia alert when conditions are met simultaneously.")
    void conditionsMetSimultaneously() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 85, "Systolic", time - 30_000L);
        storage.addPatientData(1, 90, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertTrue(containsCondition(alerts, "Hypotensive Hypoxemia"));
    }

    @Test
    @DisplayName("Does not create alert if only systolic BP is low.")
    void onlySystolicConditionMet() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 85, "SystolicPressure", time);
        storage.addPatientData(1, 95, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Hypotensive Hypoxemia"));
    }

    @Test
    @DisplayName("Does not create alert if only saturation is low.")
    void onlySaturationConditionMet() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 120, "SystolicPressure", time);
        storage.addPatientData(1, 90, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Hypotensive Hypoxemia"));
    }

    @Test
    @DisplayName("Does not create alert if one of the records is too old (irrelevant).")
    void recordsTooFarApart() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 85, "SystolicPressure", time - 2 * 60 * 1000L);
        storage.addPatientData(1, 90, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Hypotensive Hypoxemia"));
    }
}