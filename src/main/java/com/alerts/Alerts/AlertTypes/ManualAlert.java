package com.alerts.Alerts.AlertTypes;

import com.alerts.Alerts.Alert;

public class ManualAlert extends Alert {

    public ManualAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getAlertType() {
        return "Manual";
    }

}
