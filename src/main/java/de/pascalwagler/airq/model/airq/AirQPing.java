package de.pascalwagler.airq.model.airq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirQPing {

    @JsonProperty("RoomType")
    private String roomType;

    @JsonProperty("devicename")
    private String deviceName;

    @JsonProperty("air-Q-Hardware-Version")
    private String airQHardwareVersion;

    @JsonProperty("air-Q-Software-Version")
    private String airQSoftwareVersion;
}
