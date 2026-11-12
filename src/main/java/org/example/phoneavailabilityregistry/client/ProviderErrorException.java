package org.example.phoneavailabilityregistry.client;

public class ProviderErrorException extends RuntimeException {
    public ProviderErrorException(String message) {
        super(message);
    }

    public ProviderErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
