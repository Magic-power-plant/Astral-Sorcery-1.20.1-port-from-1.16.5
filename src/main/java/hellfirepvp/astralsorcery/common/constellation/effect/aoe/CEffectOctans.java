/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import com.mojang.datafixers.util.Either;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectOctans
 * Created by HellFirePvP
 * Date: 01.02.2020 / 15:45
 */
public class CEffectOctans extends CEffectAbstractList<ListEntries.CounterMaxEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("octans");
    public static OctansConfig CONFIG = new OctansConfig();

    private static boolean corruptedSkipWaterCheck = false;

    public CEffectOctans(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.octans, CONFIG.maxAmount.get(), (world, pos, state) -> {
            if (!corruptedSkipWaterCheck) {
                //1.16.5: World#getHeight(Heightmap.Type, BlockPos) + BlockPos#down() ->
                //1.20.1: LevelReader#getHeightmapPos(Heightmap.Types, BlockPos) + BlockPos#below()
                pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).below();
            }
            //1.16.5 tested state.getMaterial() == Material.WATER. Materials were removed in 1.20, so the
            //block's fluid is asked for directly; LiquidBlock.LEVEL == 0 is still the source-block test.
            return corruptedSkipWaterCheck || (
                    world.isEmptyBlock(pos.above()) &&
                            (state.getBlock() instanceof LiquidBlock &&
                                    state.getFluidState().is(Fluids.WATER) &&
                                    state.getValue(LiquidBlock.LEVEL) == 0) ||
                            state.getBlock() instanceof BubbleColumnBlock
                    );
        });
        this.excludeRitualColumn();
    }

    @Nullable
    @Override
    public ListEntries.CounterMaxEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return new ListEntries.CounterMaxEntry(pos, 1);
    }

    @Nullable
    @Override
    public ListEntries.CounterMaxEntry createElement(Level world, BlockPos pos) {
        pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).below();
        return new ListEntries.CounterMaxEntry(pos, 1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level world, BlockPos pos, RitualPedestalBlockEntity pedestal, float alphaMultiplier, boolean extended) {
        ConstellationEffectProperties prop = this.createProperties(pedestal.getMirrorCount());

        Vector3 at = new Vector3(pos).add(0.5, 0.5, 0.5);
        at.addY(prop.getSize() * 0.75F);
        for (int i = 0; i < Math.max(1, prop.getSize() / 6); i++) {
            Vector3 vec = at.clone().add(Vector3.random().setY(0).multiply(rand.nextFloat() * prop.getSize()));

            Color c = MiscUtils.eitherOf(rand,
                    () -> ColorsAS.CONSTELLATION_OCTANS,
                    () -> ColorsAS.CONSTELLATION_OCTANS.darker());
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(vec)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .color(VFXColorFunction.constant(c))
                    .setScaleMultiplier(0.6F + rand.nextFloat() * 0.3F)
                    .setGravityStrength(0.0004F + rand.nextFloat() * 0.0008F)
                    .setMaxAge(100 + rand.nextInt(60));
        }
    }

    @Override
    public boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(world instanceof ServerLevel)) {
            return false;
        }

        boolean update = false;
        if (properties.isCorrupted()) {
            corruptedSkipWaterCheck = true;
            Either<ListEntries.CounterMaxEntry, BlockPos> newEntry = this.peekNewPosition(world, pos, properties);
            corruptedSkipWaterCheck = false;
            return newEntry.mapLeft(entry -> {
                BlockState state = world.getBlockState(entry.getPos());
                BlockPos offset = entry.getPos().subtract(pos);
                if (world.isEmptyBlock(entry.getPos()) &&
                        (this.isLinkedRitual || Math.abs(offset.getX()) > 5 || Math.abs(offset.getZ()) > 5 || offset.getY() < 0)) {
                    //1.16.5: World#getDimensionType().isUltrawarm() -> 1.20.1: Level#dimensionType().ultraWarm();
                    //World#setBlockState(pos, state) defaulted to flag 3 (= Block.UPDATE_ALL).
                    if (!world.dimensionType().ultraWarm()) {
                        if (world.setBlock(entry.getPos(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL)) {
                            for (int i = 0; i < 3; i++) {
                                spawnFishingDropsAt((ServerLevel) world, entry.getPos());
                            }
                            world.neighborChanged(entry.getPos(), Blocks.WATER, entry.getPos());
                        }
                    }
                } else if (BlockUtils.isFluidBlock(state)) {
                    if (state.getBlock() == Blocks.WATER) {
                        if (rand.nextInt(100) == 0) {
                            spawnFishingDropsAt((ServerLevel) world, entry.getPos());
                        }
                    } else {
                        world.setBlock(entry.getPos(), Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
                    }
                } else if (state.getBlock() instanceof BubbleColumnBlock) {
                    if (rand.nextInt(70) == 0) {
                        spawnFishingDropsAt((ServerLevel) world, entry.getPos());
                    }
                }
                return true;
            }).left().orElse(false);
        }

        ListEntries.CounterMaxEntry entry = getRandomElementChanced();
        if (entry != null) {
            if (MiscUtils.canEntityTickAt(world, entry.getPos())) {
                if (!isValid(world, entry)) {
                    removeElement(entry);
                } else {
                    sendConstellationPing(world, new Vector3(entry.getPos()).add(0.5, 1, 0.5));
                    int count = entry.getCounter();
                    count++;
                    entry.setCounter(count);

                    if (count >= entry.getMaxCount()) {
                        int min = Math.min(CONFIG.minFishTickTime.get(), CONFIG.maxFishTickTime.get());
                        int max = Math.max(CONFIG.minFishTickTime.get(), CONFIG.maxFishTickTime.get());

                        int diff = Math.max(1, max - min + 1);
                        entry.setMaxCount(min + rand.nextInt(diff));
                        entry.setCounter(0);

                        spawnFishingDropsAt((ServerLevel) world, entry.getPos());
                    }
                }
                update = true;
            }
        }

        if (findNewPosition(world, pos, properties)
                .ifRight(attemptedPos -> sendConstellationPing(world, new Vector3(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, attemptedPos).below()).add(0.5, 0.5, 0.5)))
                .left().isPresent()) {
            update = true;
        }
        return update;
    }

    private void spawnFishingDropsAt(ServerLevel world, BlockPos pos) {
        Vector3 dropLoc = new Vector3(pos).add(0.5, 0.85, 0.5);
        ItemStack tool = new ItemStack(Items.FISHING_ROD);
        //1.16.5: ItemStack#addEnchantment + Enchantments.LUCK_OF_THE_SEA -> ItemStack#enchant + Enchantments.FISHING_LUCK
        tool.enchant(Enchantments.FISHING_LUCK, 2);

        //1.16.5: LootTables.GAMEPLAY_FISHING_FISH / _TREASURE -> BuiltInLootTables.FISHING_FISH / FISHING_TREASURE
        ResourceLocation fromTable = BuiltInLootTables.FISHING_FISH;
        if (rand.nextFloat() < 0.1F) {
            fromTable = BuiltInLootTables.FISHING_TREASURE;
        }

        //1.16.5 built a LootContext directly (LootContext.Builder#withRandom(rand), #build(LootParameterSets.FISHING)).
        //1.20.1 split that into LootParams (the context parameters) and the random source, which is now passed as a
        //seed to LootTable#getRandomItems - hence rand.nextLong() in place of withRandom(rand).
        //LootParameters.field_237457_g_ is LootContextParams.ORIGIN; Vector3d#copyCentered -> Vec3#atCenterOf.
        LootParams.Builder builder = new LootParams.Builder(world);
        builder.withLuck(rand.nextInt(2) * rand.nextFloat());
        builder.withParameter(LootContextParams.TOOL, tool);
        builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
        //1.16.5: world.getServer().getLootTableManager().getLootTableFromLocation(fromTable)
        LootTable lootTable = world.getServer().getLootData().getLootTable(fromTable);
        for (ItemStack loot : lootTable.getRandomItems(builder.create(LootContextParamSets.FISHING), rand.nextLong())) {
            ItemEntity ei = ItemUtils.dropItemNaturally(world, dropLoc.getX(), dropLoc.getY(), dropLoc.getZ(), loot);
            //1.16.5: Entity#getMotion/#setMotion -> 1.20.1: Entity#getDeltaMovement/#setDeltaMovement
            Vector3 motion = new Vector3(ei.getDeltaMovement());
            motion.setY(Math.abs(motion.getY()));
            ei.setDeltaMovement(motion.toVector3d());
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

    private static class OctansConfig extends CountConfig {

        private final int defaultMinFishTickTime = 20;
        private final int defaultMaxFishTickTime = 60;

        public ForgeConfigSpec.IntValue minFishTickTime;
        public ForgeConfigSpec.IntValue maxFishTickTime;

        public OctansConfig() {
            super("octans", 8D, 1D, 64);
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.minFishTickTime = cfgBuilder
                    .comment("Defines the minimum default tick-time until a fish may be fished by the ritual. Gets reduced internally the more starlight was provided at the ritual.")
                    .translation(translationKey("minFishTickTime"))
                    .defineInRange("minFishTickTime", this.defaultMinFishTickTime, 5, Integer.MAX_VALUE);

            this.maxFishTickTime = cfgBuilder
                    .comment("Defines the maximum default tick-time until a fish may be fished by the ritual. Gets reduced internally the more starlight was provided at the ritual. Has to be bigger as the minimum time; if it isn't it'll be set to the minimum.")
                    .translation(translationKey("maxFishTickTime"))
                    .defineInRange("maxFishTickTime", this.defaultMaxFishTickTime, 10, Integer.MAX_VALUE);
        }
    }
}
