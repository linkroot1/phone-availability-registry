package org.example.phoneavailabilityregistry.registry;

import org.example.phoneavailabilityregistry.model.Provider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryProviderRegistry implements ProviderRegistry {

    private final Map<String, Provider> providers = new HashMap<>();

    @Override
    public void replaceAll(List<Provider> newProviders) {
        if (newProviders == null) {
            throw new IllegalArgumentException("providers must not be null");
        }

        Map<String, Provider> replacement = new HashMap<>();
        for (Provider provider : newProviders) {
            if (provider == null) {
                throw new IllegalArgumentException("provider must not be null");
            }
            if (provider.id() == null || provider.id().isBlank()) {
                throw new IllegalArgumentException("provider id must not be blank");
            }
            if (replacement.putIfAbsent(provider.id(), provider) != null) {
                throw new IllegalArgumentException("duplicate provider id: " + provider.id());
            }
        }

        providers.clear();
        providers.putAll(replacement);
    }

    @Override
    public Optional<Provider> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(providers.get(id));
    }
}
