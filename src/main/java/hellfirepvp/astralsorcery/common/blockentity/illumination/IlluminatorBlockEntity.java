package hellfirepvp.astralsorcery.common.blockentity.illumination;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.BlockFlareLight;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.color.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IlluminatorBlockEntity extends TickableBlockEntity {

    public static final LightCheck ILLUMINATOR_CHECK = new LightCheck();

    public static final int SEARCH_RADIUS = 64;
    public static final int STEP_WIDTH = 2;

    private List<List<BlockPos>> layerPositions = null;
    private boolean doRecalculation = false;
    private int ticksUntilNextPlacement = 180;
    private boolean playerPlaced = false;
    private int boostedTicks = 0;

    private DyeColor color = DyeColor.YELLOW;

    public IlluminatorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.ILLUMINATOR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isPlayerPlaced()) {
            return;
        }

        if (!level.isClientSide()) {
            if (layerPositions == null) {
                recalculate();
            }
            placeFlare();
            placeFlare();
            placeFlare();
            if (rand.nextInt(3) == 0 && placeFlare()) {
                doRecalculation = true;
            }
            if (boostedTicks > 0) {
                boostedTicks--;
            }
            ticksUntilNextPlacement--;
            if (ticksUntilNextPlacement <= 0) {
                ticksUntilNextPlacement = boostedTicks > 0 ? 30 : 180;
                if (doRecalculation) {
                    doRecalculation = false;
                    recalculate();
                }
            }
        }

        if (level.isClientSide()) {
            this.tickEffects();
        }
    }

    private void recalculate() {
        int height = Math.max(0, getBlockPos().getY() - 7);
        int parts = height / 7;
        layerPositions = new ArrayList<>(parts);
        for (int i = 0; i < parts; i++) {
            int yPart = 3 + i * 7;
            List<BlockPos> positions = new ArrayList<>();
            generatePositions(positions, new BlockPos(getBlockPos().getX(), yPart, getBlockPos().getZ()));
            layerPositions.add(positions);
        }
    }

    private void generatePositions(List<BlockPos> positions, BlockPos center) {
        int xPos = center.getX();
        int yPos = center.getY();
        int zPos = center.getZ();
        BlockPos currentPos = center;
        if (!positions.contains(currentPos)) {
            positions.add(currentPos);
        }

        Direction dir = Direction.NORTH;
        while (Math.abs(currentPos.getX() - xPos) <= SEARCH_RADIUS &&
                Math.abs(currentPos.getY() - yPos) <= SEARCH_RADIUS &&
                Math.abs(currentPos.getZ() - zPos) <= SEARCH_RADIUS) {
            currentPos = currentPos.relative(dir, STEP_WIDTH);
            if (!positions.contains(currentPos)) {
                positions.add(currentPos);
            }
            Direction tryDirNext = dir.getClockWise();
            if (!positions.contains(currentPos.relative(tryDirNext, STEP_WIDTH))) {
                dir = tryDirNext;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffects() {
        if (!this.doesSeeSky() && this.boostedTicks <= 0) {
            return;
        }
        FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(this).add(0.5, 0.5, 0.5))
                .setScaleMultiplier(0.25F)
                .setMotion(new Vector3(rand.nextFloat() * 0.025F * (rand.nextBoolean() ? 1 : -1),
                        rand.nextFloat() * 0.025F * (rand.nextBoolean() ? 1 : -1),
                        rand.nextFloat() * 0.025F * (rand.nextBoolean() ? 1 : -1)));
        Color c = ColorUtils.flareColorFromDye(this.getColor());
        p.color(VFXColorFunction.constant(MiscUtils.eitherOf(rand,
                Color.WHITE, c.brighter().brighter(), c)));

        if (this.boostedTicks > 0) {
            if (this.ticksExisted % 4 == 0) {
                Collection<Vector3> positions = MiscUtils.getCirclePositions(
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        Vector3.RotAxis.Y_AXIS, 0.8F + rand.nextFloat() * 0.1F, 20 + rand.nextInt(10));

                for (Vector3 v : positions) {
                    p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .spawn(v)
                            .setScaleMultiplier(0.15F)
                            .setMotion(new Vector3(0, (rand.nextBoolean() ? 1 : -1) * rand.nextFloat() * 0.01, 0));
                    p.color(VFXColorFunction.constant(MiscUtils.eitherOf(rand,
                            Color.WHITE, c.brighter().brighter(), c)));
                }
            }
        }
    }

    private boolean placeFlare() {
        boolean recalc = false;
        for (List<BlockPos> list : layerPositions) {
            if (list.isEmpty()) {
                recalc = true;
                continue;
            }

            int index = rand.nextInt(list.size());
            BlockPos at = list.remove(index);
            if (!recalc && list.isEmpty()) {
                recalc = true;
            }
            at = at.offset(rand.nextInt(5) - 2, rand.nextInt(13) - 6, rand.nextInt(5) - 2);
            BlockPos finalAt = at;
            MiscUtils.executeWithChunk(level, at, at, (pos) -> {
                if (IlluminatorBlockEntity.this.doesSeeSky() && IlluminatorBlockEntity.ILLUMINATOR_CHECK.test(level, finalAt, level.getBlockState(finalAt))) {
                    DyeColor dyeColor = IlluminatorBlockEntity.this.getColor();
                    BlockState toPlace = BlocksAS.FLARE_LIGHT.get().defaultBlockState().setValue(BlockFlareLight.COLOR, dyeColor);
                    if (level.setBlock(finalAt, toPlace, 3)) {
                        EntityFlare.spawnAmbientFlare(level, IlluminatorBlockEntity.this.getBlockPos());
                    }
                }
            });
        }
        return recalc;
    }

    public void setColor(DyeColor color) {
        this.color = color;
        this.markForUpdate();
    }

    public DyeColor getColor() {
        return color;
    }

    public void setPlayerPlaced(boolean playerPlaced) {
        this.playerPlaced = playerPlaced;
        this.markForUpdate();
    }

    public boolean isPlayerPlaced() {
        return playerPlaced;
    }

    public void onWandUsed(ItemStack stack) {
        this.boostedTicks = 10 * 60 * 20;
        this.setColor(ItemIlluminationWand.getConfiguredColor(stack));
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.putBoolean("playerPlaced", this.playerPlaced);
        compound.putInt("color", this.color.getId());
        compound.putInt("boostedTicks", this.boostedTicks);
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.playerPlaced = compound.getBoolean("playerPlaced");
        this.color = DyeColor.byId(compound.getInt("color"));
        this.boostedTicks = compound.getInt("boostedTicks");
    }

    public static class LightCheck implements BlockPredicate {

        @Override
        public boolean test(Level level, BlockPos pos, BlockState state) {
            return level.isEmptyBlock(pos) &&
                    !MiscUtils.canSeeSky(level, pos, false, false) &&
                    level.getBrightness(LightLayer.BLOCK, pos) < 8 &&
                    level.getBrightness(LightLayer.SKY, pos) < 4;
        }

    }
}
