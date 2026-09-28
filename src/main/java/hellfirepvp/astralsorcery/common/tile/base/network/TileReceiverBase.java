package hellfirepvp.astralsorcery.common.tile.base.network;

import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.starlight.IStarlightReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.tile.base.TileNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public abstract class TileReceiverBase<T extends ITransmissionReceiver> extends TileNetwork<T> implements IStarlightReceiver<T>, LinkableTileEntity {

    protected TileReceiverBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nonnull
    public BlockPos getTrPos() {
        return getBlockPos();
    }

    @Override
    @Nonnull
    public Level getTrWorld() {
        return getLevel();
    }

    @Override
    public void onBlockLinkCreate(Player player, BlockPos other) {}

    @Override
    public void onEntityLinkCreate(Player player, LivingEntity linked) {}

    @Override
    public boolean tryLinkBlock(Player player, BlockPos other) {
        return false;
    }

    @Override
    public boolean tryLinkEntity(Player player, LivingEntity other) {
        return false;
    }

    @Override
    public boolean tryUnlink(Player player, BlockPos other) {
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return new LinkedList<>();
    }
}