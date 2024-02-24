# air-Q Prometheus Exporter

This is a Prometheus exporter for the air-Q, an air quality measuring device. The air-Q data is exported as Prometheus text metrics (or JSON) and can be visualized e.g. in Grafana.

![Grafana Dashboard](img/grafana-dashboard.png)

## Usage

The command line application can be configured via parameters or environment variables.

### Parameters

| Command Line Parameter            | Environment Variable | Default Value | Description                                                                                       |
|-----------------------------------|---|---------------|---------------------------------------------------------------------------------------------------|
| `-h` or `--host "<host>"`         | `AIRQ_PROM_EXP_HOST` |               | IP address or host name of the air-Q e.g. `192.168.178.46` or `airq.local`.                       |
| `-p` or `--password "<password>"` | `AIRQ_PROM_EXP_PASSWORD` |               | Password of the air-Q                                                                             |
| `-s` or `--port "<port>"`         | `AIRQ_PROM_EXP_PORT` | 8080          | Port of the webserver. The server will serve Prometheus data at `http://localhost:<port>/metrics` |
| `-h` or `--help`                  |  |           | Shows a help message                                                                              |
| `-v` or `--version`               |  |           | Shows the version                                                                                 |

### Running

#### Standalone with Java (`java -jar`)

> [!NOTE]  
> An installation of Java > 11 is required to run this application.

Start the application from the command line by executing the following command:

```shell
java -jar airq-prometheus-exporter.jar --host "192.168.178.46" --password "myPassword123" --port "8080"
```

One can also set the environment variables and call the application without arguments.

```shell
export AIRQ_PROM_EXP_HOST="192.168.178.46"
export AIRQ_PROM_EXP_PASSWORD="myPassword123"
export AIRQ_PROM_EXP_PORT="8080"

java -jar airq-prometheus-exporter.jar
```

#### With Docker (`docker run`)

> [!IMPORTANT]  
> At the moment there is no published Docker image on Docker Hub. However, it is possible to build the image manually.

If a Docker image exists, one can run the application with the following command:

```shell
docker run -d -e export AIRQ_PROM_EXP_HOST="192.168.178.46" AIRQ_PROM_EXP_PASSWORD="myPassword123" AIRQ_PROM_EXP_PORT="8080" -p 8080:8080 --name air-prometheus-exporter air-prometheus-exporter
```

This will map the port `8080` from inside the container to the port `8080` on the outside. If you change the port with `AIRQ_PROM_EXP_PORT` or `--port` you also have to change the port mapping in this command.

#### With Docker (`docker compose`)

> [!IMPORTANT]  
> At the moment there is no published Docker image on Docker Hub. However, it is possible to build the image manually.

Create a file `docker-compose.yml` with the following content:

```yml
version: '2'
services:
  airq-prometheus-exporter:
    container_name: airq-prometheus-exporter
    image: airq-prometheus-exporter
    command: '-h "192.168.178.46" -p "myPassword123" -s "8080"'
    ports:
      - '8080:8080'
```

### HTTP Endpoints

The application will expose four HTTP endpoints that display the air-Q data or loaded configuration in different formats.

1. `http://localhost:<port>/metrics`

    Serves the air-Q data as Prometheus metrics format (`text/plain; version=0.0.4; charset=utf-8`).

    Example:

    ```
    # HELP airq_timestamp Timestamp
    # TYPE airq_timestamp gauge
    airq_timestamp{device="c1c8f61092e7c1f607359324880fcb33",} 1.707151608E12
    # HELP airq_uptime Uptime (s)
    # TYPE airq_uptime gauge
    airq_uptime{device="c1c8f61092e7c1f607359324880fcb33",} 4598.0
    # HELP airq_pressure Air Pressure (hPa)
    # TYPE airq_pressure gauge
    airq_pressure{device="c1c8f61092e7c1f607359324880fcb33",} 1006.89
    # HELP airq_co Carbon Monoxide (mg/m³)
    # TYPE airq_co gauge
    airq_co{device="c1c8f61092e7c1f607359324880fcb33",} 0.403
   ...
    ```

2. `http://localhost:<port>/data-raw`

    Serves the raw unprocessed (but decrypted) air-Q data.

    Example:
    
    ```json
    {"co2":[938.0,78.1],"humidity":[60.452,3.86],"TypPS":2.4,"humidity_abs":[9.759,0.49],"sound":[27.1,13.0],"sound_max":[74.6,1.9],"timestamp":1707147049000,"health":723,"dHdt":0.0,"dCO2dt":0.0,"pm2_5":[11,11],"Status":{"co":"co sensor still in warm up phase; waiting time = 177 s","tvoc":"tvoc sensor still in warm up phase; waiting time = 177 s","no2":"no2 sensor still in warm up phase; waiting time = 177 s"},"pressure":[1007.15,1.0],"pm1":[1,10],"uptime":41,"DeviceID":"abcdefg","temperature":[18.77,0.54],"measuretime":946,"dewpt":[11.142,0.77],"performance":633.849,"pm10":[11,11]}
    ```

3. `http://localhost:<port>/data-parsed`

   Serves the internal representation of the air-Q data.
   
   Example:
   
   ```json
   {
     "deviceId": "abcdefg",
     "sensorData": {
       "tvoc": {
         "id": "tvoc",
         "value": 0,
         "errorMargin": 0
       },
       "co2": {
         "id": "co2",
         "value": 801,
         "errorMargin": 74
       },
       "sound": {
         "id": "sound",
         "value": 32,
         "errorMargin": 9.3
       },
       "sound_max": {
         "id": "sound_max",
         "value": 39.3,
         "errorMargin": 5.6
       },
   ...
   ```

## Build the JAR

Build a runnable JAR with the following Maven command:

```shell
mvn clean package -DskipTests
```

## Build the Docker Image

Build a Docker image for the Raspberry Pi with the following command:

```shell
docker build -t airq-prometheus-exporter . --platform linux/arm64/v8
```

Build a Docker image for macOS with the following command:

```shell
docker build -t airq-prometheus-exporter . --platform linux/amd64
```

## Test

> [!NOTE]  
> This section is incomplete and consists of notes for me to deploy the application on my Raspberry Pi.

Run the Docker image locally:

```shell
docker compose up -d
```

Export the image to a *tar* file `airq-prometheus-exporter.tar`

```shell
docker save --output "airq-prometheus-exporter.tar" "airq-prometheus-exporter"
```

Copy the image to the Raspberry Pi with *scp*.

```shell
scp "airq-prometheus-exporter.tar" "username@raspberrypi:~/airq-prometheus-exporter"
```

Import image from *tar* file

```shell
docker load --input "airq-prometheus-exporter.tar"
```

## Grafana

The Grafana dashboard is available as a JSON file in the folder `grafana-dashboard`. The installation is described below:

1. Download the JSON file from folder `grafana-dashboard`.
2. Open your Grafana interface and hover over the *Dashboard* icon on the left.
3. Click the *Import* menu.
4. Upload the dashboard file by clicking on the button *Upload JSON file*

## License

This project is open source licensed under the BSD 3-Clause License. Please see the [LICENSE file](LICENSE) for more information.
