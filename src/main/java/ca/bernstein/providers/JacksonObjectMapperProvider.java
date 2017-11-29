package ca.bernstein.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public JacksonObjectMapperProvider() {
        this.objectMapper = new ObjectMapper();

        SimpleModule jacksonModule = new SimpleModule();

        objectMapper.registerModule(new SimpleModule());
    }

    @Override
    public ObjectMapper getContext(Class<?> aClass) {
        return this.objectMapper;
    }
}
