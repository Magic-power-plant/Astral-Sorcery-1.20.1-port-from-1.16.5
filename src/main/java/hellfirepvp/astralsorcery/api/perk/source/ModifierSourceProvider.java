package hellfirepvp.astralsorcery.api.perk.source;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

/** Lifecycle and serialization contract for a keyed family of modifier sources. */
public abstract class ModifierSourceProvider<T extends ModifierSource> {
    private final ResourceLocation key;

    protected ModifierSourceProvider(ResourceLocation key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    public final ResourceLocation getKey() {
        return key;
    }

    protected abstract void update(ServerPlayer player);
    protected abstract void removeModifiers(ServerPlayer player);
    public abstract void serialize(T source, FriendlyByteBuf buffer);
    public abstract T deserialize(FriendlyByteBuf buffer);
}
