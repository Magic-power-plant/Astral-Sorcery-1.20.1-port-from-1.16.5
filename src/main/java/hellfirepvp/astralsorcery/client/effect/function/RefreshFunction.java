/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.function;

import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RefreshFunction
 * Created by HellFirePvP
 * Date: 27.05.2019 / 22:43
 */
public interface RefreshFunction<T extends EntityComplexFX> {

    RefreshFunction<?> DESPAWN = fx -> false;

    public static <E extends BlockEntity, T extends EntityComplexFX> RefreshFunction<T> blockEntityExists(E blockEntity) {
        return new BlockEntityExists<>(blockEntity);
    }

    public static <E extends BlockEntity, T extends EntityComplexFX> RefreshFunction<T> blockEntityExistsAnd(E blockEntity, BiPredicate<E, T> refreshFct) {
        BlockEntityExists<E, T> fct = new BlockEntityExists<>(blockEntity);
        return (fx) -> Optional.ofNullable(fct.getBlockEntityIfValid()).map(t -> refreshFct.test(t, fx)).orElse(false);
    }

    public boolean shouldRefresh(@Nonnull T fx);

    public static class BlockEntityExists<E extends BlockEntity, T extends EntityComplexFX> implements RefreshFunction<T> {

        private final ResourceKey<Level> dimType;
        private final BlockPos pos;
        private final Class<E> clazzExpected;

        public BlockEntityExists(E blockEntity) {
            this.dimType = blockEntity.getLevel().dimension();
            this.pos = blockEntity.getBlockPos();
            this.clazzExpected = (Class<E>) blockEntity.getClass();
        }

        @Override
        public boolean shouldRefresh(@Nonnull T fx) {
            return getBlockEntityIfValid() != null;
        }

        @Nullable
        protected E getBlockEntityIfValid() {
            Level clWorld = Minecraft.getInstance().level;
            E blockEntity;
            if (clWorld != null &&
                    clWorld.dimension().equals(dimType) &&
                    (blockEntity = MiscUtils.getBlockEntityAt(clWorld, pos, clazzExpected, true)) != null &&
                    !blockEntity.isRemoved()) {
                return blockEntity;
            }
            return null;
        }
    }

}
