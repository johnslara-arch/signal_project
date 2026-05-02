package com.alerts.AlertStrategy;

import com.data_management.PatientRecord;

import java.util.List;

public interface AlertStrategy {

    public void checkAlert(List<PatientRecord> allRecords);

}
