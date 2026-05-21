package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.BloodPressureStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

@DisplayName("BloodPressureStrategy")
public class BloodPressureStrategyTests {

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
    BloodPressureStrategy strategy;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
        this.strategy = new BloodPressureStrategy();
    }

    @Test
    @DisplayName("Creates consecutive trend alert for both systolic and diastolic BPs when condition is met.")
    void consecutiveTrendAlertCreated() {

        // Systolic increasing trend.
        storage.addPatientData(1, 110, "SystolicPressure", 1000L);
        storage.addPatientData(1, 125, "SystolicPressure", 2000L);
        storage.addPatientData(1, 140, "SystolicPressure", 3000L);
        List<PatientRecord> records1 = storage.getRecords(1, 0L, 4000L);

        // Systolic decreasing trend.
        storage.addPatientData(2, 140, "SystolicPressure", 1000L);
        storage.addPatientData(2, 125, "SystolicPressure", 2000L);
        storage.addPatientData(2, 110, "SystolicPressure", 3000L);
        List<PatientRecord> records2 = storage.getRecords(2, 0L, 4000L);

        // Diastolic increasing trend.
        storage.addPatientData(3, 110, "DiastolicPressure", 1000L);
        storage.addPatientData(3, 125, "DiastolicPressure", 2000L);
        storage.addPatientData(3, 140, "DiastolicPressure", 3000L);
        List<PatientRecord> records3 = storage.getRecords(3, 0L, 4000L);

        // Diastolic decreasing trend.
        storage.addPatientData(4, 140, "DiastolicPressure", 1000L);
        storage.addPatientData(4, 125, "DiastolicPressure", 2000L);
        storage.addPatientData(4, 110, "DiastolicPressure", 3000L);
        List<PatientRecord> records4 = storage.getRecords(4, 0L, 4000L);

        List<Alert> alerts1 = strategy.checkAlert(records1);
        List<Alert> alerts2 = strategy.checkAlert(records2);
        List<Alert> alerts3 = strategy.checkAlert(records3);
        List<Alert> alerts4 = strategy.checkAlert(records4);

        assertTrue(containsCondition(alerts1, "Increasing Trend"));
        assertTrue(containsCondition(alerts2, "Decreasing Trend"));
        assertTrue(containsCondition(alerts3, "Increasing Trend"));
        assertTrue(containsCondition(alerts4, "Decreasing Trend"));
    }

    @Test
    @DisplayName("Does not create consecutive trend alert when changes are smaller than 10 mmHg.")
    void noConsecutiveTrendAlert() {
        storage.addPatientData(1, 110, "SystolicPressure", 1000L);
        storage.addPatientData(1, 118, "SystolicPressure", 2000L);
        storage.addPatientData(1, 130, "SystolicPressure", 3000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);

        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Increasing Trend"));
    }

    @Test
    @DisplayName("Creates critical threshold alerts when bounds are exceeded.")
    void createsCriticalThresholdAlerts() {
        storage.addPatientData(1, 185, "SystolicPressure", 1000L);
        storage.addPatientData(2, 85, "SystolicPressure", 1000L);
        storage.addPatientData(3, 125, "DiastolicPressure", 1000L);
        storage.addPatientData(4, 55, "DiastolicPressure", 1000L);

        List<PatientRecord> records1 = storage.getRecords(1, 0L, 4000L);
        List<PatientRecord> records2 = storage.getRecords(2, 0L, 4000L);
        List<PatientRecord> records3 = storage.getRecords(3, 0L, 4000L);
        List<PatientRecord> records4 = storage.getRecords(4, 0L, 4000L);

        List<Alert> alerts1 = strategy.checkAlert(records1);
        List<Alert> alerts2 = strategy.checkAlert(records2);
        List<Alert> alerts3 = strategy.checkAlert(records3);
        List<Alert> alerts4 = strategy.checkAlert(records4);

        assertTrue(containsCondition(alerts1, "Critically High"));
        assertTrue(containsCondition(alerts2, "Critically Low"));
        assertTrue(containsCondition(alerts3, "Critically High"));
        assertTrue(containsCondition(alerts4, "Critically Low"));
    }

    @Test
    @DisplayName("No critical threshold alert created if no bounds exceeded.")
    void normalBPNoAlerts() {
        storage.addPatientData(1, 120, "SystolicPressure", 1000L);
        storage.addPatientData(1, 80, "DiastolicPressure", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);

        List<Alert> alerts = strategy.checkAlert(records);
        assertFalse(containsCondition(alerts, "Critically"));
    }

}
