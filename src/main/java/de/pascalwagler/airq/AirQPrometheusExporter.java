package de.pascalwagler.airq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import de.pascalwagler.airq.controller.AirQController;
import de.pascalwagler.airq.controller.MetricsController;
import de.pascalwagler.airq.controller.WebInterfaceController;
import de.pascalwagler.airq.model.internal.StaticSensorConfig;
import de.pascalwagler.airq.model.internal.AirQDataHolder;
import de.pascalwagler.airq.model.internal.ErrorInformation;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;
import io.javalin.micrometer.MicrometerPlugin;
import io.javalin.rendering.template.JavalinMustache;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.rendering.template.TemplateUtil.model;

@Slf4j
public class AirQPrometheusExporter implements Callable<Void> {

    private final ProgramArguments programArguments;
    private AirQ airQ;
    private final AirQDataHolder airQDataHolder = new AirQDataHolder();
    private PrometheusMeterRegistry prometheusMeterRegistry;
    private StaticSensorConfig staticSensorConfig;

    @SuppressWarnings("squid:S106")
    public static void main(String[] args) {
        ProgramArguments programArguments = new ProgramArguments();
        try {
            CommandLine cmd = new CommandLine(programArguments);
            CommandLine.ParseResult result = cmd.parseArgs(args);
            if (result.isUsageHelpRequested()) {
                cmd.usage(System.out, CommandLine.Help.Ansi.AUTO);
            } else if (result.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
            } else {
                AirQPrometheusExporter airQPrometheusExporter = new AirQPrometheusExporter(programArguments);
                airQPrometheusExporter.call();
            }
        } catch (CommandLine.ParameterException ex) {
            System.err.println(ex.getMessage());
            ex.getCommandLine().usage(System.out, CommandLine.Help.Ansi.AUTO);
        }
    }

    public AirQPrometheusExporter(ProgramArguments programArguments) {
        this.programArguments = programArguments;
    }

    public Void call() {
        try {
            airQ = new AirQ(programArguments.getAirQHost(), programArguments.getPassword());

            readStaticSensorInformation();
            setupMicrometer();
            startJavalinServer();
        } catch (Exception e) {
            log.error("An unexpected exception occurred.", e);
        }
        return null;
    }

    private void readStaticSensorInformation() throws IOException {
        URL configUrl = this.getClass().getResource("/config.json");
        assert configUrl != null;
        staticSensorConfig = new ObjectMapper().readValue(configUrl.openStream(), StaticSensorConfig.class);
    }

    private void setupMicrometer() {
        prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        if (programArguments.isExportOtherMetrics()) {
            // Bind other JVM metrics for general information
            new ClassLoaderMetrics().bindTo(prometheusMeterRegistry);
            new JvmMemoryMetrics().bindTo(prometheusMeterRegistry);
            new JvmGcMetrics().bindTo(prometheusMeterRegistry);
            new JvmThreadMetrics().bindTo(prometheusMeterRegistry);
            new UptimeMetrics().bindTo(prometheusMeterRegistry);
            new ProcessorMetrics().bindTo(prometheusMeterRegistry);
        }
    }

    private void startJavalinServer() {

        // For iterating Maps in Mustache templates
        DefaultMustacheFactory defaultMustacheFactory = new DefaultMustacheFactory("./templates");
        defaultMustacheFactory.setObjectHandler(new ReflectionObjectHandler() {
            @Override
            protected boolean areMethodsAccessible(Map<?, ?> map) {
                return true;
            }
        });

        // Manual dependency injection because a DI framework would be overkill
        MetricsController metricsController = new MetricsController(prometheusMeterRegistry, airQDataHolder, airQ, staticSensorConfig);
        AirQController airQController = new AirQController(airQ);
        WebInterfaceController webInterfaceController = new WebInterfaceController(airQ, staticSensorConfig, programArguments);

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.fileRenderer(new JavalinMustache(defaultMustacheFactory));
            config.staticFiles.add("/static");
            if (programArguments.isExportOtherMetrics()) {
                config.registerPlugin(new MicrometerPlugin(micrometerConfig -> micrometerConfig.registry = prometheusMeterRegistry));
            }

            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> mapper
                    .configOverride(Map.class)
                    .setInclude(JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL))
            ));

            config.router.apiBuilder(() -> {
                get("/metrics", metricsController::getMetrics);

                get("/airq/data", airQController::getData);
                get("/airq/config", airQController::getConfig);
                get("/airq/standardpass", airQController::getStandardpass);
                get("/airq/ssid", airQController::getSsid);
                get("/airq/average", airQController::getAverage);
                get("/airq/blink", airQController::blink);
                get("/airq/log", airQController::getLog);
                get("/airq/ping", airQController::getPing);
                get("/airq/version", airQController::getVersion);

                get("/", webInterfaceController::redirectToWebInterface);
                get("/web", webInterfaceController::redirectToWebInterface);
                get("/web/data", webInterfaceController::serveData);
                get("/web/config", webInterfaceController::serveConfig);
                get("/web/logs", webInterfaceController::serveLogs);
            });
        }).start(programArguments.getServerPort());

        app.exception(Exception.class, (exception, ctx) -> {
            // Prevent infinite recursion
            try {
                log.error("Uncaught exception", exception);

                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                String stackTraceString = sw.toString();
                final DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                ErrorInformation errorInformation = ErrorInformation.builder()
                        .url(ctx.url())
                        .time(iso8601DateFormat.format(new Date()))
                        .clazz(exception.getClass().getCanonicalName())
                        .message(exception.getMessage())
                        .causeClazz(exception.getCause() != null ? exception.getCause().getClass().getCanonicalName() : null)
                        .causeMessage(exception.getCause() != null ? exception.getCause().getMessage() : null)
                        .stackTrace(stackTraceString)
                        .build();

                ctx.render("/error.html", model("errorInformation", errorInformation))
                        .status(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                log.error("An Exception occurred while showing the error page.", ex);
                ctx.html("An Exception occurred while showing the error page. Please see the log file for more information.");
            }
        });
    }
}
