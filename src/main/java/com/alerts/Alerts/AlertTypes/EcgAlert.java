package com.alerts.Alerts.AlertTypes;

import com.alerts.Alerts.Alert;

public class EcgAlert extends Alert {

    public EcgAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getAlertType() {
        return "Ecg";
    }

}
