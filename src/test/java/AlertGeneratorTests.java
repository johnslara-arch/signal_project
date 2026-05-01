import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;

import java.util.List;
import java.util.ArrayList;

@DisplayName("AlertGenerator")
public class AlertGeneratorTests {

    /**
     * The following subclass of AlertGenerator overrides the triggerAlert method
     * so that alerts produced by the following tests can be checked.
     */
    static class SpyAlertGenerator extends AlertGenerator {
        final List<Alert> captured = new ArrayList<>();

        SpyAlertGenerator(DataStorage storage) {
            super(storage);
        }

        @Override
        protected void triggerAlert(Alert alert) {
            captured.add(alert);
        }
    }

    /**
     * Case insensitive check that examines the kind of alerts the alertgenerator
     * has created.
     * 
     * @param gen       the SpyAlertGenerator that created the alert.
     * @param substring the kind of alert which should be found.
     * 
     * @return true if the expected substring is found in the alert created.
     */
    private boolean containsCondition(SpyAlertGenerator gen, String substring) {
        return gen.captured.stream().anyMatch(a -> a.getCondition().toLowerCase().contains(substring.toLowerCase()));
    }

    @Nested
    @DisplayName("AlertGenerator — Blood Pressure")
    class BloodPressureAlertTests {

        DataStorage storage;
        SpyAlertGenerator generator;

        @BeforeEach
        void setUp() {
            storage = new DataStorage();
            generator = new SpyAlertGenerator(storage);
        }

        @Test
        @DisplayName("Creates consecutive trend alert for both systolic and diastolic BPs when condition is met.")
        void consecutiveTrendAlertCreated() {

            // Systolic increasing trend.
            storage.addPatientData(1, 110, "SystolicPressure", 1000L);
            storage.addPatientData(1, 125, "SystolicPressure", 2000L);
            storage.addPatientData(1, 140, "SystolicPressure", 3000L);

            // Systolic decreasing trend.
            storage.addPatientData(2, 140, "SystolicPressure", 1000L);
            storage.addPatientData(2, 125, "SystolicPressure", 2000L);
            storage.addPatientData(2, 110, "SystolicPressure", 3000L);

            // Diastolic increasing trend.
            storage.addPatientData(3, 110, "DiastolicPressure", 1000L);
            storage.addPatientData(3, 125, "DiastolicPressure", 2000L);
            storage.addPatientData(3, 140, "DiastolicPressure", 3000L);

            // Diastolic decreasing trend.
            storage.addPatientData(4, 140, "DiastolicPressure", 1000L);
            storage.addPatientData(4, 125, "DiastolicPressure", 2000L);
            storage.addPatientData(4, 110, "DiastolicPressure", 3000L);

            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Increasing Trend"));

            generator.evaluateData(storage.getAllPatients().get(1));
            assertTrue(containsCondition(generator, "Decreasing Trend"));

            generator.evaluateData(storage.getAllPatients().get(2));
            assertTrue(containsCondition(generator, "Increasing Trend"));

            generator.evaluateData(storage.getAllPatients().get(3));
            assertTrue(containsCondition(generator, "Decreasing Trend"));
        }

