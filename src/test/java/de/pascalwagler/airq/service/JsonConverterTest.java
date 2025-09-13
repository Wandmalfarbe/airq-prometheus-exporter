package de.pascalwagler.airq.service;

import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.SensorData;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonConverterTest {

    @Test
    void convertDataFromJsonNode_coWithWarmup() throws IOException {
        // Arrange
        String json = readFile("/with-warmup/data-with-warmup.json");

        SensorData coSensorData = SensorData.builder()
                .id("co")
                .statusMessage("co sensor still in warm up phase; waiting time = 969 s")
                .build();

        // Act
        AirQData airQData = JsonConverter.convertDataFromJson(json);

        // Assert
        assertEquals(coSensorData, airQData.getSensorData().get("co"));
    }

    private String readFile(String filePath) throws IOException {
        try (InputStream in = this.getClass().getResourceAsStream(filePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
