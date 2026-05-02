package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;
import com.alerts.Alerts.AlertTypes.BloodSaturationAlert;

public class BloodSaturationAlertFactory implements AlertFactory {

    @Override
    public Alert createAlert(String patientID, String condition, long timestamp) {
        return new BloodSaturationAlert(patientID, condition, timestamp);
    }

}
