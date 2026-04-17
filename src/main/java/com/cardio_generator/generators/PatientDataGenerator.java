package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating patient data. Specific implementations will generate 
 * different types of data.
 */
public interface PatientDataGenerator {

    /**
     * Generates patient data and outputs it using the provided OutputStrategy.
     * @param patientId the ID of the patient for whom the data is being generated.
     * @param outputStrategy the strategy to use for outputting the generated data (e.g. console).
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
