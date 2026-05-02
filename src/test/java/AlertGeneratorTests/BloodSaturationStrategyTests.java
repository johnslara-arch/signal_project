package AlertGeneratorTests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.AlertStrategy.BloodSaturationStrategy;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

@DisplayName("BloodSaturationStrategy")
public class BloodSaturationStrategyTests {

    /**
     * The following subclass of the concrete strategy class overrides the
     * triggerAlert method so that alerts produced by the following tests can be
     * checked.
     */
    static class SpyStrategy extends BloodSaturationStrategy {
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
    @DisplayName("Creates low saturation alert when value drops below 92%.")
    void lowSaturationAlertCreated() {
        storage.addPatientData(1, 91, "Saturation", 1000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        spy.checkAlert(records);
        assertTrue(containsCondition(spy, "Low Blood Saturation"));
    }

    @Test
    @DisplayName("Does not create low saturation alert when value is exactly 92% or above.")
    void noLowSaturationAlertCreated() {
        storage.addPatientData(1, 92, "Saturation", 1000L);
        storage.addPatientData(1, 98, "Saturation", 2000L);
        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Low Blood Saturation"));
    }

    @Test
    @DisplayName("Creates rapid drop alert when saturation drops by 5% or more within 10 minutes.")
    void rapidSaturationDropAlertCreated() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 98, "Saturation", time - 3 * 60 * 1000L); // 3 mins ago
        storage.addPatientData(1, 93, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        spy.checkAlert(records);
        assertTrue(containsCondition(spy, "Rapid Blood Saturation Drop"));
    }

    @Test
    @DisplayName("Does not create rapid drop alert when the drop occurs over more than 10 minutes.")
    void longerThan10Minutes() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 98, "Saturation", time - 15 * 60 * 1000L); // 15 mins ago
        storage.addPatientData(1, 93, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Rapid Blood Saturation Drop"));
    }

    @Test
    @DisplayName("Does not create rapid drop alert when drop is less than 5%.")
    void lessThan5SaturationDrop() {
        long time = System.currentTimeMillis();
        storage.addPatientData(1, 98, "Saturation", time - 3 * 60 * 1000L);
        storage.addPatientData(1, 95, "Saturation", time);
        List<PatientRecord> records = storage.getRecords(1, 0L, time + 1000L);
        spy.checkAlert(records);
        assertFalse(containsCondition(spy, "Rapid Blood Saturation Drop"));
    }
}