package hellfirepvp.astralsorcery.common.starlight.network.handler;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.starlight.network.StarlightNetworkRegistry;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockTransmutationHandler
 * Created by HellFirePvP
 * Date: 13.10.2019 / 10:41
 */
public class BlockTransmutationHandler implements StarlightNetworkRegistry.IStarlightBlockHandler {

    private static final Map<TransmutationKey, ActiveTransmutation> RUNNING_TRANSMUTATIONS = new HashMap<>();

    @Nullable
    public static BlockTransmutation findRecipe(Level level, BlockPos pos, BlockState state, @Nullable IWeakConstellation starlightType) {
        for (BlockTransmutation recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypesAS.BLOCK_TRANSMUTATION.get())) {
            if (recipe.matches(state, starlightType)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    public boolean isApplicable(Level level, BlockPos pos, BlockState state, @Nullable IWeakConstellation starlightType) {
        return findRecipe(level, pos, state, starlightType) != null;
    }

    @Override
    public void receiveStarlight(Level level, Random rand, BlockPos pos, BlockState state, @Nullable IWeakConstellation starlightType, double amount) {
        receiveStarlight(level, pos, state, starlightType, amount);
    }

    public static boolean receiveStarlight(Level level, BlockPos pos, BlockState state, @Nullable IWeakConstellation starlightType, double amount) {
        if (level.isClientSide || amount <= 0) {
            return false;
        }
        BlockTransmutation recipe = findRecipe(level, pos, state, starlightType);
        if (recipe == null) {
            return false;
        }
        TransmutationKey key = new TransmutationKey(level, pos);
        ActiveTransmutation active = RUNNING_TRANSMUTATIONS.get(key);
        if (active == null || active.recipe != recipe) {
            active = new ActiveTransmutation(recipe);
            RUNNING_TRANSMUTATIONS.put(key, active);
        }
        active.acceptStarlight(amount);

        //1.16.5 BlockTransmutationHandler#receiveStarlight broadcasts a BLOCK_TRANSMUTATION_TICK effect
        //to everyone within 24 blocks on every charge tick.
        //1.16.5 PacketChannel.pointFromPos used the raw block coordinates (not the block centre), which is
        //1.20.1's Vec3#atLowerCornerOf.
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.BLOCK_TRANSMUTATION_TICK)
                .addData(buf -> ByteBufUtils.writePos(buf, pos));
        PacketChannel.sendToAllAround(pkt, level, Vec3.atLowerCornerOf(pos), 24);

        if (active.isFinished() && active.finish(level, pos)) {
            RUNNING_TRANSMUTATIONS.remove(key);
        }
        return true;
    }

    /**
     * Client-side counterpart of the BLOCK_TRANSMUTATION_TICK effect broadcast above.
     * Ported 1:1 from 1.16.5 BlockTransmutationHandler#playTransmutation.
     */
    @OnlyIn(Dist.CLIENT)
    public static void playTransmutation(PktPlayEffect effect) {
        Random rand = new Random();
        BlockPos pos = ByteBufUtils.readPos(effect.getExtraData());

        Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(pos).add(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()))
                .setAlphaMultiplier(1F)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL))
                .setScaleMultiplier(0.2F + rand.nextFloat() * 0.15F)
                .setGravityStrength(-0.0014F)
                .setMaxAge(40 + rand.nextInt(20));
    }

    public static boolean tryTransmuteImmediately(Level level, BlockPos pos, @Nullable IWeakConstellation starlightType) {
        BlockState state = level.getBlockState(pos);
        BlockTransmutation recipe = findRecipe(level, pos, state, starlightType);
        if (recipe == null) {
            return false;
        }
        return new ActiveTransmutation(recipe).finish(level, pos);
    }

    public static void clear(Level level, BlockPos pos) {
        RUNNING_TRANSMUTATIONS.remove(new TransmutationKey(level, pos));
    }

    private static class ActiveTransmutation {

        private static final int MS_THRESHOLD = 15_000;

        private final BlockTransmutation recipe;
        private long lastMillisecondStarlightReceived = System.currentTimeMillis();
        private double accumulatedStarlight = 0;

        private ActiveTransmutation(BlockTransmutation recipe) {
            this.recipe = recipe;
        }

        private void acceptStarlight(double amount) {
            long now = System.currentTimeMillis();
            if (now - this.lastMillisecondStarlightReceived >= MS_THRESHOLD) {
                this.accumulatedStarlight = 0;
            }
            this.accumulatedStarlight += amount;
            this.lastMillisecondStarlightReceived = now;
        }

        private boolean isFinished() {
            return this.accumulatedStarlight >= this.recipe.getStarlightRequired();
        }

        private boolean finish(Level level, BlockPos pos) {
            if (level.isClientSide) {
                return false;
            }
            BlockState output = this.recipe.getOutput();
            if (level.setBlock(pos, output, 3)) {
                ItemStack stack = this.recipe.getOutputDisplay();
                if (!stack.isEmpty()) {
                    level.players().stream()
                            .filter(player -> player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) <= 225)
                            .forEach(player -> ResearchManager.informCrafted(player, stack));
                }
                return true;
            }
            this.accumulatedStarlight *= 0.9F;
            return false;
        }
    }

    private record TransmutationKey(Level level, BlockPos pos) {
        private TransmutationKey(Level level, BlockPos pos) {
            this.level = level;
            this.pos = pos.immutable();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TransmutationKey other)) {
                return false;
            }
            return this.level == other.level && this.pos.equals(other.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(level), pos);
        }
    }
}
