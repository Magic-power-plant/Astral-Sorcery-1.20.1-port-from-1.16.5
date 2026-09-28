package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class LensColorType {

    private static final Map<ResourceLocation, LensColorType> BY_NAME = new HashMap<>();

    private final Supplier<ItemStack> itemSupplier;
    private final ResourceLocation name;
    private final TargetType type;
    private final Color color;
    private final float flowReduction;
    private final boolean ignoresBlockCollision;

    public LensColorType(ResourceLocation name, TargetType type, Supplier<ItemStack> itemSupplier, Color color, float flowReduction, boolean ignoresBlockCollision) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.flowReduction = flowReduction;
        this.ignoresBlockCollision = ignoresBlockCollision;
        this.itemSupplier = itemSupplier;
        BY_NAME.put(name, this);
    }

    public ResourceLocation getName() {
        return name;
    }

    public TargetType getType() {
        return type;
    }

    public float getFlowMultiplier() {
        return flowReduction;
    }

    public boolean doesIgnoreBlockCollision() {
        return ignoresBlockCollision;
    }

    public Color getColor() {
        return color;
    }

    public ItemStack getStack() {
        return itemSupplier.get();
    }

    @Nullable
    public static LensColorType byName(ResourceLocation name) {
        return BY_NAME.get(name);
    }

    public abstract void entityInBeam(Level world, Vector3 origin, Vector3 target, Entity entity, PartialEffectExecutor executor);

    public abstract void blockInBeam(Level world, BlockPos pos, BlockState state, PartialEffectExecutor executor);

    public enum TargetType {

        ANY,
        ENTITY,
        BLOCK,
        NONE;

        public boolean doEntityInteraction() {
            return this == ANY || this == ENTITY;
        }

        public boolean doBlockInteraction() {
            return this == ANY || this == BLOCK;
        }
    }
}
