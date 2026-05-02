package com.alerts.Alerts.AlertDecorators;

import com.alerts.Alerts.Alert;

public abstract class AlertDecorator extends Alert {

    protected Alert decoratedAlert;

    public AlertDecorator(Alert alert) {
        super(alert.getPatientId(), alert.getCondition(), alert.getTimestamp());
        this.decoratedAlert = alert;
    }

}
