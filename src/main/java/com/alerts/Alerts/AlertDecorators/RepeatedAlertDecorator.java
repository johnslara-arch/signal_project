package com.alerts.Alerts.AlertDecorators;

import java.util.List;

import com.alerts.AlertStrategy.AlertStrategy;
import com.alerts.AlertStrategy.BloodPressureStrategy;
import com.alerts.AlertStrategy.BloodSaturationStrategy;
import com.alerts.AlertStrategy.EcgStrategy;
import com.alerts.AlertStrategy.HypotensiveHypoxemiaStrategy;
import com.alerts.AlertStrategy.ManualStrategy;
import com.data_management.DataStorage;
import com.alerts.Alerts.Alert;
import com.data_management.PatientRecord;

/**
 * Rechecks the records of the patient who triggered the alert within an
 * indicated time interval for another alert of the same type.
 */
public class RepeatedAlertDecorator extends AlertDecorator {

    private long startTime;
    private long endTime;
    private DataStorage dataStorage;

    public RepeatedAlertDecorator(Alert alert, DataStorage dataStorage, long startTime, long endTime) {
        super(alert);
        this.startTime = startTime;
        this.endTime = endTime;
        this.dataStorage = dataStorage;
    }

    public void recheckAlertCondition() {
        int patientID = Integer.parseInt(decoratedAlert.getPatientId());
        List<PatientRecord> recordsToCheck = dataStorage.getRecords(patientID, startTime, endTime);

        String alertType = decoratedAlert.getAlertType();
        AlertStrategy strategy = getStrategyForRecheck(alertType);

        strategy.checkAlert(recordsToCheck);
    }

    private AlertStrategy getStrategyForRecheck(String alertType) {
        switch (alertType) {
            case "BloodPressure":
                return new BloodPressureStrategy();
            case "BloodSaturation":
                return new BloodSaturationStrategy();
            case "Ecg":
                return new EcgStrategy();
            case "HypotensiveHypoxemia":
                return new HypotensiveHypoxemiaStrategy();
            case "Manual":
                return new ManualStrategy();
            default:
                throw new IllegalArgumentException("Unknown alert type detected: " + alertType);
        }
    }

}
