package de.pascalwagler.airq.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pascalwagler.airq.JsonConverter;
import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonConverterTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Test
    void convertDataFromJsonNode_coWithWarmup() throws IOException {
        // Arrange
        URL url = this.getClass().getResource("/with-warmup/data-with-warmup.json");
        JsonNode jsonNode = objectMapper.readTree(url);

        SensorData coSensorData = SensorData.builder()
                .id("co")
                .statusMessage("co sensor still in warm up phase; waiting time = 969 s")
                .build();

        // Act
        AirQData airQData = JsonConverter.convertDataFromJsonNode(jsonNode);

        // Assert
        assertEquals(coSensorData, airQData.getSensorData().get("co"));
    }
}
