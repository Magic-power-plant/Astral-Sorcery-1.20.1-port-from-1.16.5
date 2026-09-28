package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class MaterialBuilderAS {

    private final MapColor color;
    private PushReaction pushReaction = PushReaction.NORMAL;
    private boolean blocksMovement = true;
    private boolean canBurn = false;
    private boolean isLiquid = false;
    private boolean isReplaceable = false;
    private boolean isSolid = true;
    private boolean isOpaque = true;

    public MaterialBuilderAS(MapColor color) {
        this.color = color;
    }

    public MaterialBuilderAS liquid() {
        this.isLiquid = true;
        return this;
    }

    public MaterialBuilderAS notSolid() {
        this.isSolid = false;
        return this;
    }

    public MaterialBuilderAS doesNotBlockMovement() {
        this.blocksMovement = false;
        return this;
    }

    public MaterialBuilderAS notOpaque() {
        this.isOpaque = false;
        return this;
    }

    public MaterialBuilderAS flammable() {
        this.canBurn = true;
        return this;
    }

    public MaterialBuilderAS replaceable() {
        this.isReplaceable = true;
        return this;
    }

    public MaterialBuilderAS pushDestroys() {
        this.pushReaction = PushReaction.DESTROY;
        return this;
    }

    public MaterialBuilderAS pushBlocks() {
        this.pushReaction = PushReaction.BLOCK;
        return this;
    }

}
