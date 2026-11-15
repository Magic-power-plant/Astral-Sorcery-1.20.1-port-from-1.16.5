package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NetworkExtensionRegistrationTest {
    @Test void explicitIdSurvivesSaveAndRegistryReconstruction() {
        ResourceLocation id = new ResourceLocation("exampleaddon", "named_node");
        NamedProvider provider = new NamedProvider(id);
        new RegisterAstralSorceryExtensionsEvent().registerTransmission(provider);
        assertEquals(0, provider.creations, "Explicit ID lookup must not construct a node");
        IPrismTransmissionNode original = provider.get();
        CompoundTag data = new CompoundTag();
        original.writeToNBT(data);
        data.putString("trNodeId", original.getProvider().getIdentifier().toString());
        TransmissionProvider restoredProvider = TransmissionClassRegistry.getProvider(ResourceLocation.parse(data.getString("trNodeId")));
        assertSame(provider, restoredProvider);
        IPrismTransmissionNode restored = restoredProvider.get();
        restored.readFromNBT(data);
        assertNotSame(original, restored);
        assertEquals(original.getLocationPos(), restored.getLocationPos());
        assertEquals(id, restored.getProvider().getIdentifier());
        assertThrows(IllegalArgumentException.class, () -> new RegisterAstralSorceryExtensionsEvent()
                .registerTransmission(new NamedProvider(id)));
        assertSame(provider, TransmissionClassRegistry.getProvider(id));
    }

    @Test void independentSourceRegistrationIsNamespacedAndDoesNotInvokeTheFactory() {
        ResourceLocation id = new ResourceLocation("exampleaddon", "source");
        SourceClassRegistry.SourceProvider provider = new SourceClassRegistry.SourceProvider() {
            public IIndependentStarlightSource provideEmptySource() { throw new AssertionError("Factory called while registering"); }
            public ResourceLocation getIdentifier() { return id; }
        };
        RegisterAstralSorceryExtensionsEvent event = new RegisterAstralSorceryExtensionsEvent();
        event.registerSource(provider);
        assertSame(provider, SourceClassRegistry.getProvider(id));
        assertThrows(IllegalArgumentException.class, () -> event.registerSource(provider));
        assertThrows(NullPointerException.class, () -> event.registerSource(null));
        assertThrows(IllegalArgumentException.class, () -> event.registerTransmission(
                new NamedProvider(new ResourceLocation("astralsorcery", "reserved"))));
    }

    @Test void sourceIdIsReadOnceAndTheValidatedKeyIsThePublishedKey() {
        ResourceLocation id = ResourceLocation.parse("exampleaddon:captured_source");
        int[] reads = {0};
        SourceClassRegistry.SourceProvider provider = new SourceClassRegistry.SourceProvider() {
            public IIndependentStarlightSource provideEmptySource() { throw new AssertionError(); }
            public ResourceLocation getIdentifier() {
                return reads[0]++ == 0 ? id : ResourceLocation.parse("minecraft:invalid_source");
            }
        };
        new RegisterAstralSorceryExtensionsEvent().registerSource(provider);
        assertEquals(1, reads[0]);
        assertSame(provider, SourceClassRegistry.getProvider(id));
        assertNull(SourceClassRegistry.getProvider(ResourceLocation.parse("minecraft:invalid_source")));
    }

    @Test void transmissionIdIsReadOnceAndReservedIdsAreRejectedWithoutConstructingNodes() {
        ResourceLocation id = ResourceLocation.parse("exampleaddon:captured_transmission");
        int[] reads = {0};
        TransmissionProvider provider = new TransmissionProvider(id) {
            public ResourceLocation getIdentifier() {
                return reads[0]++ == 0 ? id : ResourceLocation.parse("minecraft:invalid_transmission");
            }
            public IPrismTransmissionNode get() { throw new AssertionError(); }
        };
        RegisterAstralSorceryExtensionsEvent event = new RegisterAstralSorceryExtensionsEvent();
        event.registerTransmission(provider);
        assertEquals(1, reads[0]);
        assertSame(provider, TransmissionClassRegistry.getProvider(id));
        assertNull(TransmissionClassRegistry.getProvider(ResourceLocation.parse("minecraft:invalid_transmission")));
        for (String namespace : java.util.List.of("minecraft", "astralsorcery")) {
            NamedProvider reserved = new NamedProvider(ResourceLocation.fromNamespaceAndPath(namespace, "reserved"));
            assertThrows(IllegalArgumentException.class, () -> event.registerTransmission(reserved));
            assertEquals(0, reserved.creations);
            assertThrows(IllegalArgumentException.class, () -> event.registerSource(new SourceClassRegistry.SourceProvider() {
                public ResourceLocation getIdentifier() { return reserved.getIdentifier(); }
                public IIndependentStarlightSource provideEmptySource() { throw new AssertionError(); }
            }));
        }
    }

    private static final class NamedProvider extends TransmissionProvider {
        int creations;
        NamedProvider(ResourceLocation id) { super(id); }
        public IPrismTransmissionNode get() {
            creations++;
            return new SimpleTransmissionNode(new BlockPos(2, 3, 4)) {
                @Override public TransmissionProvider getProvider() { return NamedProvider.this; }
            };
        }
    }
}
