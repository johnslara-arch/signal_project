package com.alerts;

import com.alerts.AlertStrategy.AlertStrategy;
import com.alerts.AlertStrategy.BloodPressureStrategy;
import com.alerts.AlertStrategy.BloodSaturationStrategy;
import com.alerts.AlertStrategy.EcgStrategy;
import com.alerts.AlertStrategy.HypotensiveHypoxemiaStrategy;
import com.alerts.AlertStrategy.ManualStrategy;
import com.alerts.Alerts.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.ArrayList;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {

    private DataStorage dataStorage;

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
     * are met. If a condition is met, an alert is triggered via the triggerAlert()
     * method.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        long currentTime = System.currentTimeMillis();
        List<PatientRecord> allRecords = patient.getRecords(0, currentTime);

        List<AlertStrategy> allStrategies = List.of(new BloodPressureStrategy(), new BloodSaturationStrategy(),
                new HypotensiveHypoxemiaStrategy(), new EcgStrategy(), new ManualStrategy());
        List<Alert> alerts = new ArrayList<>();

        for (AlertStrategy strategy : allStrategies) {
            alerts.addAll(strategy.checkAlert(allRecords));
        }

        for (Alert alert : alerts) {
            triggerAlert(alert);
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
