/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.data.config.registry.BlockEntityAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockRandomProximityPositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.time.TimeStopController;
import hellfirepvp.astralsorcery.common.util.time.TimeStopZone;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectHorologium
 * Created by HellFirePvP
 * Date: 18.01.2020 / 22:41
 */
public class CEffectHorologium extends CEffectAbstractList<ListEntries.PosEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("horologium");
    public static HorologiumConfig CONFIG = new HorologiumConfig();

    public CEffectHorologium(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.horologium, CONFIG.maxAmount.get(), (world, pos, state) ->
                BlockEntityAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(MiscUtils.getBlockEntityAt(world, pos, BlockEntity.class, false)));
    }

    @Nonnull
    @Override
    protected BlockPositionGenerator createPositionStrategy() {
        return new BlockRandomProximityPositionGenerator();
    }

    @Nullable
    @Override
    public ListEntries.PosEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Nullable
    @Override
    public ListEntries.PosEntry createElement(Level world, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level world, BlockPos pos, RitualPedestalBlockEntity pedestal, float alphaMultiplier, boolean extended) {
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        for (int i = 0; i < 2; i++) {
            Color c = MiscUtils.eitherOf(rand,
                    () -> Color.WHITE,
                    () -> ColorsAS.CONSTELLATION_HOROLOGIUM);
            Vector3 at = Vector3.random().normalize().multiply(rand.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.3F + rand.nextFloat() * 0.5F)
                    .setMaxAge(40 + rand.nextInt(20));
        }

        if (rand.nextInt(16) == 0) {
            Vector3 rand1 = Vector3.random().normalize().multiply(rand.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);
            Vector3 rand2 = Vector3.random().normalize().multiply(rand.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);
            Effects.of(EffectTemplatesAS.LIGHTNING)
                    .spawn(rand1)
                    .makeDefault(rand2)
                    .color(VFXColorFunction.WHITE);
        }
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        boolean changed = false;

        if (properties.isCorrupted()) {
            TimeStopZone zone = TimeStopController.tryGetZoneAt(world, pos);
            if (zone == null) {
                zone = TimeStopController.freezeWorldAt(TimeStopZone.EntityTargetController.noPlayers(), world, pos, (float) properties.getSize(), 100);
            }
            zone.setTicksToLive(100);
            return true;
        }

        ListEntries.PosEntry entry = this.getRandomElementChanced();
        if (entry != null) {
            if (MiscUtils.executeWithChunk(world, entry.getPos(), () -> {
                BlockEntity blockEntity = MiscUtils.getBlockEntityAt(world, entry.getPos(), BlockEntity.class, true);
                if (blockEntity != null && isValid(world, entry)) {
                    sendConstellationPing(world, new Vector3(entry.getPos()).add(Vector3.positiveRandom()));
                    sendConstellationPing(world, new Vector3(entry.getPos()).add(Vector3.positiveRandom()));
                    try {
                        long startNs = System.nanoTime();
                        int times = 4 + rand.nextInt(2);
                        while (times > 0) {
                            tickBlockEntity(world, entry.getPos(), blockEntity);
                            if ((System.nanoTime() - startNs) >= 80_000) {
                                break;
                            }
                            times--;
                        }
                    } catch (Exception exc) {
                        BlockEntityAccelerationBlacklistRegistry.INSTANCE.addErrored(blockEntity);
                        this.removeElement(entry);
                        AstralSorcery.LOG.warn("Couldn't accelerate TileEntity " + blockEntity.getClass().getName() + ".");
                        AstralSorcery.LOG.warn("Temporarily blacklisting that class. Consider adding that to the blacklist if it persists?");
                        exc.printStackTrace();
                    }
                } else {
                    this.removeElement(entry);
                    return true;
                }
                return false;
            }, false)) {
                changed = true;
            }
        }

        if (this.findNewPosition(world, pos, properties)
                .ifRight(attemptedPos -> sendConstellationPing(world, new Vector3(attemptedPos).add(0.5, 0.5, 0.5)))
                .left().isPresent()) {
            changed = true;
        }

        return changed;
    }

    //1.16.5 accelerated a tile by casting it to ITickableTileEntity and calling #tick() (CEffectHorologium.java:130).
    //1.20.1 removed ITickableTileEntity entirely: a block entity is ticked by the BlockEntityTicker its EntityBlock
    //hands out through #getTicker, which is precisely what the Level does once per tick for it. Fetching that ticker
    //and invoking it here is therefore the same "one extra tick" the reference performed - and, unlike a cast to one
    //of this mod's own tile base classes, it keeps working for vanilla and third-party machines.
    //VERIFY_PORT[AS-CST-02]: needs an in-game check that accelerating e.g. a vanilla furnace still works, since the
    //ticker is resolved per call instead of being cached by the chunk like vanilla does.
    @SuppressWarnings("unchecked")
    private static void tickBlockEntity(Level world, BlockPos pos, BlockEntity blockEntity) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof EntityBlock entityBlock) {
            BlockEntityTicker<BlockEntity> ticker = entityBlock.getTicker(world, state, (BlockEntityType<BlockEntity>) blockEntity.getType());
            if (ticker != null) {
                ticker.tick(world, pos, state, blockEntity);
            }
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class HorologiumConfig extends CountConfig {

        public HorologiumConfig() {
            super("horologium", 6D, 3D, 32);
        }
    }
}
