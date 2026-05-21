package com.alerts.AlertStrategy;

import java.util.ArrayList;
import java.util.List;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.HypotensiveHypoxemiaAlertFactory;
import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

public class HypotensiveHypoxemiaStrategy extends HelpersAlertStrategy {

    private final AlertFactory hhAlertFactory = new HypotensiveHypoxemiaAlertFactory();

    /**
     * Checks for a combined alert condition. Triggers an alert if both systolic BP
     * is below 90 mmHg
     * and blood saturation levels are below 92% at the same time (defined time
     * proximity as records occuring within a minute of one another).
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    @Override
    public List<Alert> checkAlert(List<PatientRecord> allRecords) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> systolicRecords = filterByType(allRecords, "SystolicPressure");
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
                    alerts.add(hhAlertFactory.createAlert(String.valueOf(systolicRecord.getPatientId()),
                            "Hypotensive Hypoxemia Alert!", systolicRecord.getTimestamp()));
                    break;
                }
            }

        }
        return alerts;
    }

}
