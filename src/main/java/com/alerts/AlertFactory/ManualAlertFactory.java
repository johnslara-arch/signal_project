package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;
import com.alerts.Alerts.AlertTypes.ManualAlert;

public class ManualAlertFactory implements AlertFactory {

    @Override
    public Alert createAlert(String patientID, String condition, long timestamp) {
        return new ManualAlert(patientID, condition, timestamp);
    }

}
