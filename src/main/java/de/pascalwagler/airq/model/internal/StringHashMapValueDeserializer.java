package de.pascalwagler.airq.model.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.HashMap;

public class StringHashMapValueDeserializer extends JsonDeserializer<HashMap<String, Sensor>> {

    @Override
    public HashMap<String, Sensor> deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {
        HashMap<String, Sensor> ret = new HashMap<>();

        ObjectCodec codec = parser.getCodec();
        TreeNode node = codec.readTree(parser);

        if (node.isArray()) {
            for (JsonNode n : (ArrayNode) node) {

                JsonNode id = n.get("id");
                JsonNode nameDe = n.get("nameDe");
                JsonNode nameEn = n.get("nameEn");
                JsonNode unit = n.get("unit");
                JsonNode type = n.get("type");
                JsonNode hasErrorMargin = n.get("hasErrorMargin");

                ret.put(id.asText(), Sensor.builder()
                        .id(id.asText())
                        .nameDe(nameDe.asText())
                        .nameEn(nameEn.asText())
                        .unit(unit != null ? unit.asText() : null)
                        .type(type != null ? type.asText() : null)
                        .hasErrorMargin(hasErrorMargin != null && hasErrorMargin.asBoolean())
                        .build());
            }
        }
        return ret;
    }
}
