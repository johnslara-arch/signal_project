package com.alerts.AlertStrategy;

import java.util.ArrayList;
import java.util.List;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.BloodPressureAlertFactory;
import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

public class BloodPressureStrategy extends HelpersAlertStrategy {

    private final AlertFactory bpAlertFactory = new BloodPressureAlertFactory();

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
    @Override
    public List<Alert> checkAlert(List<PatientRecord> allRecords) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> systolicRecords = filterByType(allRecords, "SystolicPressure");
        List<PatientRecord> diastolicRecords = filterByType(allRecords, "DiastolicPressure");

        alerts.addAll(checkBpTrendAlert(systolicRecords, "Systolic"));
        alerts.addAll(checkBpTrendAlert(diastolicRecords, "Diastolic"));
        alerts.addAll(checkBpCriticalThresholds(systolicRecords, 90, 180, "Systolic"));
        alerts.addAll(checkBpCriticalThresholds(diastolicRecords, 60, 120, "Diastolic"));

        return alerts;
    }

    /**
     * Triggers an alert if consecutive trend conditions are met.
     * 
     * @param records filtered list of BP records (either systolic or diastolic).
     * @param label   type of BP that triggers alert.
     */
    private List<Alert> checkBpTrendAlert(List<PatientRecord> records, String label) {
        List<Alert> alerts = new ArrayList<>();
        if (records.size() < 3) {
            return alerts;
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
                alerts.add(bpAlertFactory.createAlert(String.valueOf(records.get(i).getPatientId()),
                        label + " Blood Pressure shows increasing trend", records.get(i).getTimestamp()));
            } else if (decreasing) {
                alerts.add(bpAlertFactory.createAlert(String.valueOf(records.get(i).getPatientId()),
                        label + " Blood Pressure shows decreasing trend", records.get(i).getTimestamp()));
            }
        }
        return alerts;

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
    private List<Alert> checkBpCriticalThresholds(List<PatientRecord> records, double lowerBound, double higherBound,
            String label) {
        List<Alert> alerts = new ArrayList<>();
        for (PatientRecord record : records) {
            double value = record.getMeasurementValue();
            if (value > higherBound) {
                alerts.add(bpAlertFactory.createAlert(String.valueOf(record.getPatientId()),
                        label + " Blood Pressure critically high: " + value, record.getTimestamp()));
            } else if (value < lowerBound) {
                alerts.add(bpAlertFactory.createAlert(String.valueOf(record.getPatientId()),
                        label + " Blood Pressure critically low: " + value, record.getTimestamp()));
            }
        }
        return alerts;
    }

}
