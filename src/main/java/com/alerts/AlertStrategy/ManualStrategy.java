package com.alerts.AlertStrategy;

import java.util.List;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.ManualAlertFactory;
import com.data_management.PatientRecord;

/**
 * Checks records for manually activated alerts, which can be done by a nurse or
 * a patient pressing the alert button.
 * If the "triggered" condition is found, a respective alert is triggered.
 * As defined in {@code DataReaderOutputFile}, a "triggered" condition is found
 * when the data value is equal to 1.0.
 * 
 * @param allRecords patient records retrieved from {@DataStorage}.
 */
public class ManualStrategy extends HelpersAlertStrategy {

    private final AlertFactory manualAlertFactory = new ManualAlertFactory();

    @Override
    public void checkAlert(List<PatientRecord> allRecords) {
        List<PatientRecord> alertRecords = filterByType(allRecords, "Alert");

        for (PatientRecord record : alertRecords) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(
                        manualAlertFactory.createAlert(String.valueOf(record.getPatientId()), "Manual Alert triggered",
                                record.getTimestamp()));
            }
        }
    }

}
