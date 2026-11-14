package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class CropHelper {

    public static final String GROWABLE = "growable";
    public static final String GROWABLE_CROP = "growable_crop";
    public static final String GROWABLE_REED = "growable_reed";
    public static final String GROWABLE_CACTUS = "growable_cactus";
    public static final String GROWABLE_NETHERWART = "growable_netherwart";
    public static final String HARVESTABLE = "harvestable";

    public static Map<String, Function<BlockPos, GrowablePlant>> growableFactoryWrapper = new HashMap<>() {{
        put(GROWABLE, GrowableWrapper::new);
        put(GROWABLE_CROP, GrowableCropWrapper::new);
        put(GROWABLE_REED, GrowableReedWrapper::new);
        put(GROWABLE_CACTUS, GrowableCactusWrapper::new);
        put(GROWABLE_NETHERWART, GrowableNetherwartWrapper::new);
        put(HARVESTABLE, HarvestableWrapper::new);
    }};

    @Nullable
    public static GrowablePlant fromNBT(CompoundTag nbt, BlockPos pos) {
        return growableFactoryWrapper.getOrDefault(nbt.getString("identifier"), (p) -> null).apply(pos);
    }

    @Nullable
    public static GrowablePlant wrapPlant(LevelAccessor world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof CropBlock) {
            return new GrowableCropWrapper(pos);
        }
        if (b instanceof BonemealableBlock) {
            if (b instanceof GrassBlock) return null;
            if (b instanceof TallGrassBlock) return null;
            if (b instanceof DoublePlantBlock) return null;
            return new GrowableWrapper(pos);
        }
        if (b instanceof SugarCaneBlock) {
            if (isReedBase(world, pos)) {
                return new GrowableReedWrapper(pos);
            }
        }
        if (b instanceof CactusBlock) {
            if (isCactusBase(world, pos)) {
                return new GrowableCactusWrapper(pos);
            }
        }
        if (b instanceof NetherWartBlock) {
            return new GrowableNetherwartWrapper(pos);
        }
        return null;
    }

    @Nullable
    public static HarvestablePlant wrapHarvestablePlant(LevelAccessor world, BlockPos pos) {
        GrowablePlant growable = wrapPlant(world, pos);
        if (growable == null) return null;
        Block block = world.getBlockState(growable.getPos()).getBlock();
        if (growable instanceof GrowableCropWrapper) {
            return (GrowableCropWrapper) growable;
        }
        if (block instanceof SugarCaneBlock && growable instanceof GrowableReedWrapper) {
            return (GrowableReedWrapper) growable;
        }
        if (block instanceof CactusBlock && growable instanceof GrowableCactusWrapper) {
            return (GrowableCactusWrapper) growable;
        }
        if (block instanceof NetherWartBlock && growable instanceof GrowableNetherwartWrapper) {
            return (GrowableNetherwartWrapper) growable;
        }
        if (block instanceof IPlantable) {
            return new HarvestableWrapper(pos);
        }
        return null;
    }

    private static boolean isReedBase(BlockGetter world, BlockPos pos) {
        return !world.getBlockState(pos.below()).getBlock().equals(Blocks.SUGAR_CANE);
    }

    private static boolean isCactusBase(BlockGetter world, BlockPos pos) {
        return !world.getBlockState(pos.below()).getBlock().equals(Blocks.CACTUS);
    }

    public static interface GrowablePlant extends CEffectAbstractList.ListEntry {

        String getIdentifier();

        boolean isValid(LevelAccessor world);

        boolean canGrow(LevelAccessor world);

        boolean tryGrow(LevelAccessor world, Random rand);

        @Override
        default void readFromNBT(CompoundTag nbt) {}

        @Override
        default void writeToNBT(CompoundTag nbt) {
            nbt.putString("identifier", this.getIdentifier());
        }
    }

    public static interface HarvestablePlant extends GrowablePlant {

        boolean canHarvest(LevelAccessor world);

        java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune);
    }

    public static class HarvestableWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public HarvestableWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor world) {
            BlockState at = world.getBlockState(pos);
            if (!(at.getBlock() instanceof BonemealableBlock)) return false;
            if (at.getBlock() instanceof StemBlock) return false;
            return !((BonemealableBlock) at.getBlock()).isValidBonemealTarget((LevelReader) world, pos, at, false);
        }

        @Override
        public java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune) {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            if (canHarvest(world)) {
                BlockPos pos = getPos();
                BlockState at = world.getBlockState(getPos());
                if (at.getBlock() instanceof IPlantable) {
                    drops.addAll(BlockUtils.getDrops(world, pos, harvestFortune, rand));
                    world.setBlock(pos, ((IPlantable) at.getBlock()).getPlant(world, pos), 3);
                }
            }
            return drops;
        }

        @Override
        public String getIdentifier() {
            return HARVESTABLE;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return wrapHarvestablePlant(world, getPos()) instanceof HarvestableWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockState at = world.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget((LevelReader) world, pos, at, false)) {
                    return true;
                }
                if (at.getBlock() instanceof StemBlock) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            if (!(world instanceof ServerLevel serverWorld)) {
                return false;
            }
            BlockState at = world.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget((LevelReader) world, pos, at, false)) {
                    growable.performBonemeal(serverWorld, net.minecraft.util.RandomSource.create(rand.nextLong()), pos, at);
                    return true;
                }
                if (at.getBlock() instanceof StemBlock && rand.nextInt(4) == 0) {
                    at.randomTick(serverWorld, pos, serverWorld.random);
                }
            }
            return false;
        }
    }

    public static class GrowableNetherwartWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableNetherwartWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return world.getBlockState(pos).getBlock() instanceof NetherWartBlock;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockState at = world.getBlockState(pos);
            return at.getBlock() instanceof NetherWartBlock && at.getValue(NetherWartBlock.AGE) < 3;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            if (rand.nextBoolean()) {
                BlockState current = world.getBlockState(pos);
                return world.setBlock(pos, current.setValue(NetherWartBlock.AGE, Math.min(3, current.getValue(NetherWartBlock.AGE) + 1)), 3);
            }
            return false;
        }

        @Override
        public boolean canHarvest(LevelAccessor world) {
            BlockState current = world.getBlockState(pos);
            return current.getBlock() instanceof NetherWartBlock && current.getValue(NetherWartBlock.AGE) >= 3;
        }

        @Override
        public java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune) {
            java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
            stacks.addAll(BlockUtils.getDrops(world, pos, harvestFortune, rand));
            world.setBlock(pos, Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 0), 3);
            return stacks;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_NETHERWART;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }
    }

    public static class GrowableCactusWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableCactusWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor world) {
            return world.getBlockState(pos.above()).getBlock() instanceof CactusBlock;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return world.getBlockState(pos).getBlock() instanceof CactusBlock;
        }

        @Override
        public java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune) {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            for (int i = 2; i > 0; i--) {
                BlockPos bp = pos.above(i);
                BlockState at = world.getBlockState(bp);
                if (at.getBlock() instanceof CactusBlock) {
                    drops.addAll(BlockUtils.getDrops(world, pos, harvestFortune, rand));
                    world.removeBlock(bp, false);
                }
            }
            return drops;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = world.getBlockState(cache);
                if (upState.isAir()) {
                    return true;
                } else if (!(upState.getBlock() instanceof CactusBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = world.getBlockState(cache);
                if (upState.isAir()) {
                    if (rand.nextBoolean()) {
                        return world.setBlock(cache, Blocks.CACTUS.defaultBlockState(), 3);
                    } else {
                        return false;
                    }
                } else if (!(upState.getBlock() instanceof CactusBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_CACTUS;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }
    }

    public static class GrowableReedWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableReedWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean canHarvest(LevelAccessor world) {
            return world.getBlockState(pos.above()).getBlock() instanceof SugarCaneBlock;
        }

        @Override
        public java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune) {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            for (int i = 2; i > 0; i--) {
                BlockPos bp = pos.above(i);
                BlockState at = world.getBlockState(bp);
                if (at.getBlock() instanceof SugarCaneBlock) {
                    drops.addAll(BlockUtils.getDrops(world, pos, harvestFortune, rand));
                    world.removeBlock(bp, false);
                }
            }
            return drops;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return world.getBlockState(pos).getBlock() instanceof SugarCaneBlock;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = world.getBlockState(cache);
                if (upState.isAir()) {
                    return true;
                } else if (!(upState.getBlock() instanceof SugarCaneBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            BlockPos cache = pos;
            for (int i = 1; i < 3; i++) {
                cache = cache.above();
                BlockState upState = world.getBlockState(cache);
                if (upState.isAir()) {
                    if (rand.nextBoolean()) {
                        return world.setBlock(cache, Blocks.SUGAR_CANE.defaultBlockState(), 3);
                    } else {
                        return false;
                    }
                } else if (!(upState.getBlock() instanceof SugarCaneBlock)) {
                    return false;
                }
            }
            return false;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_REED;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }
    }

    public static class GrowableCropWrapper implements HarvestablePlant {

        private final BlockPos pos;

        public GrowableCropWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return wrapPlant(world, this.pos) instanceof GrowableCropWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockState state = world.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock crop) {
                return !crop.isMaxAge(state);
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            BlockState state = world.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock crop) {
                if (!crop.isMaxAge(state)) {
                    int age = crop.getAge(state);
                    int next = Math.min(age + 1, crop.getMaxAge());
                    return world.setBlock(pos, crop.getStateForAge(next), 3);
                }
            }
            return false;
        }

        @Override
        public boolean canHarvest(LevelAccessor world) {
            BlockState state = world.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock crop) {
                return crop.isMaxAge(state);
            }
            return false;
        }

        @Override
        public java.util.List<ItemStack> harvestDropsAndReplant(ServerLevel world, Random rand, int harvestFortune) {
            java.util.List<ItemStack> drops = new java.util.ArrayList<>();
            BlockState state = world.getBlockState(this.pos);
            if (state.getBlock() instanceof CropBlock crop) {
                drops.addAll(BlockUtils.getDrops(world, pos, harvestFortune, rand));
                world.setBlock(pos, crop.getStateForAge(0), 3);
            }
            return drops;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE_CROP;
        }

        @Override
        public BlockPos getPos() {
            return this.pos;
        }
    }

    public static class GrowableWrapper implements GrowablePlant {

        private final BlockPos pos;

        public GrowableWrapper(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public String getIdentifier() {
            return GROWABLE;
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }

        @Override
        public boolean isValid(LevelAccessor world) {
            return wrapPlant(world, pos) instanceof GrowableWrapper;
        }

        @Override
        public boolean canGrow(LevelAccessor world) {
            BlockState at = world.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock growable) {
                if (growable.isValidBonemealTarget((LevelReader) world, pos, at, false)) {
                    return true;
                }
                if (at.getBlock() instanceof StemBlock) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean tryGrow(LevelAccessor world, Random rand) {
            BlockState at = world.getBlockState(pos);
            if (at.getBlock() instanceof BonemealableBlock growable && world instanceof ServerLevel serverWorld) {
                if (growable.isValidBonemealTarget((LevelReader) world, pos, at, false)) {
                    if (!growable.isBonemealSuccess(serverWorld, serverWorld.random, pos, at)) {
                        if (rand.nextInt(20) != 0) {
                            return true;
                        }
                    }
                    growable.performBonemeal(serverWorld, serverWorld.random, pos, at);
                    return true;
                }
                if (at.getBlock() instanceof StemBlock) {
                    at.randomTick(serverWorld, pos, serverWorld.random);
                    return true;
                }
            }
            return false;
        }
    }

}
