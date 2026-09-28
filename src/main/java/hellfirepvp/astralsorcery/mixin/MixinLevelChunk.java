/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.event.BlockChangeNotifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinLevelChunk
 * Created by HellFirePvP
 * Date: 06.06.2022 / 09:26
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.mixin.MixinLevelChunk);
 * ObserverLib is a source-only reference for this port, not a runtime dependency.
 *
 * Injects into LevelChunk#setBlockState(BlockPos, BlockState, boolean) right before the first
 * BlockState#getBlock() call, i.e. after the section state has been swapped and the
 * "state unchanged" early return has been taken. Captured locals (javap LocalVariableTable):
 * i (y), levelchunksection, flag (section was empty), j/k/l (x&15, y&15, z&15), blockstate (previous state).
 */
@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk {

    @Shadow @Final Level level;

    @Inject(
            method = "setBlockState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onBlockStateUpdate(BlockPos p_62865_, BlockState p_62866_, boolean p_62867_, CallbackInfoReturnable<BlockState> cir, int i, LevelChunkSection levelchunksection, boolean flag, int j, int k, int l, BlockState blockstate) {
        if (this.level.isClientSide()) {
            return;
        }
        LevelChunk thisLevelChunk = (LevelChunk)(Object) this;
        BlockChangeNotifier.onBlockChange(this.level, thisLevelChunk, p_62865_, blockstate, p_62866_);
    }
}
