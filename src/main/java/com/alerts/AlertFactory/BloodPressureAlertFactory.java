package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;
import com.alerts.Alerts.AlertTypes.BloodPressureAlert;

public class BloodPressureAlertFactory implements AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }

}
