package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimplePrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionSourceNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalTransmissionNode;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverAltar;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverRitualPedestal;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverTreeBeacon;
import hellfirepvp.astralsorcery.common.tile.network.StarlightReceiverWell;
import hellfirepvp.astralsorcery.common.tile.network.StarlightTransmissionLens;
import hellfirepvp.astralsorcery.common.tile.network.StarlightTransmissionPrism;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TransmissionClassRegistry {

    public static final TransmissionClassRegistry eventInstance = new TransmissionClassRegistry();

    private static final Map<ResourceLocation, TransmissionProvider> providerMap = new HashMap<>();

    private TransmissionClassRegistry() {}

    public void registerProvider(TransmissionProvider provider) {
        register(provider);
    }

    @Nullable
    public static TransmissionProvider getProvider(ResourceLocation identifier) {
        return providerMap.get(identifier);
    }

    public static void register(TransmissionProvider provider) {
        if (providerMap.containsKey(provider.getIdentifier())) {
            throw new IllegalArgumentException("Already registered identifier TransmissionProvider: " + provider.getIdentifier());
        }
        providerMap.put(provider.getIdentifier(), provider);
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

        //1.16.5 TransmissionClassRegistry.java:71. Without this an addon has no way to register its own
        //TransmissionProvider, so its persisted nodes are dropped with a warning by LightNetworkBuffer on world
        //load. No effect for a vanilla player - the eleven providers above are registered directly either way.
        MinecraftForge.EVENT_BUS.post(new StarlightNetworkEvent.TransmissionRegister(eventInstance));
    }
}
