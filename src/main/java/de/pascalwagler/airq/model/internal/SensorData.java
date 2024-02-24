package de.pascalwagler.airq.model.internal;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Builder
@Data
public class SensorData {

    /**
     * The sensor id e.g. "no2", "sound_max", "uptime" or "TypPS".
     */
    @NotNull
    private String id;

    /**
     * The measured sensor value. This can be null if the sensor is in warmup period.
     * The field statusMessage will be filled instead.
     */
    @Nullable
    private Double value;

    /**
     * The error margin of that current sensor reading. This can be null because some "sensors" don't provide
     * an error margin e.g. "uptime", "timestamp" or "measuretime":
     */
    @Nullable
    private Double errorMargin;

    /**
     * If the sensor is in the warmup period, this message fill be filled with more information
     * regarding the sensor status.
     */
    @Nullable
    private String statusMessage;
}
