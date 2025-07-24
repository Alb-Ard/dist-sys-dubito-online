package org.albard.dubito.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

public final class DefaultJsonMapping {
    private DefaultJsonMapping() {
    }

    public static void applyDefaultMapping(final ObjectMapper mapper) {
        final PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class)
                .build();
        mapper.activateDefaultTypingAsProperty(ptv, DefaultTyping.NON_FINAL_AND_ENUMS, "@class");
    }
}
