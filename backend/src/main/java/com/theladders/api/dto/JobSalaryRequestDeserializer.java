package com.theladders.api.dto;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class JobSalaryRequestDeserializer extends ValueDeserializer<JobSalaryRequest> {

    @Override
    public JobSalaryRequest deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(parser);
        if (node.isNumber()) {
            return new JobSalaryRequest(node.asLong(), "USD", "Annual");
        }
        Long value = node.hasNonNull("value") ? node.get("value").asLong() : null;
        String currency = node.hasNonNull("currency") ? node.get("currency").asString() : null;
        String unit = node.hasNonNull("unit") ? node.get("unit").asString() : null;
        return new JobSalaryRequest(value, currency, unit);
    }
}
