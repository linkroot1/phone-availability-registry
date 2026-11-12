package org.example.phoneavailabilityregistry.client;

import org.example.phoneavailabilityregistry.model.PhoneAvailability;
import org.example.phoneavailabilityregistry.model.Provider;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpPhoneAvailabilityClient implements PhoneAvailabilityClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpPhoneAvailabilityClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PhoneAvailability check(Provider provider, String phone) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("phone must not be blank");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.baseUrl() + "/subscribers/" + phone))
                .header("Authorization", "Bearer " + provider.apiKey())
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ProviderUnavailableException(
                    "Network error calling provider " + provider.id(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderUnavailableException(
                    "Interrupted calling provider " + provider.id(), e);
        }

        return switch (response.statusCode()) {
            case 200 -> parseAvailable(response.body(), provider);
            case 404 -> PhoneAvailability.UNAVAILABLE;
            default -> throw new ProviderErrorException(
                    "Provider " + provider.id() + " returned " + response.statusCode());
        };
    }

    private PhoneAvailability parseAvailable(String body, Provider provider) {
        JsonNode node;
        try {
            node = objectMapper.readTree(body);
        } catch (RuntimeException e) {
            throw new ProviderErrorException(
                    "Cannot parse response from provider " + provider.id(), e);
        }

        if (node != null && node.has("status") && "ok".equals(node.get("status").asText())) {
            return PhoneAvailability.AVAILABLE;
        }
        throw new ProviderErrorException(
                "Unexpected 200 body from provider " + provider.id() + ": " + body);
    }
}
