import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.DataReaderOutputFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

@DisplayName("DataReaderOutputFile")
public class DataReaderOutputFileTests {

    @TempDir
    Path tempDir;

    DataStorage storage;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        this.storage = DataStorage.getInstance();
    }

    private void writeFile(String filename, String... lines) throws IOException {
        Path file = tempDir.resolve(filename);
        try (PrintWriter pw = new PrintWriter(file.toFile())) {
            for (String line : lines) {
                pw.println(line);
            }
        }
    }

    @Test
    @DisplayName("Parses a standard data line, with regular components, correctly.")
    void readStandardDataLine() throws IOException {
        writeFile("ECG_test_data.txt", "Patient ID: 1, Timestamp: 100000, Label: ECG, Data: 86.0");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);
        List<PatientRecord> records = storage.getRecords(1, 0L, 200000L);
        assertEquals(1, records.size());
        assertEquals(86.0, records.get(0).getMeasurementValue());
        assertEquals("ECG", records.get(0).getRecordType());
        assertEquals(100000L, records.get(0).getTimestamp());
    }

    @Test
    @DisplayName("Parses saturation value with % added to measurement value correctly")
    void readSaturationDataCorrectly() throws IOException {
        writeFile("Saturation_test_data.txt", "Patient ID: 2, Timestamp: 2000, Label: Saturation, Data: 93.0%");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(2, 0L, 5000L);
        assertEquals(1, records.size());
        assertEquals(93.0, records.get(0).getMeasurementValue());
    }

    @Test
    @DisplayName("Correctly converts 'triggered' manual alert condition to a measurement value of 1.0.")
    void mapsTriggeredManualAlertTo1() throws IOException {
        writeFile("Alert_test_data.txt", "Patient ID: 3, Timestamp: 3000, Label: Alert, Data: triggered");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(3, 0L, 5000L);
        assertEquals(1.0, records.get(0).getMeasurementValue());
    }

    @Test
    @DisplayName("Correctly converts 'resolved' manual alert condition to a measurement value of 0.0.")
    void mapsResolvedManualAlertTo1() throws IOException {
        writeFile("Alert_test_data.txt", "Patient ID: 3, Timestamp: 4000, Label: Alert, Data: resolved");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(3, 0L, 5000L);
        assertEquals(0.0, records.get(0).getMeasurementValue());
    }

    @Test
    @DisplayName("Reads multiple files in a directory if more than one .txt file present.")
    void readMultipleFiles() throws IOException {
        writeFile("BloodPressure_test_data.txt",
                "Patient ID: 1, Timestamp: 1000, Label: SystolicPressure, Data: 120.0");
        writeFile("Saturation_test_data.txt", "Patient ID: 1, Timestamp: 2000, Label: Saturation, Data: 98.0%");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, 5000L);
        assertEquals(2, records.size());
    }

    @Test
    @DisplayName("Ignores non-.txt files in the directory.")
    void ignoresIrrelevantFiles() throws IOException {
        writeFile("notes.csv", "Patient ID: 1, Timestamp: 1000, Label: ECG, Data: 72.0");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    @DisplayName("Continues to parse the file when a malformed line is detected, just skips it.")
    void skipsMalformatedLine() throws IOException {
        writeFile("Bad_test_data.txt", "this is not a valid line",
                "Patient ID: 1, Timestamp: 5000, Label: ECG, Data: 80.0");
        new DataReaderOutputFile(tempDir.toString()).readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 0L, 10000L);
        assertEquals(1, records.size());
    }

    @Test
    @DisplayName("Throws IOException when directory does not exist.")
    void directoryDoesNotExist() {
        DataReaderOutputFile reader = new DataReaderOutputFile("/wrong/path");
        assertThrows(IOException.class, () -> reader.readData(storage));
    }
}
