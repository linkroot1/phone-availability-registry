package org.example.phoneavailabilityregistry;

import org.example.phoneavailabilityregistry.client.HttpPhoneAvailabilityClient;
import org.example.phoneavailabilityregistry.client.ProviderErrorException;
import org.example.phoneavailabilityregistry.client.ProviderUnavailableException;
import org.example.phoneavailabilityregistry.model.PhoneAvailability;
import org.example.phoneavailabilityregistry.model.Provider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpPhoneAvailabilityClientTest {

    private static final Provider PROVIDER =
            new Provider("provider-a", "https://a.example.com", "key-a");

    @SuppressWarnings("unchecked")
    private final HttpClient httpClient = mock(HttpClient.class);

    private final HttpPhoneAvailabilityClient client =
            new HttpPhoneAvailabilityClient(httpClient, new ObjectMapper());

    @SuppressWarnings("unchecked")
    private void stubResponse(int status, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);
    }

    @Test
    void returnsAvailableOn200WithStatusOk() throws Exception {
        stubResponse(200, "{\"status\":\"ok\"}");

        PhoneAvailability result = client.check(PROVIDER, "+79990000000");

        assertEquals(PhoneAvailability.AVAILABLE, result);
    }

    @Test
    void returnsUnavailableOn404() throws Exception {
        stubResponse(404, "");

        PhoneAvailability result = client.check(PROVIDER, "+79991112233");

        assertEquals(PhoneAvailability.UNAVAILABLE, result);
    }

    @Test
    void throwsOnOtherHttpError() throws Exception {
        stubResponse(500, "boom");

        ProviderErrorException ex = assertThrows(ProviderErrorException.class,
                () -> client.check(PROVIDER, "+79990000000"));

        assertTrue(ex.getMessage().contains("500"));
        assertTrue(ex.getMessage().contains("provider-a"));
    }

    @Test
    void throwsOn200WithUnexpectedBody() throws Exception {
        stubResponse(200, "{\"status\":\"unknown\"}");

        assertThrows(ProviderErrorException.class,
                () -> client.check(PROVIDER, "+79990000000"));
    }

    @Test
    void throwsOn200WithMalformedBody() throws Exception {
        stubResponse(200, "not-json");

        assertThrows(ProviderErrorException.class,
                () -> client.check(PROVIDER, "+79990000000"));
    }

    @Test
    void wrapsNetworkError() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new java.io.IOException("connection refused"));

        ProviderUnavailableException ex = assertThrows(ProviderUnavailableException.class,
                () -> client.check(PROVIDER, "+79990000000"));

        assertInstanceOf(java.io.IOException.class, ex.getCause());
    }

    @Test
    void rejectsBlankPhone() {
        assertThrows(IllegalArgumentException.class,
                () -> client.check(PROVIDER, " "));
        verifyNoInteractions(httpClient);
    }

    @Test
    void sendsRequestToExpectedUrl() throws Exception {
        stubResponse(200, "{\"status\":\"ok\"}");

        client.check(PROVIDER, "+79990000000");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

        HttpRequest sent = requestCaptor.getValue();
        assertEquals("https://a.example.com/subscribers/+79990000000",
                sent.uri().toString());
        assertEquals("GET", sent.method());
        assertEquals("Bearer key-a",
                sent.headers().firstValue("Authorization").orElseThrow());
    }
}
