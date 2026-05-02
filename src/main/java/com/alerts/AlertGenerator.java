package com.alerts;

import com.alerts.AlertStrategy.AlertStrategy;
import com.alerts.AlertStrategy.BloodPressureStrategy;
import com.alerts.AlertStrategy.BloodSaturationStrategy;
import com.alerts.AlertStrategy.EcgStrategy;
import com.alerts.AlertStrategy.HypotensiveHypoxemiaStrategy;
import com.alerts.AlertStrategy.ManualStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

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

        List<AlertStrategy> allStrategies = List.of(new BloodPressureStrategy(), new BloodSaturationStrategy(),
                new HypotensiveHypoxemiaStrategy(), new EcgStrategy(), new ManualStrategy());

        for (AlertStrategy strategy : allStrategies) {
            strategy.checkAlert(allRecords);
        }
    }

}
