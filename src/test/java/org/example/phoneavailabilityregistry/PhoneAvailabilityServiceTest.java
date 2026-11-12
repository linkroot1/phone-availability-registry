package org.example.phoneavailabilityregistry;

import org.example.phoneavailabilityregistry.client.PhoneAvailabilityClient;
import org.example.phoneavailabilityregistry.client.ProviderErrorException;
import org.example.phoneavailabilityregistry.client.ProviderUnavailableException;
import org.example.phoneavailabilityregistry.model.PhoneAvailability;
import org.example.phoneavailabilityregistry.model.Provider;
import org.example.phoneavailabilityregistry.registry.InMemoryProviderRegistry;
import org.example.phoneavailabilityregistry.registry.ProviderRegistry;
import org.example.phoneavailabilityregistry.service.PhoneAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneAvailabilityServiceTest {

    private static final Provider PROVIDER =
            new Provider("provider-a", "https://a.example.com", "key-a");

    private ProviderRegistry registry;
    private PhoneAvailabilityClient client;
    private PhoneAvailabilityService service;

    @BeforeEach
    void setUp() {
        registry = new InMemoryProviderRegistry();
        registry.replaceAll(List.of(PROVIDER));
        client = mock(PhoneAvailabilityClient.class);
        service = new PhoneAvailabilityService(registry, client);
    }

    @Test
    void returnsAvailableWhenClientReportsOk() {
        when(client.check(PROVIDER, "+79990000000"))
                .thenReturn(PhoneAvailability.AVAILABLE);

        PhoneAvailability result = service.check("provider-a", "+79990000000");

        assertEquals(PhoneAvailability.AVAILABLE, result);
        verify(client).check(PROVIDER, "+79990000000");
    }

    @Test
    void returnsUnavailableWhenClientReports404() {
        when(client.check(PROVIDER, "+79991112233"))
                .thenReturn(PhoneAvailability.UNAVAILABLE);

        PhoneAvailability result = service.check("provider-a", "+79991112233");

        assertEquals(PhoneAvailability.UNAVAILABLE, result);
    }

    @Test
    void throwsWhenProviderUnknown() {
        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> service.check("unknown", "+79990000000"));

        assertTrue(ex.getMessage().contains("unknown"));
        verifyNoInteractions(client);
    }

    @Test
    void propagatesProviderErrorFromClient() {
        when(client.check(PROVIDER, "+79990000000"))
                .thenThrow(new ProviderErrorException("provider returned 500"));

        assertThrows(ProviderErrorException.class,
                () -> service.check("provider-a", "+79990000000"));
    }

    @Test
    void propagatesNetworkErrorFromClient() {
        when(client.check(PROVIDER, "+79990000000"))
                .thenThrow(new ProviderUnavailableException("network down", new RuntimeException()));

        assertThrows(ProviderUnavailableException.class,
                () -> service.check("provider-a", "+79990000000"));
    }

    @Test
    void usesProviderFromRegistryNotFromRequest() {
        when(client.check(PROVIDER, "+79990000000"))
                .thenReturn(PhoneAvailability.AVAILABLE);

        service.check("provider-a", "+79990000000");

        verify(client).check(PROVIDER, "+79990000000");
        verifyNoMoreInteractions(client);
    }
}
