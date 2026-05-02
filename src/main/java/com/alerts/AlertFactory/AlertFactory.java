package com.alerts.AlertFactory;

import com.alerts.Alerts.Alert;

public interface AlertFactory {

    public Alert createAlert(String patientId, String condition, long timestamp);
}
