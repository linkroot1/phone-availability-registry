package org.example.phoneavailabilityregistry.client;

import org.example.phoneavailabilityregistry.model.PhoneAvailability;
import org.example.phoneavailabilityregistry.model.Provider;

public interface PhoneAvailabilityClient {

    PhoneAvailability check(Provider provider, String phone);
}
