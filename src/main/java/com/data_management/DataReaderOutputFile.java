package com.data_management;

import java.io.IOException;
import java.nio.file.Files;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Implements {@link DataReader} to fetch output files produced by
 * {@code FileOutputStrategy} and read their data.
 * The data held in each file is patient records of the form:
 * Patient ID: 1, Timestamp: ..., Label: HeartRate, Data: ...
 * After reading every .txt file in the specified directory, it stores the data
 * in {@link DataStorage}.
 */
public class DataReaderOutputFile implements DataReader {

    // Where to find the files produced by the FileOutputStrategy
    private final String directoryPath;

    public DataReaderOutputFile(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * Goes through all the text files in a valid directory and stores valid records
     * into {@code dataStorage}.
     * 
     * @param dataStorage the place where valid records are stored.
     * 
     * @throws IOException if the directory cannot be read or file cannot be opened.
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        Path filePath = Paths.get(directoryPath);

        if (!Files.isDirectory(filePath)) {
            throw new IOException("Path provided is not an existing directory: " + filePath);
        }

        try (Stream<Path> files = Files.list(filePath)) {
            files.filter(p -> p.toString().endsWith(".txt")).forEach(file -> {
                try {
                    parseFile(file, dataStorage);
                } catch (IOException e) {
                    System.err.println("Could not read file " + file + ": " + e.getMessage());
                }
            });
        }

    }

    /**
     * Parses one file at a time and adds each valid record to {@code dataStorage}
     * once parsed.
     * 
     * @param filePath    the path of the file to parse.
     * @param dataStorage the storage space to add valid records to.
     * 
     * @throws IOException if the file cannot be read.
     */
    private void parseFile(Path filePath, DataStorage dataStorage) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    parseLine(line, dataStorage);
                } catch (Exception e) {
                    System.err.print("Unexpected format found, skipping line: " + lineNumber + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses one line at a time with the format specified at the top of this class.
     * Splits parts of the String on ", " with a look ahead (e.g. for "Patient ID:")
     * to ensure data
     * stays together.
     * 
     * @param line        the line to parse through.
     * @param dataStorage the storage space to add the obtained data to.
     * 
     * @throws IllegalArgumentException if line has wrong format so returns more or
     *                                  less than 4 fields.
     */
    private void parseLine(String line, DataStorage dataStorage) {

        String[] parts = line.split(", (?=[A-Z])");
        if (parts.length != 4) {
            throw new IllegalArgumentException("4 fields were expected, however only found " + parts.length);
        }

        int patientID = Integer.parseInt(extractValue(parts[0], "Patient ID"));
        long timestamp = Long.parseLong(extractValue(parts[1], "Timestamp"));
        String label = extractValue(parts[2], "Label");
        String data = extractValue(parts[3], "Data");

        double measurementValue = parseDataValue(data);
        dataStorage.addPatientData(patientID, measurementValue, label, timestamp);

    }

    /**
     * Extracts the actual value from the parts separated in the {@code parseLine}
     * method.
     * 
     * @param part       the string of the entire part parsed in parseLine.
     * @param typeOfData the type of value being extracted, used to indicate to user
     *                   which part of the data throws the exception.
     * 
     * @throws IllegalArgumentException if the delimiter for the data, the colon, is
     *                                  missing.
     * 
     * @return a String of the value extracted.
     */
    private String extractValue(String part, String typeOfData) {
        int colonIndex = part.indexOf(':');
        if (colonIndex < 0) {
            throw new IllegalArgumentException("Missing colon in part " + part + " representing " + typeOfData);
        }
        return part.substring(colonIndex + 1).trim();
    }

    /**
     * Used to convert the string of the data value to a double. Multiple scenarios
     * are possible.
     * For example, blood saturation values need to have "%" stripped.
     * An assumption was made to convert "triggered" values of alerts to 1.0 and
     * "resolved" values to 0.0.
     * 
     * @param data the string value obtained from the file.
     * 
     * @return the data value as a double.
     */
    private double parseDataValue(String data) {
        if (data.endsWith("%")) {
            return Double.parseDouble(data.substring(0, data.length() - 1));
        }
        switch (data.toLowerCase()) {
            case "triggered":
                return 1.0;
            case "resolved":
                return 0.0;
            default:
                return Double.parseDouble(data);
        }
    }

}
