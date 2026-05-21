package com.data_management;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alerts.AlertGenerator;

/**
 * Manages storage and retrieval of patient data within a healthcare monitoring
 * system.
 * This class serves as a repository for all patient records, organized by
 * patient IDs.
 */
public class DataStorage {
    private static DataStorage instance;
    private final Map<Integer, Patient> patientMap; // Stores patient objects indexed by
                                                    // their unique patient ID.

    /**
     * Constructs a new instance of DataStorage, initializing the underlying storage
     * structure. Concurrent HashMap is used to make DataStorage thread safe after
     * the implementation of the WebSocket output.
     */
    private DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    /**
     * This method ensures Singleton design pattern as it only
     * creates a new instance of the {@code DataStorage} if none exists.
     * 
     * @return new or existing instance of {@code DataStorage}.
     */
    public static DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    /**
     * This method is only used for testing. It is used to reset
     * Data Storage before each unit test is carried out.
     */
    public static void resetInstance() {
        instance = null;
    }

    /**
     * Adds or updates patient data in the storage.
     * If the patient does not exist, a new Patient object is created and added to
     * the storage.
     * Otherwise, the new data is added to the existing patient's records.
     * 
     * Current limitation of this method is the lack of check for duplicate data.
     * With additional time could add a checker for records already in the chosen
     * storage instance. Methods would be added to PatientRecord to check this.
     *
     * @param patientId        the unique identifier of the patient
     * @param measurementValue the value of the health metric being recorded
     * @param recordType       the type of record, e.g., "HeartRate",
     *                         "BloodPressure"
     * @param timestamp        the time at which the measurement was taken, in
     *                         milliseconds since the Unix epoch
     */
    public void addPatientData(int patientId, double measurementValue, String recordType, long timestamp) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            patient = new Patient(patientId);
            patientMap.put(patientId, patient);
        }
        patient.addRecord(measurementValue, recordType, timestamp);
    }

    /**
     * Retrieves a list of PatientRecord objects for a specific patient, filtered by
     * a time range.
     *
     * @param patientId the unique identifier of the patient whose records are to be
     *                  retrieved
     * @param startTime the start of the time range, in milliseconds since the Unix
     *                  epoch
     * @param endTime   the end of the time range, in milliseconds since the Unix
     *                  epoch
     * @return a list of PatientRecord objects that fall within the specified time
     *         range
     */
    public List<PatientRecord> getRecords(int patientId, long startTime, long endTime) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getRecords(startTime, endTime);
        }
        return new ArrayList<>(); // return an empty list if no patient is found
    }

    /**
     * Retrieves a collection of all patients stored in the data storage.
     *
     * @return a list of all patients
     */
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }

    /**
     * The main method for the DataStorage class.
     * Initializes the system, reads data into storage, and continuously monitors
     * and evaluates patient data.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {

        DataStorage storage = DataStorage.getInstance();

        if (args.length == 0) {
            System.err.println("directory path required.");
            return;
        }

        try {
            DataReader reader = new DataReaderOutputFile(args[0]);
            reader.readData(storage);
        } catch (IOException e) {
            System.err.println("Failed to read data from storage: " + e.getMessage());
            return;
        }

        // Initialize the AlertGenerator with the storage
        AlertGenerator alertGenerator = new AlertGenerator(storage);

        // Evaluate all patients' data to check for conditions that may trigger alerts
        for (Patient patient : storage.getAllPatients()) {
            alertGenerator.evaluateData(patient);
        }
    }
}
