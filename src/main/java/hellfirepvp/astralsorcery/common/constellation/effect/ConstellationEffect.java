/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.api.IConstellationEffect;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.blockentity.ritual.RitualLinkBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationEffect
 * Created by HellFirePvP
 * Date: 05.06.2019 / 22:07
 */
public abstract class ConstellationEffect implements IConstellationEffect {

    protected static final Random rand = new Random();
    protected static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);

    private final IWeakConstellation cst;
    private final ILocatable pos;
    private boolean needsChunkToBeLoaded = false;

    protected ConstellationEffect(@Nonnull ILocatable origin, @Nonnull IWeakConstellation cst) {
        this.cst = cst;
        this.pos = origin;
    }

    protected void setChunkNeedsToBeLoaded() {
        this.needsChunkToBeLoaded = true;
    }

    public boolean needsChunkToBeLoaded() {
        return this.needsChunkToBeLoaded;
    }

    @Override
    public abstract boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait);

    @Nullable
    public RitualPedestalBlockEntity getPedestal(Level world, BlockPos pos) {
        BlockEntity te = MiscUtils.getBlockEntityAt(world, pos, BlockEntity.class, false);
        if (te instanceof RitualLinkBlockEntity link) {
            BlockPos linkedPos = link.getLinkedTo();
            if (linkedPos != null) {
                linkedPos = linkedPos.subtract(RitualPedestalBlockEntity.RITUAL_ANCHOR_OFFEST);
                return MiscUtils.getBlockEntityAt(world, linkedPos, RitualPedestalBlockEntity.class, false);
            }
        }
        return te instanceof RitualPedestalBlockEntity pedestal ? pedestal : null;
    }

    public ConstellationEffectProperties createProperties(int mirrors) {
        return new ConstellationEffectProperties(getConfig().range.get() + mirrors * getConfig().rangePerLens.get());
    }

    public abstract Config getConfig();

    public abstract PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag();

    protected static PlayerAffectionFlags.AffectionFlag makeAffectionFlag(String constellationName) {
        return new PlayerAffectionFlags.NoOpAffectionFlag(AstralSorcery.key("constellation_effect_" + constellationName));
    }

    @Nonnull
    public IWeakConstellation getConstellation() {
        return cst;
    }

    @Nonnull
    public ILocatable getPos() {
        return pos;
    }

    public void clearCache() {}

    public void readFromNBT(CompoundTag cmp) {}

    public void writeToNBT(CompoundTag cmp) {}

    @Nullable
    public Player getOwningPlayerInWorld(Level world, BlockPos pos) {
        RitualPedestalBlockEntity pedestal = getPedestal(world, pos);
        if (pedestal != null) {
            return pedestal.getOwner();
        }
        return null;
    }

    public void sendConstellationPing(Level world, Vector3 at) {
        sendConstellationPing(world, at, this.getConstellation());
    }

    public static void sendConstellationPing(Level world, Vector3 at, IConstellation cst) {
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.CONSTELLATION_EFFECT_PING)
                .addData(buf -> {
                    ByteBufUtils.writeVector(buf, at);
                    //1.16.5 wrote the constellation with ByteBufUtils#writeRegistryEntry, which serialized
                    //`entry.getRegistryName()` plus the owning forge registry's name and looked the value back up
                    //through RegistryManager.ACTIVE. Forge deleted IForgeRegistryEntry in 1.19 and this tree keeps
                    //constellations in its own ConstellationRegistry instead of a forge registry, so the entry is
                    //written as its registry name and resolved through that registry in playConstellationPing below.
                    ByteBufUtils.writeResourceLocation(buf, cst.getRegistryName());
                });
        //1.16.5: PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(world, at.toBlockPos(), 32)),
        //where pointFromPos(World, Vector3i, double) truncated the position to block coordinates. The 1.20.1 helper
        //takes a Vec3 directly; passing the exact position only moves the range-check origin by <1 block.
        PacketChannel.sendToAllAround(pkt, world, at.toVector3d(), 32);
    }

    protected void markPlayerAffected(Player player) {
        //1.16.5: player.getEntityWorld().isRemote() -> 1.20.1: player.level().isClientSide()
        if (player.level().isClientSide()) {
            return;
        }
        PlayerAffectionFlags.markPlayerAffected(player, this.getPlayerAffectionFlag());
    }

    @OnlyIn(Dist.CLIENT)
    public static void playConstellationPing(PktPlayEffect pktPlayEffect) {
        Vector3 at = ByteBufUtils.readVector(pktPlayEffect.getExtraData());
        IConstellation cst = ConstellationRegistry.getConstellation(ByteBufUtils.readResourceLocation(pktPlayEffect.getExtraData()));
        //1.16.5's ByteBufUtils#readRegistryEntry could equally return null for an unknown id and would have thrown
        //below; the explicit guard only avoids a client crash on a desynced constellation id.
        if (cst == null) {
            return;
        }

        for (int i = 0; i < 6; i++) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at.clone().add(Vector3.random().multiply(0.25F)))
                    .setMotion(Vector3.random().multiply(0.015F))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(cst.getConstellationColor().brighter()))
                    .setScaleMultiplier(0.25F + rand.nextFloat() * 0.15F)
                    .setMaxAge(35 + rand.nextInt(20));
        }
    }

    public static abstract class Config extends ConfigEntry {

        private final boolean defaultEnabled = true;
        private final double  defaultRange;
        private final double  defaultRangePerLens;

        public ForgeConfigSpec.BooleanValue enabled;
        public ForgeConfigSpec.DoubleValue  range;
        public ForgeConfigSpec.DoubleValue  rangePerLens;

        public Config(String constellationName, double defaultRange, double defaultRangePerLens) {
            super(String.format("constellation.effect.%s", constellationName));
            this.defaultRange = defaultRange;
            this.defaultRangePerLens = defaultRangePerLens;
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            this.enabled = cfgBuilder
                    .comment("Set this to false to disable this ritual effect")
                    .translation(translationKey("enabled"))
                    .define("enabled", this.defaultEnabled);
            this.range = cfgBuilder
                    .comment("Defines the radius (in blocks) in which the ritual will do its effects.")
                    .translation(translationKey("range"))
                    .defineInRange("range", this.defaultRange, 1, 512);
            this.rangePerLens = cfgBuilder
                    .comment("Defines the increase in radius the ritual will get per active lens enhancing the ritual.")
                    .translation(translationKey("rangePerLens"))
                    .defineInRange("rangePerLens", this.defaultRangePerLens, 0, 128);
        }
    }
}
