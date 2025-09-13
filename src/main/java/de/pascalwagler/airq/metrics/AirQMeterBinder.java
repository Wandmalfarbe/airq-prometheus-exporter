package de.pascalwagler.airq.metrics;

import de.pascalwagler.airq.model.airq.AirQConfig;
import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.AirQDataHolder;
import de.pascalwagler.airq.model.internal.Sensor;
import de.pascalwagler.airq.model.internal.SensorData;
import de.pascalwagler.airq.model.internal.StaticSensorConfig;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Slf4j
public class AirQMeterBinder implements MeterBinder {

    private static final String PREFIX = "airq";
    private final AirQDataHolder airQDataHolder;
    private final StaticSensorConfig staticSensorConfig;

    private final Sensor unknownSensor = Sensor.builder()
            .nameDe("Unbekannt")
            .nameEn("Unknown")
            .unit("N/A")
            .build();

    public AirQMeterBinder(AirQDataHolder airQDataHolder, StaticSensorConfig staticSensorConfig) {
        this.airQDataHolder = airQDataHolder;
        this.staticSensorConfig = staticSensorConfig;
    }

    /**
     * Removes all previous bindings (for air-Q Metrics only) and rebinds them.
     */
    @Override
    public void bindTo(@NotNull MeterRegistry registry) {

        AirQData airQData = airQDataHolder.getAirQData();
        AirQConfig airQConfig = airQDataHolder.getAirQConfig();
        Map<String, SensorData> data = airQData.getSensorData();

        // Remove all previously registered meters before adding them again.
        // This is done to support changing tags because the meters will be
        // registered with (possibly new tags) afterward.
        // See also https://github.com/micrometer-metrics/micrometer/issues/2223
        //
        // This is against how micrometer was designed, but it works for now.
        registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith(PREFIX))
                .forEach(registry::remove);

        for (var sensorData : data.values()) {
            Sensor sensor = staticSensorConfig.getSensors()
                    .getOrDefault(sensorData.getId(), unknownSensor);

            if (sensor == unknownSensor) {
                log.warn("Configuration missing for key '{}'.", sensorData.getId());
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
                    .description(sensor.getDescription())
                    .tag("device", airQData.getDeviceId())
                    .tag("serialNumber", airQConfig.getSerialNumber())
                    .tag("type", airQConfig.getType())
                    .tag("deviceName", airQConfig.getDeviceName())
                    .tag("roomType", airQConfig.getRoomType())
                    .tag("airQHardwareVersion", airQConfig.getAirQHardwareVersion())
                    .tag("airQSoftwareVersion", airQConfig.getAirQSoftwareVersion())
                    .register(registry);
        }
    }
}
