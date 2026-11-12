package org.example.phoneavailabilityregistry.registry;

import org.example.phoneavailabilityregistry.model.Provider;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JsonFileProviderLoader {

    private final ProviderRegistry registry;
    private final ObjectMapper objectMapper;

    public JsonFileProviderLoader(ProviderRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    public void load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (!Files.exists(path)) {
            throw new IOException("Providers file not found: " + path);
        }

        List<Provider> providers;
        try {
            providers = objectMapper.readValue(
                    Files.readAllBytes(path),
                    new TypeReference<List<Provider>>() {}
            );
        } catch (RuntimeException e) {
            throw new IOException("Cannot parse providers file: " + path, e);
        }

        registry.replaceAll(providers);
    }
}
