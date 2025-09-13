package de.pascalwagler.airq.controller;

import de.pascalwagler.airq.AirQ;
import io.javalin.http.ContentType;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AirQController {

    private final AirQ airQ;

    public void getData(Context ctx) {
        ctx.contentType(ContentType.APPLICATION_JSON)
                .result(airQ.getResponseAsString(AirQ.AirQRequest.DATA));
    }

    public void getConfig(Context ctx) {
        ctx.contentType(ContentType.APPLICATION_JSON)
                .result(airQ.getResponseAsString(AirQ.AirQRequest.CONFIG));
    }

    public void getStandardpass(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.STANDARDPASS));
    }

    public void getSsid(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.SSID));
    }

    public void getAverage(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.AVERAGE));
    }

    public void blink(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.BLINK));
    }

    public void getLog(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.LOG));
    }

    public void getPing(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.PING));
    }

    public void getVersion(Context ctx) {
        ctx.json(airQ.getResponseAsString(AirQ.AirQRequest.VERSION));
    }
}
