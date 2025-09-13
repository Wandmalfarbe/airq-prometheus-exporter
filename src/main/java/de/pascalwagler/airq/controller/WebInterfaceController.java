package de.pascalwagler.airq.controller;

import de.pascalwagler.airq.AirQ;
import de.pascalwagler.airq.ProgramArguments;
import de.pascalwagler.airq.model.airq.AirQConfig;
import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.StaticSensorConfig;
import de.pascalwagler.airq.service.JsonConverter;
import de.pascalwagler.airq.service.LogService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import static io.javalin.rendering.template.TemplateUtil.model;

@RequiredArgsConstructor
public class WebInterfaceController {

    private final AirQ airQ;
    private final StaticSensorConfig staticSensorConfig;
    private final ProgramArguments programArguments;

    public void redirectToWebInterface(Context ctx) {
        ctx.redirect("/web/data");
    }

    public void serveData(Context ctx) {
        AirQConfig airQConfig = JsonConverter.convertConfigFromJson(airQ.getResponseAsString(AirQ.AirQRequest.CONFIG));
        AirQData airQData = JsonConverter.convertDataFromJson(airQ.getResponseAsString(AirQ.AirQRequest.DATA));
        ctx.render("/data.html", model("airQConfig", airQConfig, "airQData", airQData));
    }

    public void serveConfig(Context ctx) {
        ctx.render("/config.html", model("programArguments", programArguments, "staticSensorConfig", staticSensorConfig));
    }

    public void serveLogs(Context ctx) {
        String[] airQLogs = JsonConverter.convertLogFromJson(airQ.getResponseAsString(AirQ.AirQRequest.LOG));
        ctx.render("/logs.html", model("logs", LogService.getLogs(), "airQLogs", LogService.mapAirQLogs(airQLogs)));
    }
}
