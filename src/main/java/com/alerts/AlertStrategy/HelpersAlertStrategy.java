package com.alerts.AlertStrategy;

import java.util.ArrayList;
import java.util.List;

import com.data_management.PatientRecord;

public abstract class HelpersAlertStrategy implements AlertStrategy {

    /**
     * Used to filter relevant records (e.g. ECG data) retrieved in the
     * {@code DataStorage}. Called by the
     * specified alert checker. Records are returned in the same order, insertion
     * order, as they were in {@code records}.
     * 
     * @param records the records from {@code DataStorage} to filter.
     * @param type    the label stating type of records to keep.
     * 
     * @return the filtered list of patient records.
     */
    protected List<PatientRecord> filterByType(List<PatientRecord> records, String type) {
        List<PatientRecord> filtered = new ArrayList<>();
        for (PatientRecord record : records) {
            if (type.equalsIgnoreCase(record.getRecordType())) {
                filtered.add(record);
            }
        }
        return filtered;
    }

}
