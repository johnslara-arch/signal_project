package com.alerts.Alerts.AlertDecorators;

import com.alerts.Alerts.Alert;

/**
 * Adds priority tag to alerts requiring urgent attention.
 */
public class PriorityAlertDecorator extends AlertDecorator {

    public PriorityAlertDecorator(Alert alert) {
        super(alert);
    }

    @Override
    public String getCondition() {
        return "PRIORITY ALERT: " + decoratedAlert.getCondition();
    }

}
