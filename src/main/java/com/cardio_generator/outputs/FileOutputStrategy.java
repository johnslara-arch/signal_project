package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements the OutputStrategy interface to write generated data to
 * text files.
 * 
 * Each type of data (ECG, Blood Pressure...) is recorded in a separate file.
 * These files are created inside the base directory if they don't exist or
 * added to each time new data is registered.
 */
public class FileOutputStrategy implements OutputStrategy {

    // google java style (s5.2.5): non-constant field names should be in
    // lowerCamelCase.
    // Changed field names to lowerCamelCase as neither are constants.
    private String baseDirectory;
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Constructor for FileOutputStrategy. Initialises the base directory where
     * files will be written, creates a new one if it doesn't exist.
     * 
     * @param baseDirectory the directory where files will be written.
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory; // Vertical space removed as did not improve readability.
    }

    /**
     * Adds the generated data to its respective file in the base directory. Data is
     * written in the format "Patient ID: id, Timestamp: time, Label: label, Data:
     * data".
     * 
     * Side effect: If directory and files don't exist on the local file system they
     * are created.
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label     the label for the generated data.
     * @param data      the generated data.
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        // google java style (s5.2.7): local variable names should be in lowerCamelCase.
        // Changed variable name to lowerCamelCase.
        String filePath = fileMap.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}