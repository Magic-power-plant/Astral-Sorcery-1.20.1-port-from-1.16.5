package hellfirepvp.astralsorcery.common.block.util;

import net.minecraft.core.BlockPos;

public interface ILocatable {

    BlockPos getLocationPos();

    static ILocatable fromPos(BlockPos pos) {
        return new PosLocatable(pos);
    }

    class PosLocatable implements ILocatable {

        private final BlockPos pos;

        private PosLocatable(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public BlockPos getLocationPos() {
            return pos;
        }
    }
}