        @Test
        @DisplayName("Does not create consecutive trend alert when changes are smaller than 10 mmHg.")
        void noConsecutiveTrendAlert() {
            storage.addPatientData(1, 110, "SystolicPressure", 1000L);
            storage.addPatientData(1, 118, "SystolicPressure", 2000L);
            storage.addPatientData(1, 130, "SystolicPressure", 3000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Increasing Trend"));
        }

        @Test
        @DisplayName("Creates critical threshold alerts when bounds are exceeded.")
        void createsCriticalThresholdAlerts() {
            storage.addPatientData(1, 185, "SystolicPressure", 1000L);
            storage.addPatientData(2, 85, "SystolicPressure", 1000L);
            storage.addPatientData(3, 125, "DiastolicPressure", 1000L);
            storage.addPatientData(4, 55, "DiastolicPressure", 1000L);

            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Critically High"));

            generator.evaluateData(storage.getAllPatients().get(1));
            assertTrue(containsCondition(generator, "Critically Low"));

            generator.evaluateData(storage.getAllPatients().get(2));
            assertTrue(containsCondition(generator, "Critically High"));

            generator.evaluateData(storage.getAllPatients().get(3));
            assertTrue(containsCondition(generator, "Critically Low"));
        }

        @Test
        @DisplayName("No critical threshold alert created if no bounds exceeded.")
        void normalBPNoAlerts() {
            storage.addPatientData(1, 120, "SystolicPressure", 1000L);
            storage.addPatientData(1, 80, "DiastolicPressure", 1000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Critically"));
        }
    }

    @Nested
    @DisplayName("AlertGenerator — Blood Saturation")
    class BloodSaturationAlertTests {

        DataStorage storage;
        SpyAlertGenerator generator;

        @BeforeEach
        void setUp() {
            storage = new DataStorage();
            generator = new SpyAlertGenerator(storage);
        }

        @Test
        @DisplayName("Creates low saturation alert when value drops below 92%.")
        void lowSaturationAlertCreated() {
            storage.addPatientData(1, 91, "Saturation", 1000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Low Blood Saturation"));
        }

        @Test
        @DisplayName("Does not create low saturation alert when value is exactly 92% or above.")
        void noLowSaturationAlertCreated() {
            storage.addPatientData(1, 92, "Saturation", 1000L);
            storage.addPatientData(1, 98, "Saturation", 2000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Low Blood Saturation"));
        }

        @Test
        @DisplayName("Creates rapid drop alert when saturation drops by 5% or more within 10 minutes.")
        void rapidSaturationDropAlertCreated() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 98, "Saturation", time - 3 * 60 * 1000L); // 3 mins ago
            storage.addPatientData(1, 93, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Rapid Blood Saturation Drop"));
        }

        @Test
        @DisplayName("Does not create rapid drop alert when the drop occurs over more than 10 minutes.")
        void longerThan10Minutes() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 98, "Saturation", time - 15 * 60 * 1000L); // 15 mins ago
            storage.addPatientData(1, 93, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Rapid Blood Saturation Drop"));
        }

        @Test
        @DisplayName("Does not create rapid drop alert when drop is less than 5%.")
        void lessThan5SaturationDrop() {
            long time = System.currentTimeMillis();
            storage.addPatientData(5, 98, "Saturation", time - 3 * 60 * 1000L);
            storage.addPatientData(5, 95, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Rapid Blood Saturation Drop"));
        }
    }

    @Nested
    @DisplayName("AlertGenerator — Hypotensive Hypoxemia")
    class HypotensiveHypoxemiaTests {

        DataStorage storage;
        SpyAlertGenerator generator;

        @BeforeEach
        void setUp() {
            storage = new DataStorage();
            generator = new SpyAlertGenerator(storage);
        }

        @Test
        @DisplayName("Creates hypotensive hypoxemia alert when conditions are met simultaneously.")
        void conditionsMetSimultaneously() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 85, "Systolic", time - 30_000L);
            storage.addPatientData(1, 90, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Hypotensive Hypoxemia"));
        }

        @Test
        @DisplayName("Does not create alert if only systolic BP is low.")
        void onlySystolicConditionMet() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 85, "SystolicPressure", time);
            storage.addPatientData(1, 95, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Hypotensive Hypoxemia"));
        }

        @Test
        @DisplayName("Does not create alert if only saturation is low.")
        void onlySaturationConditionMet() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 120, "SystolicPressure", time);
            storage.addPatientData(1, 90, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Hypotensive Hypoxemia"));
        }

        @Test
        @DisplayName("Does not create alert if one of the records is too old (irrelevant).")
        void recordsTooFarApart() {
            long time = System.currentTimeMillis();
            storage.addPatientData(1, 85, "SystolicPressure", time - 2 * 60 * 1000L);
            storage.addPatientData(1, 90, "Saturation", time);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Hypotensive Hypoxemia"));
        }
    }

    @Nested
    @DisplayName("AlertGenerator — ECG")
    class EcgAlertTests {

        DataStorage storage;
        SpyAlertGenerator generator;

        @BeforeEach
        void setUp() {
            storage = new DataStorage();
            generator = new SpyAlertGenerator(storage);
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
            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Abnormal ECG Peak"));
        }

        @Test
        @DisplayName("Does not create an alert if all readings are consistent with sliding window average.")
        void consistentEcgReadings() {
            for (int i = 0; i < 25; i++) {
                storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
            }
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Abnormal ECG Peak"));
        }

        @Test
        @DisplayName("Does not create an alert if there aren't enough records to calculate an average for given sliding window size.")
        void tooFewReadings() {
            for (int i = 0; i < 5; i++) {
                storage.addPatientData(1, 1.0, "ECG", 1000L + i * 100L);
            }
            storage.addPatientData(1, 10.0, "ECG", 1000L + 20 * 100L); // abnormal peak
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Abnormal ECG Peak"));
        }
    }

    @Nested
    @DisplayName("AlertGenerator — Manual Alerts")
    class TriggeredAlertTests {

        DataStorage storage;
        SpyAlertGenerator generator;

        @BeforeEach
        void setUp() {
            storage = new DataStorage();
            generator = new SpyAlertGenerator(storage);
        }

        @Test
        @DisplayName("Creates a manual alert when Alert record has value 1.0 (triggered).")
        void triggeredAlertFound() {
            storage.addPatientData(1, 1.0, "Alert", 1000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertTrue(containsCondition(generator, "Manual Alert Triggered"));
        }

        @Test
        @DisplayName("Does not create a manual alert when Alert record has value 0.0 (resolved).")
        void resolvedAlertFound() {
            storage.addPatientData(1, 0.0, "Alert", 1000L);
            generator.evaluateData(storage.getAllPatients().get(0));
            assertFalse(containsCondition(generator, "Manual Alert Triggered"));
        }

    }

}
