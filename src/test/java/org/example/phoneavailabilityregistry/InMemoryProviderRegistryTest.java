package org.example.phoneavailabilityregistry;

import org.example.phoneavailabilityregistry.model.Provider;
import org.example.phoneavailabilityregistry.registry.InMemoryProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProviderRegistryTest {

    private final InMemoryProviderRegistry registry = new InMemoryProviderRegistry();

    private static final Provider P1 =
            new Provider("p1", "https://one.example.com", "k1");
    private static final Provider P2 =
            new Provider("p2", "https://two.example.com", "k2");
    private static final Provider P3 =
            new Provider("p3", "https://three.example.com", "k3");

    @Test
    void findByIdReturnsEmptyOnEmptyRegistry() {
        assertTrue(registry.findById("p1").isEmpty());
    }

    @Test
    void replaceAllAddsProviders() {
        registry.replaceAll(List.of(P1, P2));

        assertEquals(P1, registry.findById("p1").orElseThrow());
        assertEquals(P2, registry.findById("p2").orElseThrow());
    }

    @Test
    void replaceAllReplacesWholeSet() {
        registry.replaceAll(List.of(P1, P2));
        registry.replaceAll(List.of(P3));

        assertTrue(registry.findById("p1").isEmpty(), "p1 должен исчезнуть");
        assertTrue(registry.findById("p2").isEmpty(), "p2 должен исчезнуть");
        assertEquals(P3, registry.findById("p3").orElseThrow());
    }

    @Test
    void replaceAllWithEmptyListClearsRegistry() {
        registry.replaceAll(List.of(P1, P2));

        registry.replaceAll(List.of());

        assertTrue(registry.findById("p1").isEmpty());
        assertTrue(registry.findById("p2").isEmpty());
    }

    @Test
    void replaceAllRejectsDuplicateIdsAndKeepsOldState() {
        registry.replaceAll(List.of(P1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> registry.replaceAll(List.of(P2, new Provider("p2", "https://x", "k"))));

        assertTrue(ex.getMessage().contains("p2"));
        assertEquals(P1, registry.findById("p1").orElseThrow());
        assertTrue(registry.findById("p2").isEmpty());
    }

    @Test
    void replaceAllRejectsNullList() {
        assertThrows(IllegalArgumentException.class, () -> registry.replaceAll(null));
    }

    @Test
    void replaceAllRejectsNullElement() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.replaceAll(java.util.Arrays.asList(P1, null)));
    }

    @Test
    void replaceAllRejectsBlankId() {
        assertThrows(IllegalArgumentException.class,
                () -> registry.replaceAll(List.of(new Provider("  ", "https://x", "k"))));
    }

    @Test
    void findByIdHandlesNull() {
        registry.replaceAll(List.of(P1));
        assertTrue(registry.findById(null).isEmpty());
    }
}
