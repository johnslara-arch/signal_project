import com.cardio_generator.outputs.WebSocketClient;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebSocketClient")
public class WebSocketClientTests {

    private DataStorage storage;
    private WebSocketClient client;

    @BeforeEach
    void setUp() {
        DataStorage.resetInstance();
        storage = DataStorage.getInstance();
        client = new WebSocketClient(URI.create("ws://localhost:8080"), storage);
    }

    /**
     * Used to call the private method {@code parseLine} without the need of a
     * connecting to a live server.
     * 
     * @param line the message received that needs to be parsed into 4 fields.
     * @throws Exception when a malformatted message is received.
     */
    private void parseLine(String line) throws Exception {
        Method method = WebSocketClient.class.getDeclaredMethod("parseLine", String.class, DataStorage.class);
        method.setAccessible(true);
        method.invoke(client, line, storage);
    }

    /**
     * Used to call the private method {@code parseDataValue} without the need of a
     * connecting to a live server.
     * 
     * @param data the String of the measurementValue that needs to be converted.
     * @return the converted measurementValue as a double.
     * @throws Exception if an unexpected argument is used.
     */
    private double parseDataValue(String data) throws Exception {
        Method method = WebSocketClient.class.getDeclaredMethod("parseDataValue", String.class);
        method.setAccessible(true);
        return (double) method.invoke(client, data);
    }

    // Testing for correct data storage!

    @Test
    @DisplayName("Parses a correctly formatted message and stores it in DataStorage.")
    void parsesAndStoresStandardMessage() throws Exception {
        parseLine("1, 1000, ECG, 72.0"); // Check that spaces don't cause any issues while parsing.
        parseLine("1,3000,Saturation,92%"); // Also testing the fact that the % on the saturation value gets stripped
                                            // correctly.

        List<PatientRecord> records = storage.getRecords(1, 0L, 4000L);
        assertEquals(2, records.size()); // Tests that it parses multiple messages correctly.
        assertEquals(1, records.get(0).getPatientId());
        assertEquals(1000L, records.get(0).getTimestamp());
        assertEquals("Saturation", records.get(1).getRecordType());
        assertEquals(72.0, records.get(0).getMeasurementValue());
        assertEquals(92.0, records.get(1).getMeasurementValue());
    }

    @Test
    @DisplayName("Parses data for multiple patients and stores it independently.")
    void parseDifferentPatientDataIndependently() throws Exception {
        parseLine("1, 1000, ECG, 72.0");
        parseLine("2,3000,Saturation,92%");

        assertEquals(1, storage.getRecords(1, 0L, 4000L).size());
        assertEquals(1, storage.getRecords(2, 0L, 4000L).size());
    }

    // Testing for error handling!

    @Test
    @DisplayName("IllegalArgumentException thrown when there are not 4 fields in a message received.")
    void mismatchFieldsException() {
        assertThrows(Exception.class, () -> parseLine("1,1000,ECG")); // Tests for a message with too few fields
        assertThrows(Exception.class, () -> parseLine("1,1000,ECG,82.0, 85.0")); // Tests for a message with too many
                                                                                 // fields
    }

    @Test
    @DisplayName("NumberFormatException thrown when data in a message received is in the wrong format.")
    void incorrectlyFormattedData() {
        assertThrows(Exception.class, () -> parseLine("AB,1000,ECG,88.0")); // Tests when patientID not a number
        assertThrows(Exception.class, () -> parseLine("1,Hello,ECG,88.0")); // Tests when timestamp is not a number
        assertThrows(Exception.class, () -> parseLine("1,1000,ECG,Concerning")); // Tests when measurement value is not
                                                                                 // a number
    }

    @Test
    @DisplayName("onMessage method continues to process data after a badly formatted message.")
    void onMessageContinues() {
        assertDoesNotThrow(() -> client.onMessage("AB,1000,ECG,88.0")); // Checks the badly formatted message does not
                                                                        // interrupt the program
        client.onMessage("AB,1000,ECG,88.0");
        client.onMessage("1,1000,ECG,88.0");
        client.onMessage("1,2000,ECG,88.0");

        List<PatientRecord> records = storage.getRecords(1, 0L, 3000L);
        assertEquals(2, records.size()); // Checks only correctly formatted messaged get stored
    }

    // Testing for parseData method!

    @Test
    @DisplayName("Check all types of measurment values still get parsed correctly.")
    void correctParseDataMethod() throws Exception {
        assertEquals(75.0, parseDataValue("75.0")); // Tests for normal numbers
        assertEquals(95.0, parseDataValue("95.0%")); // Tests for saturation values with %
        assertEquals(1.0, parseDataValue("triggered")); // Tests for correct mapping from triggered to 1
        assertEquals(0.0, parseDataValue("RESOLVED")); // Tests for correct mapping from resolved to 0 and case
                                                       // insensivity
        assertEquals(75.0, parseDataValue("75")); // Tests for lack of decimal point conversion
    }

    // Integration testing - WARNING this requires a live server.
    // Tested by starting the signal generator with websocket:2121.

    @Test
    @DisplayName("Checks connection to a live server and correct data reception.")
    @Disabled("Only run manually once the server on ws://localhost:8080 has been started.")
    void integrationWithServerTest() throws Exception {
        com.data_management.DataReaderWebSocket reader = new com.data_management.DataReaderWebSocket(
                URI.create("ws://localhost:2121"));
        reader.readData(storage);

        Thread.sleep(5000L); // Leave the connection open for a short time to receive some data
        reader.stopConnectionToServer();

        assertFalse(storage.getAllPatients().isEmpty()); // Some records should be stored after 3 seconds of
                                                         // connection to the server
    }
}
