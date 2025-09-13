package de.pascalwagler.airq.controller;

import de.pascalwagler.airq.AirQ;
import de.pascalwagler.airq.metrics.AirQMeterBinder;
import de.pascalwagler.airq.model.internal.AirQDataHolder;
import de.pascalwagler.airq.model.internal.StaticSensorConfig;
import de.pascalwagler.airq.service.JsonConverter;
import io.javalin.http.Context;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsController {

    private final PrometheusMeterRegistry prometheusMeterRegistry;
    private final AirQDataHolder airQDataHolder;
    private final AirQ airQ;
    private final StaticSensorConfig staticSensorConfig;

    public void getMetrics(Context ctx) {

        airQDataHolder.setAirQConfig(JsonConverter.convertConfigFromJson(airQ.getResponseAsString(AirQ.AirQRequest.CONFIG)));
        airQDataHolder.setAirQData(JsonConverter.convertDataFromJson(airQ.getResponseAsString(AirQ.AirQRequest.DATA)));

        new AirQMeterBinder(airQDataHolder, staticSensorConfig).bindTo(prometheusMeterRegistry);

        ctx.contentType("text/plain; version=0.0.4; charset=utf-8")
                .result(prometheusMeterRegistry.scrape());
    }
}
