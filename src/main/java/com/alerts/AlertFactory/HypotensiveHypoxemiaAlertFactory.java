package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;
import com.alerts.Alerts.HypotensiveHypoxemiaAlert;

public class HypotensiveHypoxemiaAlertFactory implements AlertFactory {

    @Override
    public Alert createAlert(String patientID, String condition, long timestamp) {
        return new HypotensiveHypoxemiaAlert(patientID, condition, timestamp);
    }

}
