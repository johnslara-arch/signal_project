package com.alerts.Alerts.AlertTypes;

import com.alerts.Alerts.Alert;

public class HypotensiveHypoxemiaAlert extends Alert {

    public HypotensiveHypoxemiaAlert(String patientId, String condition, long timestamp) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getAlertType() {
        return "HypotensiveHypoxemia";
    }

}
