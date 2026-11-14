package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.registry.extension.ExtensionRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimplePrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionSourceNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalTransmissionNode;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverAltar;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverRitualPedestal;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverTreeBeacon;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverWell;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightTransmissionLens;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightTransmissionPrism;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public class TransmissionClassRegistry {

    public static final TransmissionClassRegistry eventInstance = new TransmissionClassRegistry();

    private static final ExtensionRegistry<TransmissionProvider> providerMap = new ExtensionRegistry<>("TransmissionProvider");

    private TransmissionClassRegistry() {}

    public void registerProvider(TransmissionProvider provider) {
        register(provider);
    }

    @Nullable
    public static TransmissionProvider getProvider(ResourceLocation identifier) {
        return providerMap.get(identifier);
    }

    public static void register(TransmissionProvider provider) {
        Objects.requireNonNull(provider, "provider");
        providerMap.register(provider.getIdentifier(), provider);
    }

    public static void freeze() {
        providerMap.freeze();
    }

    public static void setupRegistry() {
        register(new SimpleTransmissionNode.Provider());
        register(new SimplePrismTransmissionNode.Provider());
        register(new SimpleTransmissionSourceNode.Provider());
        register(new CrystalTransmissionNode.Provider());
        register(new CrystalPrismTransmissionNode.Provider());
        register(new StarlightReceiverAltar.Provider());
        register(new StarlightReceiverWell.Provider());
        register(new StarlightReceiverRitualPedestal.Provider());
        register(new StarlightReceiverTreeBeacon.Provider());
        register(new StarlightTransmissionLens.Provider());
        register(new StarlightTransmissionPrism.Provider());

        // Extension events are dispatched later, during enqueued common setup.
    }
}
