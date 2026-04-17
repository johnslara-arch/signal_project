package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  This class implements the OutputStrategy interface to write generated data to files.
 */
public class FileOutputStrategy implements OutputStrategy {

    // Changed variable name to lowerCamelCase.
    private String baseDirectory;

    /**
     * Constructor for FileOutputStrategy. Initialises the base directory where
     * files will be written.
     * 
     * @param baseDirectory the directory where files will be written.
     */
    public FileOutputStrategy(String baseDirectory) {
        this.baseDirectory = baseDirectory; // Vertical space removed as did not improve readability.
    }

    //Changed variable name to lowerCamelCase.
    // Moved fileMap declaration closer to the point it is first used as per 4.8.2.2 in Guide.
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Outputs the generated data to a chosen file.
     * 
     * @param patientId the ID of the patient for whom the data is generated.
     * @param timestamp the timestamp of the generated data.
     * @param label the label for the generated data.
     * @param data the generated data.
     * @throws IOException if an error occurs while creating or writing tothe file.
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
        //Changed variable name to lowerCamelCase.
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