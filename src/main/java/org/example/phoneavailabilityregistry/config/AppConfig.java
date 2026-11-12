package org.example.phoneavailabilityregistry.config;

import org.example.phoneavailabilityregistry.client.HttpPhoneAvailabilityClient;
import org.example.phoneavailabilityregistry.client.PhoneAvailabilityClient;
import org.example.phoneavailabilityregistry.registry.InMemoryProviderRegistry;
import org.example.phoneavailabilityregistry.registry.JsonFileProviderLoader;
import org.example.phoneavailabilityregistry.registry.ProviderRegistry;
import org.example.phoneavailabilityregistry.service.PhoneAvailabilityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public ProviderRegistry providerRegistry() {
        return new InMemoryProviderRegistry();
    }

    @Bean
    public PhoneAvailabilityClient phoneAvailabilityClient(HttpClient httpClient,
                                                           ObjectMapper objectMapper) {
        return new HttpPhoneAvailabilityClient(httpClient, objectMapper);
    }

    @Bean
    public PhoneAvailabilityService phoneAvailabilityService(ProviderRegistry registry,
                                                             PhoneAvailabilityClient client) {
        return new PhoneAvailabilityService(registry, client);
    }

    @Bean
    public JsonFileProviderLoader jsonFileProviderLoader(ProviderRegistry registry,
                                                         ObjectMapper objectMapper,
                                                         Environment environment) {
        JsonFileProviderLoader loader = new JsonFileProviderLoader(registry, objectMapper);

        String file = environment.getProperty("providers.file");
        if (file != null && !file.isBlank()) {
            try {
                loader.load(Path.of(file));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load providers from " + file, e);
            }
        }

        return loader;
    }
}
