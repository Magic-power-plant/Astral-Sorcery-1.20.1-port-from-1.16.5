/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.base;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FakedStateBlockEntity
 * Created by HellFirePvP
 * Date: 04.09.2020 / 19:19
 */
public abstract class FakedStateBlockEntity extends TickableBlockEntity {

    private BlockState fakedState = Blocks.AIR.defaultBlockState();
    private Color overlayColor = Color.WHITE;

    protected FakedStateBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    public boolean revert() {
        //1.16.5 only asked getWorld().isRemote(); 1.20.1 BlockEntity#level is null between construction and
        //setLevel, hence the extra null guard (same house style as TileEntityTick#doesSeeSky).
        if (this.getLevel() == null || this.getLevel().isClientSide()) {
            return false;
        }
        //1.16.5: Constants.BlockFlags.DEFAULT_AND_RERENDER -> 1.20.1: Block.UPDATE_ALL_IMMEDIATE (both == 11)
        return this.getLevel().setBlock(this.getBlockPos(), this.getFakedState(), Block.UPDATE_ALL_IMMEDIATE);
    }

    @Nonnull
    public BlockState getFakedState() {
        return fakedState;
    }

    @Nonnull
    public Color getOverlayColor() {
        return overlayColor;
    }

    public void setFakedState(@Nonnull BlockState fakedState) {
        this.fakedState = fakedState;
        this.markForUpdate();
    }

    public void setOverlayColor(@Nonnull Color overlayColor) {
        this.overlayColor = overlayColor;
        this.markForUpdate();
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.fakedState = NBTHelper.getBlockStateFromTag(compound.getCompound("fakedState"), Blocks.AIR.defaultBlockState());
        this.overlayColor = new Color(compound.getInt("color"), false);
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        NBTHelper.setBlockState(compound, "fakedState", this.fakedState);
        compound.putInt("color", this.overlayColor.getRGB());
    }

    //TODO_PORT[AS-TFS-01]: 1.16.5 overrides TileEntity#getMaxRenderDistanceSquared() to widen the faked-state
    //render range to 256 blocks (65536 = 256^2) (1.16.5 TileFakedState.java:82-85). 1.20.1 moved that knob off the
    //block entity entirely: BlockEntity/IForgeBlockEntity no longer declare it and the cutoff now comes from
    //BlockEntityRenderer#getViewDistance() (an int, NOT squared, default 64). The method below therefore overrides
    //nothing and has no effect. Fixing it means overriding getViewDistance() to return 256 in
    //client/render/tile/RenderTileFakedState.java, which is outside this slice - reported as an external change.
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
