package com.alerts.AlertStrategy;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

public abstract class HelpersAlertStrategy implements AlertStrategy {

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
    protected List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> filtered = new ArrayList<>();
        for (PatientRecord record : records) {
            if (type.equalsIgnoreCase(record.getRecordType())) {
                filtered.add(record);
            }
        }
        return filtered;
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
