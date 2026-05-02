package com.alerts.AlertStrategy;

import java.util.List;

import com.alerts.AlertFactory.AlertFactory;
import com.alerts.AlertFactory.BloodSaturationAlertFactory;
import com.data_management.PatientRecord;

public class BloodSaturationStrategy extends HelpersAlertStrategy {

    private final AlertFactory satAlertFactory = new BloodSaturationAlertFactory();

    private static final long SATURATION_RAPID_DROP_WINDOW_MS = 10 * 60 * 1000L;

    /**
     * Checks blood saturation levels for low saturation (less than 92%) or rapid
     * drops in saturation (more than or equal to 5% within 10 minutes window).
     * If either condition is met it triggers a corresponding alert.
     * 
     * @param allRecords patient records retrieved from {@DataStorage}.
     */
    @Override
    public void checkAlert(List<PatientRecord> allRecords) {

        List<PatientRecord> saturationRecords = filterByType(allRecords, "Saturation");

        for (int i = 0; i < saturationRecords.size(); i++) {
            double value = saturationRecords.get(i).getMeasurementValue(); // In patient records saturation data is
                                                                           // already stored as a double not a string
                                                                           // such as 95%.

            if (value < 92) {
                triggerAlert(satAlertFactory.createAlert(String.valueOf(saturationRecords.get(i).getPatientId()),
                        "Low Blood Saturation detected: " + value + "%", saturationRecords.get(i).getTimestamp()));
            }

            for (int j = i - 1; j >= 0; j--) {
                long timePassed = saturationRecords.get(i).getTimestamp() - saturationRecords.get(j).getTimestamp();
                if (timePassed > SATURATION_RAPID_DROP_WINDOW_MS) {
                    break;
                }

                double oldValue = saturationRecords.get(j).getMeasurementValue();
                if (oldValue - value >= 5) {
                    triggerAlert(satAlertFactory.createAlert(String.valueOf(saturationRecords.get(i).getPatientId()),
                            "Rapid Blood Saturation drop from " + oldValue + "% to" + value,
                            saturationRecords.get(i).getTimestamp()));
                    break;
                }
            }
        }
    }

}
