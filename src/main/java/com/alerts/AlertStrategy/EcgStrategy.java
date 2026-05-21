package com.alerts.AlertStrategy;

import java.util.ArrayList;
import java.util.List;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.EcgAlertFactory;
import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

public class EcgStrategy extends HelpersAlertStrategy {

    private static final int ECG_WINDOW_SIZE = 20;
    private static final double ECG_PEAK_MULTIPLIER = 2;

    private final AlertFactory ecgAlertFactory = new EcgAlertFactory();

    /**
     * Checks ECG records for values that exceed a sliding window average using
     * {@value #ECG_WINDOW_SIZE} by a factor of {@value #ECG_PEAK_MULTIPLIER}.
     * The size of the factor and number of values used to calculate sliding window
     * have been assumed to be 2 and 20 respectively. Triggers an alert when
     * this condition is met.
     * 
     * @param allRecords patient records retrieved from {@Link DataStorage}.
     */
    @Override
    public List<Alert> checkAlert(List<PatientRecord> allRecords) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> ecgRecords = filterByType(allRecords, "ECG");

        if (ecgRecords.size() < ECG_WINDOW_SIZE) {
            return alerts;
        }

        for (int i = ECG_WINDOW_SIZE; i < ecgRecords.size(); i++) {
            double average = 0;
            for (int j = i; j > i - ECG_WINDOW_SIZE; j--) {
                average += ecgRecords.get(j).getMeasurementValue();
            }
            average = average / ECG_WINDOW_SIZE;

            if (average > 0 && ecgRecords.get(i).getMeasurementValue() > ECG_PEAK_MULTIPLIER * average) {
                alerts.add(ecgAlertFactory.createAlert(String.valueOf(ecgRecords.get(i).getPatientId()),
                        "Abnormal ECG peak detected: " + ecgRecords.get(i).getMeasurementValue(),
                        ecgRecords.get(i).getTimestamp()));
            }
        }

        return alerts;

    }

}
