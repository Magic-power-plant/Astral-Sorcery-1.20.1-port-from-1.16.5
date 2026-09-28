package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

//1.16.5 marks only getEffectColor client-side. Marking the whole interface made TileRitualPedestal and
//TileTreeBeacon (which implement it) fail to load on a dedicated server.
public interface TileAreaOfInfluence {

    @OnlyIn(Dist.CLIENT)
    @Nullable
    Color getEffectColor();

    @Nonnull
    Vector3 getEffectPosition();

    float getRadius();

    @Nonnull
    BlockPos getEffectOriginPosition();

    @Nonnull
    ResourceKey<Level> getDimension();

    boolean providesEffect();
}
