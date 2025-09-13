package de.pascalwagler.airq.model.internal;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AirQData {

    private String deviceId;

    @Builder.Default
    private Map<String, SensorData> sensorData = new HashMap<>();
}
