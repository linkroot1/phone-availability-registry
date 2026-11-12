package org.example.phoneavailabilityregistry;

import org.example.phoneavailabilityregistry.model.Provider;
import org.example.phoneavailabilityregistry.registry.InMemoryProviderRegistry;
import org.example.phoneavailabilityregistry.registry.JsonFileProviderLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonFileProviderLoaderTest {

    @TempDir
    Path tempDir;

    private final InMemoryProviderRegistry registry = new InMemoryProviderRegistry();
    private final JsonFileProviderLoader loader =
            new JsonFileProviderLoader(registry, new ObjectMapper());

    private Path writeJson(String content) throws IOException {
        Path file = tempDir.resolve("providers.json");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void loadsProvidersFromJsonFile() throws IOException {
        Path file = writeJson("""
                [
                  {"id":"p1","baseUrl":"https://one.example.com","apiKey":"k1"},
                  {"id":"p2","baseUrl":"https://two.example.com","apiKey":"k2"}
                ]
                """);

        loader.load(file);

        assertEquals(new Provider("p1", "https://one.example.com", "k1"),
                registry.findById("p1").orElseThrow());
        assertEquals(new Provider("p2", "https://two.example.com", "k2"),
                registry.findById("p2").orElseThrow());
    }

    @Test
    void loadReplacesPreviousSet() throws IOException {
        registry.replaceAll(List.of(new Provider("old", "https://old.example.com", "k")));

        Path file = writeJson("""
                [ {"id":"new","baseUrl":"https://new.example.com","apiKey":"k2"} ]
                """);

        loader.load(file);

        assertTrue(registry.findById("old").isEmpty());
        assertTrue(registry.findById("new").isPresent());
    }

    @Test
    void throwsWhenFileDoesNotExist() {
        Path missing = tempDir.resolve("missing.json");

        IOException ex = assertThrows(IOException.class, () -> loader.load(missing));

        assertTrue(ex.getMessage().contains("missing.json"));
    }

    @Test
    void throwsOnMalformedJson() throws IOException {
        Path file = writeJson("{ this is not valid json ]");

        assertThrows(IOException.class, () -> loader.load(file));
    }

    @Test
    void keepsOldStateWhenFileIsInvalid() {
        registry.replaceAll(List.of(new Provider("keep", "https://keep.example.com", "k")));

        Path file = tempDir.resolve("broken.json");
        assertDoesNotThrow(() -> Files.writeString(file, "[ { "));

        assertThrows(IOException.class, () -> loader.load(file));

        assertTrue(registry.findById("keep").isPresent());
    }
}
