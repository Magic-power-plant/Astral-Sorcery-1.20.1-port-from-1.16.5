package hellfirepvp.astralsorcery.common.world.marker;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.CrystalPropertiesAS;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.blockentity.CollectorCrystalBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MarkerManagerAS {

    public static void handleMarker(String marker, BlockPos pos, LevelAccessor genWorld, RandomSource rand, BoundingBox box) {
        switch (marker) {
            case "brick_shrine_chest":
                if (rand.nextBoolean()) {
                    makeChest(genWorld, pos, LootAS.SHRINE_CHEST, rand, box);
                } else {
                    genWorld.setBlock(pos, BlocksAS.MARBLE_BRICKS.get().defaultBlockState(), 3);
                }
                break;
            case "shrine_chest":
                if (rand.nextBoolean()) {
                    makeChest(genWorld, pos, LootAS.SHRINE_CHEST, rand, box);
                } else {
                    genWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                break;
            case "random_top_block":
                if (rand.nextFloat() < 0.7F) {
                    genWorld.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                } else {
                    genWorld.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                break;
            case "crystal":
                makeCollectorCrystal(genWorld, pos, rand, box);
                break;
        }
    }

    private static void makeCollectorCrystal(LevelAccessor world, BlockPos pos, RandomSource rand, BoundingBox box) {
        if (box.isInside(pos) && world.getBlockState(pos).getBlock() != BlocksAS.ROCK_COLLECTOR_CRYSTAL.get()) {
            world.setBlock(pos, BlocksAS.ROCK_COLLECTOR_CRYSTAL.get().defaultBlockState(), 3);

            CollectorCrystalBlockEntity tcc = MiscUtils.getBlockEntityAt(world, pos, CollectorCrystalBlockEntity.class, true);
            if (tcc != null) {
                IMajorConstellation cst = MiscUtils.getRandomEntry(ConstellationRegistry.getMajorConstellations(), rand);
                tcc.setAttributes(CrystalPropertiesAS.WORLDGEN_SHRINE_COLLECTOR_ATTRIBUTES);
                tcc.setAttunedConstellation(cst);
            }
        }
    }

    private static void makeChest(LevelAccessor world, BlockPos pos, ResourceLocation tableName, RandomSource rand, BoundingBox box) {
        if (box.isInside(pos) && world.getBlockState(pos).getBlock() != Blocks.CHEST) {
            BlockState chest = Blocks.CHEST.defaultBlockState();

            world.setBlock(pos, chest, 3);
            if (world instanceof net.minecraft.world.level.Level) {
                RandomizableContainerBlockEntity.setLootTable((net.minecraft.world.level.Level) world, rand, pos, tableName);
            }
        }
    }
}
