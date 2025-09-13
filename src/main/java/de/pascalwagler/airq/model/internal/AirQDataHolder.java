package de.pascalwagler.airq.model.internal;

import de.pascalwagler.airq.model.airq.AirQConfig;
import de.pascalwagler.airq.model.airq.AirQPing;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirQDataHolder {
    private AirQData airQData;
    private AirQConfig airQConfig;
    private AirQPing airQPing;
}
