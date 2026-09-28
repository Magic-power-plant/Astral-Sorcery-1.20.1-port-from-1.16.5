package hellfirepvp.astralsorcery.common.event.handler;

import hellfirepvp.astralsorcery.common.block.BlockAltar;
import hellfirepvp.astralsorcery.common.event.BlockChangeNotifier;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Port of 1.16.5 {@code EventHandlerAutoLink}: listens to every server-side block change (through the embedded
 * {@link BlockChangeNotifier} fired from {@code MixinLevelChunk}) and keeps the starlight network's auto-links
 * to crafting tables and altars in sync.
 */
public class EventHandlerAutoLink implements BlockChangeNotifier.Listener {

    @Override
    public void onChange(Level world, LevelChunk chunk, BlockPos pos, BlockState oldState, BlockState newState) {
        if (world.isClientSide() || !chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
            return;
        }

        Block oldB = oldState.getBlock();
        Block newB = newState.getBlock();

        if (oldB != newB) {
            WorldNetworkHandler handle = WorldNetworkHandler.getNetworkHandler(world);
            handle.informBlockChange(pos);

            if (oldB == Blocks.CRAFTING_TABLE) {
                handle.removeAutoLinkTo(pos);
            }
            if (newB == Blocks.CRAFTING_TABLE) {
                handle.attemptAutoLinkTo(pos);
            }
            if (oldB instanceof BlockAltar) {
                handle.removeAutoLinkTo(pos);
            }
            if (newB instanceof BlockAltar) {
                handle.attemptAutoLinkTo(pos);
            }
        }
    }
}
