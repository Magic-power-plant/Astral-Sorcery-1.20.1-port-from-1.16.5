/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalPelotrio;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.data.config.registry.EntityTransmutationRegistry;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.block.util.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectPelotrio
 * Created by HellFirePvP
 * Date: 01.02.2020 / 14:44
 */
public class CEffectPelotrio extends CEffectAbstractList<ListEntries.EntitySpawnEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("pelotrio");
    private static final AABB PROXIMITY_BOX = new AABB(0, 0, 0, 0, 0, 0);

    public static PelotrioConfig CONFIG = new PelotrioConfig();

    public CEffectPelotrio(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.pelotrio, CONFIG.maxAmount.get(), (world, pos, state) -> {
            if (!(world instanceof ServerLevel serverWorld)) {
                return false;
            }
            //1.16.5: World#getHeight(Heightmap.Type, BlockPos) + BlockPos#up() ->
            //1.20.1: LevelReader#getHeightmapPos(Heightmap.Types, BlockPos) + BlockPos#above()
            pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).above();
            return ListEntries.EntitySpawnEntry.createEntry(serverWorld, pos, MobSpawnType.SPAWNER) != null;
        });
    }

    @Nullable
    @Override
    public ListEntries.EntitySpawnEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return null;
    }

    @Nullable
    @Override
    public ListEntries.EntitySpawnEntry createElement(Level world, BlockPos pos) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return null;
        }
        pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).above();
        return ListEntries.EntitySpawnEntry.createEntry(serverWorld, pos, MobSpawnType.SPAWNER);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level world, BlockPos pos, RitualPedestalBlockEntity pedestal, float alphaMultiplier, boolean extended) {
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        if (rand.nextFloat() < 0.2F) {
            Vector3 at = Vector3.random().normalize().multiply(rand.nextFloat() * prop.getSize()).add(pos).add(0.5, 0.5, 0.5);

            Effects.spawnSource(new FXOrbitalPelotrio(at)
                    .setOrbitAxis(Vector3.random())
                    .setOrbitRadius(0.8 + rand.nextFloat() * 0.7)
                    .setTicksPerRotation(20 + rand.nextInt(20)));
        }
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return false;
        }

        boolean update = false;

        //1.16.5: World#getEntitiesWithinAABB -> Level#getEntitiesOfClass; AxisAlignedBB#offset/#grow -> AABB#move/#inflate
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, PROXIMITY_BOX.move(pos).inflate(properties.getSize()));

        if (properties.isCorrupted()) {
            for (LivingEntity entity : nearbyEntities) {
                if (entity != null && entity.isAlive() && rand.nextInt(300) == 0) {
                    LivingEntity transmuted = EntityTransmutationRegistry.INSTANCE.transmuteEntity(serverWorld, entity);
                    if (transmuted != null) {
                        //1.16.5: LivingEntity#addPotionEffect(EffectInstance) -> #addEffect(MobEffectInstance);
                        //EffectsAS holds RegistryObjects in 1.20.1, hence the #get().
                        transmuted.addEffect(new MobEffectInstance(EffectsAS.EFFECT_DROP_MODIFIER.get(), Integer.MAX_VALUE, 1));
                        //1.16.5: World#addEntity -> Level#addFreshEntity. The spawn stays deferred by a tick
                        //because we are iterating the world's entity list right now.
                        AstralSorcery.getProxy().scheduleDelayed(() -> world.addFreshEntity(transmuted));
                        update = true;
                    }
                }
            }
            return update;
        }

        ListEntries.EntitySpawnEntry entry = this.getRandomElementChanced();
        if (entry != null) {
            int count = entry.getCounter();
            count++;
            entry.setCounter(count);
            sendConstellationPing(world, new Vector3(entry.getPos()).add(0.5, 0.5, 0.5));
            if (count >= 10) {
                entry.spawn(serverWorld, MobSpawnType.SPAWNER);
                removeElement(entry);
            }

            update = true;
        }

        if (nearbyEntities.size() > CONFIG.proximityAmount.get()) {
            return update; //Flood prevention
        }

        if (rand.nextFloat() < CONFIG.spawnChance.get()) {
            if (findNewPosition(world, pos, properties).left().isPresent()) {
                update = true;
            }
        }
        return update;
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class PelotrioConfig extends CountConfig {

        private final double defaultSpawnChance = 0.05D;
        private final int defaultProximityAmount = 24;

        public ForgeConfigSpec.DoubleValue spawnChance;
        public ForgeConfigSpec.IntValue proximityAmount;

        public PelotrioConfig() {
            super("pelotrio", 12D, 0D, 5);
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.spawnChance = cfgBuilder
                    .comment("Defines the per-tick chance that a new position for a entity-spawn will be searched for.")
                    .translation(translationKey("spawnChance"))
                    .defineInRange("spawnChance", this.defaultSpawnChance, 0, 1);

            this.proximityAmount = cfgBuilder
                    .comment("Defines the threshold at which the ritual will stop spawning mobs. If there are more or equal amount of mobs near this ritual, the ritual will not spawn more mobs. Mainly to reduce potential server lag.")
                    .translation(translationKey("proximityAmount"))
                    .defineInRange("proximityAmount", this.defaultProximityAmount, 0, 256);
        }
    }
}
