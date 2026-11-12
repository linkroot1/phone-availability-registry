package org.example.phoneavailabilityregistry.service;

import org.example.phoneavailabilityregistry.client.PhoneAvailabilityClient;
import org.example.phoneavailabilityregistry.model.PhoneAvailability;
import org.example.phoneavailabilityregistry.model.Provider;
import org.example.phoneavailabilityregistry.registry.ProviderRegistry;

import java.util.NoSuchElementException;

public class PhoneAvailabilityService {

    private final ProviderRegistry registry;
    private final PhoneAvailabilityClient client;

    public PhoneAvailabilityService(ProviderRegistry registry, PhoneAvailabilityClient client) {
        this.registry = registry;
        this.client = client;
    }

    public PhoneAvailability check(String providerId, String phone) {
        Provider provider = registry.findById(providerId)
                .orElseThrow(() -> new NoSuchElementException("Unknown provider: " + providerId));
        return client.check(provider, phone);
    }
}
