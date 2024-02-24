package de.pascalwagler.airq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import de.pascalwagler.airq.model.internal.AirQData;
import de.pascalwagler.airq.model.internal.SensorData;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class JsonConverter {

    private JsonConverter() {
    }

    public static AirQData convertDataFromJsonNode(JsonNode jsonNode) {

        AirQData airQData = AirQData.builder().build();
        Iterator<Map.Entry<String, JsonNode>> elements = jsonNode.fields();

        while (elements.hasNext()) {
            Map.Entry<String, JsonNode> data = elements.next();
            String key = data.getKey();
            JsonNode node = data.getValue();
            SensorData sensorData = SensorData.builder()
                    .id(key)
                    .build();

            if (node instanceof ArrayNode) {
                parseArray((ArrayNode) node, sensorData, airQData);
            } else if (node instanceof NumericNode) {
                parseNumeric((NumericNode) node, sensorData, airQData);
            } else if (node instanceof TextNode) {
                parseText(node, key, airQData);
            } else if (node instanceof ObjectNode) {
                parseObject((ObjectNode) node, airQData);
            } else {
                log.warn("Unknown measurement '" + key + "' with value '" + node + "' in air-Q data response.");
            }
        }

        return airQData;
    }

    /**
     * Status when some sensors are still warming up.
     */
    private static void parseObject(ObjectNode node, AirQData airQData) {
        Iterator<String> sensorNames = node.fieldNames();
        sensorNames.forEachRemaining(sensorId -> {

            SensorData existingSensorData = airQData.getSensorData().get(sensorId);
            String statusMessage = node.findValue(sensorId).textValue();
            if (existingSensorData != null) {
                existingSensorData.setStatusMessage(statusMessage);
            } else {
                airQData.getSensorData().put(sensorId, SensorData.builder()
                        .id(sensorId)
                        .statusMessage(statusMessage)
                        .build());
            }
        });
    }

    private static void parseText(JsonNode node, String key, AirQData airQData) {
        String textValue = node.textValue();
        switch (key) {
            case "DeviceID":
                airQData.setDeviceId(textValue);
                break;
            case "Status":
                // If 'Status' has the text value 'OK', no sensors are in the warmup phase.
                // Otherwise, the value for 'Status' will be an Object.
                break;
            default:
                log.warn("Unknown measurement '" + key + "' with value '" + node + "' in air-Q data response.");
        }
    }

    /**
     * Normal measurement (Double, Long, Int). When values are returned without decimals, Jackson
     * converts them to int or long.
     */
    private static void parseNumeric(NumericNode node, SensorData sensorData, AirQData airQData) {
        sensorData.setValue(node.doubleValue());
        airQData.getSensorData().put(sensorData.getId(), sensorData);
    }

    /**
     * Array with two values. The first is the measured value and the second is the error margin.
     */
    private static void parseArray(ArrayNode node, SensorData sensorData, AirQData airQData) {
        Iterator<JsonNode> iterator = node.elements();
        JsonNode value = iterator.next();
        JsonNode errorMargin = iterator.next();

        sensorData.setValue(value.doubleValue());
        sensorData.setErrorMargin(errorMargin.doubleValue());

        airQData.getSensorData().put(sensorData.getId(), sensorData);
    }
}
