package de.pascalwagler.airq.metrics;

import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.AirQDataHolder;
import de.pascalwagler.airq.model.internal.Config;
import de.pascalwagler.airq.model.internal.Sensor;
import de.pascalwagler.airq.model.internal.SensorData;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Slf4j
public class AirQMetrics implements MeterBinder {

    private static final String PREFIX = "airq";
    private final AirQDataHolder airQDataHolder;
    private final Config config;

    Sensor unknownSensor = Sensor.builder()
            .nameDe("Unbekannt")
            .nameEn("Unknown")
            .unit("N/A")
            .build();

    public AirQMetrics(AirQDataHolder airQDataHolder, Config config) {
        this.airQDataHolder = airQDataHolder;
        this.config = config;
    }

    @Override
    public void bindTo(@NotNull MeterRegistry registry) {

        AirQData airQData = airQDataHolder.getAirQData();
        Map<String, SensorData> data = airQData.getSensorData();

        for (var sensorData : data.values()) {
            Sensor sensor = config.getSensors()
                    .getOrDefault(sensorData.getId(), unknownSensor);

            if (sensor != null) {

                String description;
                if (sensor.getUnit() == null) {
                    description = sensor.getNameEn();
                } else {
                    description = sensor.getNameEn() + " (" + sensor.getUnit() + ")";
                }
                Gauge.builder(PREFIX + "." + sensorData.getId(), airQDataHolder, a -> {
                            SensorData liveSensorData = a.getAirQData().getSensorData().get(sensorData.getId());
                            if (liveSensorData.getStatusMessage() != null) {
                                // Sensor is in warmup period and will be available shortly
                                return Double.NaN;
                            } else {
                                return liveSensorData.getValue();
                            }
                        })
                        .description(description)
                        .tag("device", airQData.getDeviceId())
                        .register(registry);
            } else {
                log.error("Configuration missing for key '" + sensorData.getId() + "'.");
            }
        }
    }
}
