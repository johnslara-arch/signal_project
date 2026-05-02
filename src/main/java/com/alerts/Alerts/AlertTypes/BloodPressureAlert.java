package com.alerts.Alerts.AlertTypes;

import com.alerts.Alerts.Alert;

public class BloodPressureAlert extends Alert {

    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getAlertType() {
        return "BloodPressure";
    }

}
