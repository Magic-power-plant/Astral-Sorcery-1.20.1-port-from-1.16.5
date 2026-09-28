package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.capability.ChunkFluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilitiesAS {

    private CapabilitiesAS() {}

    public static final ResourceLocation CHUNK_FLUID_KEY = AstralSorcery.key("chunk_fluid");

    public static final Capability<ChunkFluidEntry> CHUNK_FLUID = CapabilityManager.get(new CapabilityToken<>() {});

}
