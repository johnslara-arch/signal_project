package com.alerts;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.BloodPressureAlertFactory;
import com.alerts.AlertFactory.BloodSaturationAlertFactory;
import com.alerts.AlertFactory.EcgAlertFactory;
import com.alerts.AlertFactory.HypotensiveHypoxemiaAlertFactory;
import com.alerts.AlertFactory.ManualAlertFactory;
import com.alerts.Alerts.Alert;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {

    private DataStorage dataStorage;
    private static final int ECG_WINDOW_SIZE = 20;
    private static final double ECG_PEAK_MULTIPLIER = 2;
    private static final long SATURATION_RAPID_DROP_WINDOW_MS = 10 * 60 * 1000L;

    private final AlertFactory bpAlertFactory = new BloodPressureAlertFactory();
    private final AlertFactory satAlertFactory = new BloodSaturationAlertFactory();
    private final AlertFactory hhAlertFactory = new HypotensiveHypoxemiaAlertFactory();
    private final AlertFactory ecgAlertFactory = new EcgAlertFactory();
    private final AlertFactory manualAlertFactory = new ManualAlertFactory();

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method called by the checkers. This method should define the specific
     * conditions under which an
     * alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> allRecords = patient.getRecords(0, currentTime);

        checkBloodPressureAlerts(allRecords);
        checkBloodSaturationAlerts(allRecords);
        checkHypotensiveHypoxemiaAlert(allRecords);
        checkEcgAlerts(allRecords);
        checkTriggeredAlerts(allRecords);
    }

    /**
     * Used to filter relevant records (e.g. ECG data) retrieved in the
     * {@code DataStorage}. Called by the
     * specified alert checker. Records are returned in the same order, insertion
     * order, as they were in {@code records}.
     * 
     * @param records the records from {@code DataStorage} to filter.
     * @param type    the label stating type of records to keep.
     * 
     * @return the filtered list of patient records.
     */
    private List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> filtered = new ArrayList<>();
        for (PatientRecord record : records) {
            if (type.equalsIgnoreCase(record.getRecordType())) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    // Blood Pressure Alerts!

    /**
     * Check blood pressure (BP) records for two alert generating conditions;
     * consecutive trend and critical threshold. Consecutive trend consists of 3
     * consecutive
     * reading changing by more than 10 mmHg in the same direction (increasing or
     * decreasing). Critical threshold is met when systolic BP falls below 90 mmHg
     * or above 180 mmHg,
     * or when diastolic BP falls under 60 mmHg or above 120 mmHg.
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    private void checkBloodPressureAlerts(List<PatientRecord> allRecords) {
        List<PatientRecord> systolicRecords = filterByType(allRecords, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(allRecords, "DiastolicPressure");

        checkBpTrendAlert(systolicRecords, "Systolic");
        checkBpTrendAlert(diastolicRecords, "Diastolic");
        checkBpCriticalThresholds(systolicRecords, 90, 180, "Systolic");
        checkBpCriticalThresholds(diastolicRecords, 60, 120, "Diastolic");
    }

    /**
     * Triggers an alert if consecutive trend conditions are met.
     * 
     * @param records filtered list of BP records (either systolic or diastolic).
     * @param label   type of BP that triggers alert.
     */
    private void checkBpTrendAlert(List<PatientRecord> records, String label) {
        if (records.size() < 3) {
            return;
        }
        for (int i = 2; i < records.size(); i++) {
            double reading1 = records.get(i - 2).getMeasurementValue();
            double reading2 = records.get(i - 1).getMeasurementValue();
            double reading3 = records.get(i).getMeasurementValue();

            double diff1 = reading2 - reading1;
            double diff2 = reading3 - reading2;

            boolean increasing = diff1 > 10 && diff2 > 10;
            boolean decreasing = diff1 < -10 && diff2 < -10;

            if (increasing) {
                triggerAlert(bpAlertFactory.createAlert(String.valueOf(records.get(i).getPatientId()),
                        label + " Blood Pressure shows increasing trend", records.get(i).getTimestamp()));
            } else if (decreasing) {
                triggerAlert(bpAlertFactory.createAlert(String.valueOf(records.get(i).getPatientId()),
                        label + " Blood Pressure shows decreasing trend", records.get(i).getTimestamp()));
            }
        }

    }

    /**
     * Triggers an alert if critical threshold conditions are met.
     * 
     * @param records     filtered list of BP records (either systolic or
     *                    diastolic).
     * @param lowerBound  lower critical threshold for BP.
     * @param higherBound higher critical threshold for BP.
     * @param label       type of BP that triggers alert.
     */
    private void checkBpCriticalThresholds(List<PatientRecord> records, double lowerBound, double higherBound,
            String label) {
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (value > higherBound) {
                triggerAlert(bpAlertFactory.createAlert(String.valueOf(record.getPatientId()),
                        label + " Blood Pressure critically high: " + value, record.getTimestamp()));
            } else if (value < lowerBound) {
                triggerAlert(bpAlertFactory.createAlert(String.valueOf(record.getPatientId()),
                        label + " Blood Pressure critically low: " + value, record.getTimestamp()));
            }
        }
    }

    // Blood Saturation Alerts!

    /**
     * Checks blood saturation levels for low saturation (less than 92%) or rapid
     * drops in saturation (more than or equal to 5% within 10 minutes window).
     * If either condition is met it triggers a corresponding alert.
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    private void checkBloodSaturationAlerts(List<PatientRecord> allRecords) {
        List<PatientRecord> saturationRecords = filterByType(allRecords, "Saturation");

        for (int i = 0; i < saturationRecords.size(); i++) {
            double value = saturationRecords.get(i).getMeasurementValue(); // In patient records saturation data is
                                                                           // already stored as a double not a string
                                                                           // such as 95%.

            if (value < 92) {
                triggerAlert(satAlertFactory.createAlert(String.valueOf(saturationRecords.get(i).getPatientId()),
                        "Low Blood Saturation detected: " + value + "%", saturationRecords.get(i).getTimestamp()));
            }

            for (int j = i - 1; j >= 0; j--) {
                long timePassed = saturationRecords.get(i).getTimestamp() - saturationRecords.get(j).getTimestamp();
                if (timePassed > SATURATION_RAPID_DROP_WINDOW_MS) {
                    break;
                }

                double oldValue = saturationRecords.get(j).getMeasurementValue();
                if (oldValue - value >= 5) {
                    triggerAlert(satAlertFactory.createAlert(String.valueOf(saturationRecords.get(i).getPatientId()),
                            "Rapid Blood Saturation drop from " + oldValue + "% to" + value,
                            saturationRecords.get(i).getTimestamp()));
                    break;
                }
            }
        }
    }

    // Hypotensive Hypoxemia Alert!

    /**
     * Checks for a combined alert condition. Triggers an alert if both systolic BP
     * is below 90 mmHg
     * and blood saturation levels are below 92% at the same time (defined time
     * proximity as records occuring within a minute of one another).
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    private void checkHypotensiveHypoxemiaAlert(List<PatientRecord> allRecords) {
        List<PatientRecord> systolicRecords = filterByType(allRecords, "Systolic");
        List<PatientRecord> saturationRecords = filterByType(allRecords, "Saturation");

        for (PatientRecord systolicRecord : systolicRecords) {
            double systolicValue = systolicRecord.getMeasurementValue();
            if (systolicValue >= 90) {
                continue;
            }
            for (int i = saturationRecords.size() - 1; i >= 0; i--) {
                PatientRecord saturationRecord = saturationRecords.get(i);
                long timePassed = Math.abs(systolicRecord.getTimestamp() - saturationRecord.getTimestamp());

                // Assume that if saturation records are taken over a minute before systolic BP
                // record being examined they are not relevant.
                if (timePassed > 60_000) {
                    continue; // As no relevant saturation record was found, move to next systolic BP record.
                }

                if (saturationRecord.getMeasurementValue() < 92) {
                    triggerAlert(hhAlertFactory.createAlert(String.valueOf(systolicRecord.getPatientId()),
                            "Hypotensive Hypoxemia Alert!", systolicRecord.getTimestamp()));
                    break;
                }
            }

        }
    }

    // ECG Alerts!

    /**
     * Checks ECG records for values that exceed a sliding window average using
     * {@value #ECG_WINDOW_SIZE} by a factor of {@value #ECG_PEAK_MULTIPLIER}.
     * The size of the factor and number of values used to calculate sliding window
     * have been assumed to be 2 and 20 respectively. Triggers an alert when
     * this condition is met.
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    private void checkEcgAlerts(List<PatientRecord> allRecords) {
        List<PatientRecord> ecgRecords = filterByType(allRecords, "ECG");

        if (ecgRecords.size() < ECG_WINDOW_SIZE) {
            return;
        }

        for (int i = ECG_WINDOW_SIZE; i < ecgRecords.size(); i++) {
            double average = 0;
            for (int j = i; j > i - ECG_WINDOW_SIZE; j--) {
                average += ecgRecords.get(j).getMeasurementValue();
            }
            average = average / ECG_WINDOW_SIZE;

            if (average > 0 && ecgRecords.get(i).getMeasurementValue() > ECG_PEAK_MULTIPLIER * average) {
                triggerAlert(ecgAlertFactory.createAlert(String.valueOf(ecgRecords.get(i).getPatientId()),
                        "Abnormal ECG peak detected: " + ecgRecords.get(i).getMeasurementValue(),
                        ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    // Manually Triggered Alerts!

    /**
     * Checks records for manually activated alerts, which can be done by a nurse or
     * a patient pressing the alert button.
     * If the "triggered" condition is found, a respective alert is triggered.
     * As defined in {@code DataReaderOutputFile}, a "triggered" condition is found
     * when the data value is equal to 1.0.
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    private void checkTriggeredAlerts(List<PatientRecord> allRecords) {
        List<PatientRecord> alertRecords = filterByType(allRecords, "Alert");

        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(
                        manualAlertFactory.createAlert(String.valueOf(record.getPatientId()), "Manual Alert triggered",
                                record.getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    protected void triggerAlert(Alert alert) {
        System.out.printf("ALERT -> Patient ID: %s | Condition: %s | Time: %d%n", alert.getPatientId(),
                alert.getCondition(), alert.getTimestamp());
    }
}
