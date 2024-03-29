package de.pascalwagler.airq.model.internal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class Config {

    @JsonDeserialize(using = StringHashMapValueDeserializer.class)
    Map<String, Sensor> sensors;
}
