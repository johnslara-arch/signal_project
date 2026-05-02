package com.alerts.Alerts.AlertTypes;

import com.alerts.Alerts.Alert;

public class BloodSaturationAlert extends Alert {

    public BloodSaturationAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getAlertType() {
        return "BloodSaturation";
    }

}
