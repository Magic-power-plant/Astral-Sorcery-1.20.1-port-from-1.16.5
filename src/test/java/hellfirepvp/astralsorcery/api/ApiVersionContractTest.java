package hellfirepvp.astralsorcery.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApiVersionContractTest {
    @Test void requiresTheSameMajorAndAnAvailableMinor() {
        assertTrue(AstralSorceryApi.supports(1, 0));
        assertFalse(AstralSorceryApi.supports(0, 0));
        assertFalse(AstralSorceryApi.supports(2, 0));
        assertFalse(AstralSorceryApi.supports(1, 1));
        assertFalse(AstralSorceryApi.supports(1, -1));
        assertDoesNotThrow(() -> AstralSorceryApi.requireVersion(1, 0));
        assertThrows(IllegalStateException.class, () -> AstralSorceryApi.requireVersion(2, 0));
    }
}
