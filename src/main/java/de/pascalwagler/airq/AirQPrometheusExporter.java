package de.pascalwagler.airq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.pascalwagler.airq.metrics.AirQMetrics;
import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.AirQDataHolder;
import de.pascalwagler.airq.model.internal.Config;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URL;
import java.util.concurrent.Callable;

@Slf4j
@Command(name = "airq-prometheus-exporter",
        version = "1.0",
        mixinStandardHelpOptions = true,
        description = "Starts a webserver that displays air-Q data as prometheus text.")
public class AirQPrometheusExporter implements Callable<Integer> {

    @Option(
            names = {"-h", "--host"},
            required = true,
            defaultValue = "${env:AIRQ_PROM_EXP_HOST}",
            description = "IP address or host name of the air-Q e.g. `192.168.178.46` or `airq.local`.")
    private String airQHost = "";

    @Option(
            names = {"-p", "--password"},
            required = true,
            defaultValue = "${env:AIRQ_PROM_EXP_PASSWORD}",
            description = "Password of the air-Q")
    private String password = "";

    @Option(
            names = {"-s", "--port"},
            defaultValue = "${env:AIRQ_PROM_EXP_SERVER:-8080}",
            description = "Port of the webserver. The server will serve Prometheus data at `http://localhost:<port>/metrics`")
    private int serverPort = 8080;

    private AirQ airQ;
    private final AirQDataHolder airQDataHolder = new AirQDataHolder();
    private PrometheusMeterRegistry meterRegistry;
    private ObjectMapper objectMapper;
    private Config config;

    public static void main(String[] args) {
        new CommandLine(new AirQPrometheusExporter()).execute(args);
    }

    @Override
    public Integer call() {

        try {
            airQ = new AirQ(airQHost, password);

            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            URL configUrl = this.getClass().getResource("/config.json");
            config = objectMapper.readValue(configUrl, Config.class);

            setupMicrometer();
            startJavalinServer();
        } catch (Exception e) {
            log.error("An unexpected exception occurred.", e);
        }
        return 1;
    }

    private void setupMicrometer() {
        meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Get data once initially to create the needed Gauges.
        airQDataHolder.setAirQData(JsonConverter.convertDataFromJsonNode(airQ.getDataAsJsonNode()));
        new AirQMetrics(airQDataHolder, config).bindTo(meterRegistry);
    }

    private void startJavalinServer() {
        Javalin app = Javalin.create(javalinConfig -> javalinConfig.showJavalinBanner = false).start(serverPort);

        app.get("/metrics", ctx -> {
            airQDataHolder.setAirQData(JsonConverter.convertDataFromJsonNode(airQ.getDataAsJsonNode()));

            ctx.contentType(TextFormat.CONTENT_TYPE_004)
                    .result(meterRegistry.scrape());
        });

        app.get("/data-raw", ctx -> {
            throw new IllegalArgumentException("blahh");
        });

        app.get("/data-parsed", ctx -> {
            AirQData airQData = JsonConverter.convertDataFromJsonNode(airQ.getDataAsJsonNode());

            ctx.contentType(ContentType.APPLICATION_JSON)
                    .result(objectMapper.writeValueAsString(airQData));
        });

        app.get("/config", ctx -> ctx.contentType(ContentType.APPLICATION_JSON)
                .result(objectMapper.writeValueAsString(config)));
    }
}
