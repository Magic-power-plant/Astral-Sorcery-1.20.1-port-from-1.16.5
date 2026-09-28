package hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.NBTHelper;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public class IndependentCrystalSource implements IIndependentStarlightSource {

    public static final double MIN_DST = 16D;

    private IWeakConstellation constellation = null;
    private CrystalAttributes crystalAttributes = null;
    private boolean doesSeeSky = false;
    private boolean doesAutoLink = false;
    private double collectionDstMultiplier = 1D;
    private BlockPos closestOtherCollector = null;
    private float posDistribution = -1F;
    private boolean enhanced = false;

    @Override
    public float produceStarlightTick(ServerLevel world, BlockPos pos) {
        if (!doesSeeSky || crystalAttributes == null) {
            return 0F;
        }
        IWeakConstellation cst = getStarlightType();
        WorldContext ctx = SkyHandler.getContext(world, LogicalSide.SERVER);
        if (ctx == null || cst == null) {
            return 0F;
        }

        if (posDistribution == -1F) {
            posDistribution = SkyCollectionHelper.getSkyNoiseDistribution(world, pos);
        }

        //1.16.5 IndependentCrystalSource#produceStarlightTick:78-87 - the visible arc between two collector
        //crystals that are close enough to share their collection radius. Without this send, closestOtherCollector
        //was computed, written to NBT and read back but never consumed by anything, and the LIGHTNING effect had
        //no sender from the starlight network at all.
        if (closestOtherCollector != null && this.constellation != null && rand.nextInt(40) == 0) {
            BlockPos otherCollector = this.closestOtherCollector;
            IWeakConstellation effectConstellation = this.constellation;
            PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIGHTNING)
                    .addData(buf -> {
                        ByteBufUtils.writeVector(buf, new Vector3(pos).add(0.5, 0.5, 0.5));
                        ByteBufUtils.writeVector(buf, new Vector3(otherCollector).add(0.5, 0.5, 0.5));
                        buf.writeInt(effectConstellation.getConstellationColor().darker().getRGB());
                    });
            //1.16.5: PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(world, pos, 32));
            //the 1.20.1 helper takes the level/position/range directly instead of a pre-built TargetPoint.
            PacketChannel.sendToAllAround(pkt, world, Vec3.atCenterOf(pos), 32);
        }

        Function<Float, Float> distrFunction = getDistributionFunc();
        float perc = CrystalCalculations.getCollectorCrystalCollectionRate(this);
        perc *= distrFunction.apply(0.3F + (0.7F * DayTimeHelper.getCurrentDaytimeDistribution(world)));
        perc *= collectionDstMultiplier;
        perc *= 1 + (0.3F * posDistribution);
        perc *= 0.4F + 0.6F * ctx.getDistributionHandler().getDistribution(cst);
        return perc;
    }

    private Function<Float, Float> getDistributionFunc() {
        if (enhanced) {
            return (in) -> 0.6F + (0.5F * in);
        } else {
            return (in) -> 0.2F + (0.8F * in);
        }
    }

    @Override
    @Nullable
    public IWeakConstellation getStarlightType() {
        return constellation;
    }

    @Override
    public boolean providesAutoLink() {
        return doesAutoLink;
    }

    @Override
    public <T extends BlockEntity> boolean updateFromTileEntity(T tile) {
        if (!(tile instanceof TileCollectorCrystal crystal)) {
            return true;
        }
        this.doesSeeSky = crystal.doesSeeSky();
        this.doesAutoLink = !crystal.isPlayerMade();
        this.enhanced = crystal.isEnhanced();
        this.constellation = crystal.getAttunedConstellation();
        this.crystalAttributes = crystal.getAttributes();
        return true;
    }

    @Override
    public void threadedUpdateProximity(BlockPos thisPos, Map<BlockPos, IIndependentStarlightSource> otherSources) {
        double minDstSq = Double.MAX_VALUE;
        BlockPos closest = null;
        for (BlockPos other : otherSources.keySet()) {
            if (other.equals(thisPos)) {
                continue;
            }
            double dstSq = Vec3.atCenterOf(thisPos).distanceToSqr(Vec3.atCenterOf(other));
            if (dstSq < minDstSq) {
                minDstSq = dstSq;
                closest = other;
            }
        }
        double dst = Math.sqrt(minDstSq);
        if (dst <= MIN_DST) {
            this.collectionDstMultiplier = dst / MIN_DST;
            this.closestOtherCollector = closest;
        } else {
            this.collectionDstMultiplier = 1D;
            this.closestOtherCollector = null;
        }
    }

    public CrystalAttributes getCrystalAttributes() {
        return crystalAttributes;
    }

    @Override
    public SourceClassRegistry.SourceProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        IConstellation cst = IConstellation.readFromNBT(compound, "constellation");
        this.constellation = cst instanceof IWeakConstellation weak ? weak : null;
        this.crystalAttributes = CrystalAttributes.getCrystalAttributes(compound);
        this.doesSeeSky = compound.getBoolean("doesSeeSky");
        this.doesAutoLink = compound.getBoolean("doesAutoLink");
        this.collectionDstMultiplier = compound.contains("collectionDstMultiplier") ? compound.getDouble("collectionDstMultiplier") : 1D;
        this.closestOtherCollector = compound.contains("closestOtherCollector") ? NBTHelper.readBlockPosFromNBT(compound.getCompound("closestOtherCollector")) : null;
        this.posDistribution = compound.contains("posDistribution") ? compound.getFloat("posDistribution") : -1F;
        this.enhanced = compound.getBoolean("enhanced");
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        if (crystalAttributes != null) {
            crystalAttributes.store(compound);
        }
        if (constellation != null) {
            constellation.writeToNBT(compound, "constellation");
        }
        compound.putBoolean("doesSeeSky", doesSeeSky);
        compound.putBoolean("doesAutoLink", doesAutoLink);
        compound.putDouble("collectionDstMultiplier", collectionDstMultiplier);
        if (closestOtherCollector != null) {
            compound.put("closestOtherCollector", NBTHelper.writeBlockPosToNBT(closestOtherCollector, new CompoundTag()));
        }
        compound.putFloat("posDistribution", posDistribution);
        compound.putBoolean("enhanced", enhanced);
    }

    public static class Provider implements SourceClassRegistry.SourceProvider {
        @Override
        public IIndependentStarlightSource provideEmptySource() {
            return new IndependentCrystalSource();
        }

        @Override
        @Nonnull
        public ResourceLocation getIdentifier() {
            return AstralSorcery.locate("independent_crystal_source");
        }
    }
}
