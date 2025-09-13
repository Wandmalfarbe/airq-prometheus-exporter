package de.pascalwagler.airq;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Getter
@Slf4j
@Command(name = "airq-prometheus-exporter",
        version = "2.0.0",
        description = "Starts a webserver that displays air-Q data as prometheus text.")
public class ProgramArguments {

    @Option(names = {"--help"}, usageHelp = true, hidden = true)
    boolean helpRequested = false;

    @Option(names = {"-v", "--version" }, versionHelp = true, hidden = true)
    boolean versionRequested = false;

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    @Option(
            names = {"-h", "--airq-host"},
            required = true,
            defaultValue = "${env:AIRQ_PROM_EXP_AIRQ_HOST}",
            description = "IP address or host name of the air-Q e.g. `192.168.178.46` or `31b31_air-q.local`.")
    private String airQHost = "";

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    @Option(
            names = {"-p", "--airq-password"},
            required = true,
            defaultValue = "${env:AIRQ_PROM_EXP_AIRQ_PASSWORD}",
            description = "Password of the air-Q")
    private String password = "";

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    @Option(
            names = {"-s", "--port"},
            defaultValue = "${env:AIRQ_PROM_EXP_SERVER:-8080}",
            description = "Port of the webserver. The server will serve Prometheus data at `http://localhost:<port>/metrics`")
    private int serverPort = 8080;

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal"})
    @Option(
            names = {"-o", "--export-other-metrics"},
            defaultValue = "${env:AIRQ_PROM_EXP_EXPORT_OTHER_METRICS:-false}",
            description = "Export additional (non-air-Q) metrics e.g., metrics about the prometheus exporter itself.")
    private boolean exportOtherMetrics = false;
}
