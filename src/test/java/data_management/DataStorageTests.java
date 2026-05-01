package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

@DisplayName("DataStorage")
class DataStorageTests {

    DataStorage storage;

    @BeforeEach
    void setUp() {
        this.storage = new DataStorage();
    }

    @Test
    @DisplayName("Adding a record for a new patient creates a retrievable patient record. Multiple records for the same patient accumulate.")
    void testAddAndGetRecords() {
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size()); // Check if two records are retrieved
        assertEquals(100.0, records.get(0).getMeasurementValue()); // Validate first record
    }

    @Test
    @DisplayName("The getRecords() method returns an empty list for an unknown patient.")
    void unknownPatientId() {
        storage.addPatientData(1, 100.0, "ECG", 1714376789050L);
        List<PatientRecord> records = storage.getRecords(99, 1714376789050L, 1714376789054L);
        assertNotNull(records);
        assertEquals(0, records.size());
    }

    @Test
    @DisplayName("The getRecords() method filters correctly by time range.")
    void filterByTimeRange() {
        storage.addPatientData(1, 100.0, "ECG", 1714376789050L);
        storage.addPatientData(1, 98.0, "ECG", 1714376789054L);
        storage.addPatientData(1, 80.0, "ECG", 1714376789058L);
        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789056L);
        assertEquals(2, records.size());
        assertEquals(98.0, records.get(1).getMeasurementValue());
    }

    @Test
    @DisplayName("All patient records belonging to differing patients are stored separately.")
    void separationOfPatientData() {
        storage.addPatientData(1, 100.0, "ECG", 1714376789050L);
        storage.addPatientData(2, 98.0, "ECG", 1714376789054L);

        List<PatientRecord> records1 = storage.getRecords(1, 1714376789050L, 1714376789054L);
        List<PatientRecord> records2 = storage.getRecords(2, 1714376789050L, 1714376789054L);

        assertEquals(100.0, records1.get(0).getMeasurementValue());
        assertEquals(98.0, records2.get(0).getMeasurementValue());
    }

    @Test
    @DisplayName("The getAllPatients() method returns all the different patients added.")
    void allPatientsReturned() {
        storage.addPatientData(1, 100.0, "ECG", 1714376789050L);
        storage.addPatientData(2, 98.0, "ECG", 1714376789054L);
        storage.addPatientData(3, 80.0, "ECG", 1714376789058L);
        assertEquals(3, storage.getAllPatients().size());
    }

}
