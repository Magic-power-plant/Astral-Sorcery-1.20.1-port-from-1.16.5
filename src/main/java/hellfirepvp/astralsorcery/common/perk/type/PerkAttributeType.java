package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.api.perk.IPerkAttributeType;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PerkAttributeType implements IPerkAttributeType {

    protected static final Random rand = new Random();

    private static final Set<PerkAttributeType> INSTANCES = Collections.newSetFromMap(new IdentityHashMap<>());

    private final ResourceLocation registryName;
    private final boolean isOnlyMultiplicative;
    private final Map<LogicalSide, Set<UUID>> applicationCache = new EnumMap<>(LogicalSide.class);
    private final ReadWriteLock accessLock = new ReentrantReadWriteLock(true);

    protected PerkAttributeType(ResourceLocation key) {
        this(key, false);
    }

    protected PerkAttributeType(ResourceLocation key, boolean isMultiplicative) {
        this.registryName = key;
        this.isOnlyMultiplicative = isMultiplicative;
        INSTANCES.add(this);
        this.init();
        this.attachListeners(MinecraftForge.EVENT_BUS);
    }

    public static PerkAttributeType makeDefault(ResourceLocation name, boolean isMultiplicative) {
        return new PerkAttributeType(name, isMultiplicative);
    }

    public boolean isMultiplicative() {
        return isOnlyMultiplicative;
    }

    public Component getTranslatedName() {
        return Component.translatable(this.getUnlocalizedName());
    }

    public String getUnlocalizedName() {
        return String.format("perk.attribute.%s.%s.name",
                this.getRegistryName().getNamespace(), this.getRegistryName().getPath());
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    protected void init() {}

    protected void attachListeners(IEventBus eventBus) {}

    protected LogicalSide getSide(Entity entity) {
        return entity.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    @Nullable
    public PerkAttributeReader getReader() {
        return RegistriesAS.PERK_ATTRIBUTE_READERS.getValue(this.getRegistryName());
    }

    @Nonnull
    public PerkAttributeModifier createModifier(float modifier, ModifierType mode) {
        if (isMultiplicative() && mode == ModifierType.ADDITION) {
            throw new IllegalArgumentException("Tried creating addition-modifier for a multiplicative-only modifier!");
        }
        return new PerkAttributeModifier(this, mode, modifier);
    }

    public void onApply(Player player, LogicalSide side, ModifierSource source) {
        this.accessLock.writeLock().lock();
        try {
            this.applicationCache.computeIfAbsent(side, ignored -> new HashSet<>()).add(player.getUUID());
        } finally {
            this.accessLock.writeLock().unlock();
        }
    }

    public void onRemove(Player player, LogicalSide side, boolean removedCompletely, ModifierSource source) {
        if (!removedCompletely) {
            return;
        }
        this.accessLock.writeLock().lock();
        try {
            this.applicationCache.getOrDefault(side, Collections.emptySet()).remove(player.getUUID());
        } finally {
            this.accessLock.writeLock().unlock();
        }
    }

    public void onModeApply(Player player, ModifierType mode, LogicalSide side) {}

    public void onModeRemove(Player player, ModifierType mode, LogicalSide side, boolean removedCompletely) {}

    public boolean hasTypeApplied(Player player, LogicalSide side) {
        this.accessLock.readLock().lock();
        try {
            return this.applicationCache.getOrDefault(side, Collections.emptySet()).contains(player.getUUID());
        } finally {
            this.accessLock.readLock().unlock();
        }
    }

    private void clear(LogicalSide side) {
        this.accessLock.writeLock().lock();
        try {
            this.applicationCache.remove(side);
        } finally {
            this.accessLock.writeLock().unlock();
        }
    }

    public static void clearCache(LogicalSide side) {
        for (PerkAttributeType type : INSTANCES) {
            type.clear(side);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerkAttributeType that = (PerkAttributeType) o;
        return Objects.equals(this.registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return this.registryName.hashCode();
    }
}
