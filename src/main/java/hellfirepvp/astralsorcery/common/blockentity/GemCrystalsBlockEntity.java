package hellfirepvp.astralsorcery.common.blockentity;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GemCrystalsBlockEntity extends TickableBlockEntity {

    public static final int TICK_GROWTH_CHANCE = 10_000;

    public GemCrystalsBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.GEM_CRYSTAL_CLUSTER.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level != null && !level.isClientSide()) {
            if (getGrowth().getGrowthStage() < 2 && doesSeeSky()) {
                this.tryGrowWithChance(TICK_GROWTH_CHANCE);
            } else if (getGrowth().getGrowthStage() == 2) {
                if (rand.nextInt(4000) == 0) {
                    this.setGrowth(getGrowth().shrink());
                }
            }
        } else if (level != null) {
            if (getGrowth().getGrowthStage() == 2) {
                playHarvestEffects();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playHarvestEffects() {
        Vector3 pos = new Vector3(this)
                .add(0.5, 0.5, 0.5)
                .add(this.getBlockState().getOffset(level, getBlockPos()));
        MiscUtils.applyRandomOffset(pos, rand, 0.5F);

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(pos)
                .color(VFXColorFunction.constant(getGrowth().getDisplayColor()))
                .setScaleMultiplier(0.1F + rand.nextFloat() * 0.05F)
                .setMaxAge(15 + rand.nextInt(5));
    }

    public void tryGrowWithChance(int growthChance) {
        float distribution = DayTimeHelper.getCurrentDaytimeDistribution(level);
        growthChance *= (1F - (0.2F * distribution));

        this.grow(growthChance);
    }

    public void grow(int chance) {
        if (rand.nextInt(Math.max(chance, 1)) == 0) {
            setGrowth(getGrowth().grow(level));
        }
    }

    public BlockGemCrystalCluster.GrowthStageType getGrowth() {
        if (level == null) return BlockGemCrystalCluster.GrowthStageType.STAGE_0;
        BlockState current = level.getBlockState(getBlockPos());
        return current.getValue(BlockGemCrystalCluster.STAGE);
    }

    public void setGrowth(BlockGemCrystalCluster.GrowthStageType stage) {
        if (level == null) return;
        BlockState next = BlocksAS.GEM_CRYSTAL_CLUSTER.get().defaultBlockState()
                .setValue(BlockGemCrystalCluster.STAGE, stage);
        level.setBlockAndUpdate(getBlockPos(), next);
    }
}
