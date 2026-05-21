package com.alerts.Alerts.AlertDecorators;

import java.util.List;

import com.alerts.AlertStrategy.AlertStrategy;
import com.data_management.DataStorage;
import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

/**
 * Rechecks the records of the patient who triggered the alert within an
 * indicated time interval for another alert of the same type.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private final boolean repeated;
    private long startTime;
    private long endTime;

    public RepeatedAlertDecorator(Alert alert, AlertStrategy strategy, DataStorage dataStorage, long startTime,
            long endTime) {
        super(alert);
        this.startTime = startTime;
        this.endTime = endTime;

        int patientID = Integer.parseInt(decoratedAlert.getPatientId());
        List<PatientRecord> recordsToCheck = dataStorage.getRecords(patientID, startTime, endTime);
        List<Alert> repeatedAlerts = strategy.checkAlert(recordsToCheck);

        this.repeated = !repeatedAlerts.isEmpty();
    }

    @Override
    public String getCondition() {
        if (repeated) {
            return "REPEATED ALERT: " + decoratedAlert.getCondition() + " between " + startTime + " and " + endTime;
        }
        return decoratedAlert.getCondition();
    }

}
