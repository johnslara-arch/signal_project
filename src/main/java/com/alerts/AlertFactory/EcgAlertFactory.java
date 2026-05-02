package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;
import com.alerts.Alerts.AlertTypes.EcgAlert;

public class EcgAlertFactory implements AlertFactory {

    @Override
    public Alert createAlert(String patientID, String condition, long timestamp) {
        return new EcgAlert(patientID, condition, timestamp);
    }

}
