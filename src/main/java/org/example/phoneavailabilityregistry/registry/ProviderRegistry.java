package org.example.phoneavailabilityregistry.registry;

import org.example.phoneavailabilityregistry.model.Provider;

import java.util.List;
import java.util.Optional;

public interface ProviderRegistry {

    void replaceAll(List<Provider> providers);

    Optional<Provider> findById(String id);
}
