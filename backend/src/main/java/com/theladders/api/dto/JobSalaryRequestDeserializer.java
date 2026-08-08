package com.theladders.api.dto;

import com.theladders.model.SalaryPeriod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.math.BigDecimal;

public class JobSalaryRequestDeserializer extends ValueDeserializer<JobSalaryRequest> {

    @Override
    public JobSalaryRequest deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(parser);
        if (node.isNumber()) {
            BigDecimal salary = node.asDecimal();
            SalaryPeriod unit = salary.compareTo(BigDecimal.valueOf(1000)) <= 0 ? SalaryPeriod.HOURLY : SalaryPeriod.ANNUAL;
            return new JobSalaryRequest(salary, "USD", unit.getLabel());
        }
        BigDecimal value = node.hasNonNull("value") ? node.get("value").asDecimal() : null;
        String currency = node.hasNonNull("currency") ? node.get("currency").asString() : null;
        String unit = node.hasNonNull("unit") ? node.get("unit").asString() : null;
        return new JobSalaryRequest(value, currency, unit);
    }
}
