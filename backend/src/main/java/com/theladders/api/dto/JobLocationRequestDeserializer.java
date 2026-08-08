package com.theladders.api.dto;

import com.theladders.Util;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class JobLocationRequestDeserializer extends ValueDeserializer<JobLocationRequest> {

    @Override
    public JobLocationRequest deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(parser);
        if (node.isString()) {
            return fromString(node.asString());
        }
        String city = node.hasNonNull("city") ? Util.nullIfEmpty(node.get("city").asString()) : null;
        String state = node.hasNonNull("state") ? Util.nullIfEmpty(node.get("state").asString()) : null;
        String country = node.hasNonNull("country") ?  Util.nullIfEmpty(node.get("country").asString()) : null;
        return new JobLocationRequest(city, state, country);
    }

    private JobLocationRequest fromString(String value) {
        String[] tokens = value.split(",", -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = Util.nullIfEmpty(tokens[i].trim());
        }
        return switch (tokens.length) {
            case 1 -> new JobLocationRequest(null, null, tokens[0]);
            case 2 -> new JobLocationRequest(null, tokens[0], tokens[1]);
            default -> new JobLocationRequest(tokens[0], tokens[1], tokens[2]);
        };
    }
}
