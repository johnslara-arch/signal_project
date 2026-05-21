package com.alerts.AlertStrategy;

import com.data_management.PatientRecord;
import com.alerts.Alerts.*;

import java.util.List;

public interface AlertStrategy {

    public List<Alert> checkAlert(List<PatientRecord> allRecords);

}
