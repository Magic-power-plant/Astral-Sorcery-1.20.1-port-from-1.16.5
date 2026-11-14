package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.util.NameUtil;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class TransmissionProvider implements Supplier<IPrismTransmissionNode> {

    private ResourceLocation identifierCache = null;

    /** Legacy built-in naming; do not use class-derived IDs for addon save formats. */
    protected TransmissionProvider() {}

    /** Explicit, stable addon ID, independent of Java class names and refactoring. */
    protected TransmissionProvider(ResourceLocation identifier) {
        this.identifierCache = java.util.Objects.requireNonNull(identifier, "identifier");
    }

    //This identifier is the on-disk key of every transmission node: LightNetworkBuffer writes it as "trNodeId"
    //and reads it back through TransmissionClassRegistry#getProvider. It is therefore part of the save format
    //and must match 1.16.5 exactly (1.16.5 TransmissionProvider.java:30 -> NameUtil.fromClass(get())).
    //
    //This used to be AstralSorcery.locate(get().getClass().getSimpleName().toLowerCase(Locale.ROOT)), which drops
    //the word separators NameUtil inserts: "simpletransmissionnode" instead of "simple_transmission_node", for
    //all eleven registered node types. Because the same wrong key was used for both writing and reading, a fresh
    //world worked and nothing ever threw - but every network saved by 1.16.5 resolved getProvider(...) to null on
    //load, silently dropping the whole light network. NameUtil#fromClass is already a faithful port in this tree;
    //it was simply not used here. The source side was never affected: IndependentCrystalSource.Provider
    //hardcodes "independent_crystal_source", as 1.16.5 does.
    @Nonnull
    public ResourceLocation getIdentifier() {
        if (identifierCache == null) {
            identifierCache = NameUtil.fromClass(get());
        }
        return identifierCache;
    }
}
