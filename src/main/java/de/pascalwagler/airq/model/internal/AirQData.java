package de.pascalwagler.airq.model.internal;

import lombok.Builder;

import java.util.HashMap;
import java.util.Map;

@lombok.Data
@Builder
public class AirQData {

    private String deviceId;

    @Builder.Default
    private Map<String, SensorData> sensorData = new HashMap<>();
}
