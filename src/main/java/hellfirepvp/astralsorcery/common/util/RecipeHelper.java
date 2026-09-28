package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class RecipeHelper {

    @Nonnull
    public static Optional<net.minecraft.util.Tuple<ItemStack, Float>> findSmeltingResult(Level world, BlockState input) {
        ItemStack stack = ItemUtils.createBlockStack(input);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return findSmeltingResult(world, stack);
    }

    @Nonnull
    public static Optional<net.minecraft.util.Tuple<ItemStack, Float>> findSmeltingResult(Level world, ItemStack input) {
        RecipeManager mgr = world.getRecipeManager();
        SimpleContainer inv = new SimpleContainer(input);
        Optional<SmeltingRecipe> smelting = mgr.getRecipeFor(RecipeType.SMELTING, inv, world);
        if (smelting.isPresent()) {
            return Optional.of(new net.minecraft.util.Tuple<>(smelting.get().getResultItem(world.registryAccess()).copy(), smelting.get().getExperience()));
        }
        Optional<CampfireCookingRecipe> campfire = mgr.getRecipeFor(RecipeType.CAMPFIRE_COOKING, inv, world);
        if (campfire.isPresent()) {
            return Optional.of(new net.minecraft.util.Tuple<>(campfire.get().getResultItem(world.registryAccess()).copy(), campfire.get().getExperience()));
        }
        Optional<SmokingRecipe> smoking = mgr.getRecipeFor(RecipeType.SMOKING, inv, world);
        if (smoking.isPresent()) {
            return Optional.of(new net.minecraft.util.Tuple<>(smoking.get().getResultItem(world.registryAccess()).copy(), smoking.get().getExperience()));
        }
        return Optional.empty();
    }

    @Nullable
    public static RecipeManager getRecipeManager() {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv != null) {
            return srv.getRecipeManager();
        }
        if (net.minecraftforge.api.distmarker.Dist.CLIENT.isClient()) {
            return getClientManager();
        }
        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static AltarRecipe findAltarRecipeResult(Predicate<ItemStack> outputPredicate) {
        RecipeManager manager = getRecipeManager();
        if (manager == null) {
            return null;
        }
        for (AltarRecipe recipe : manager.getAllRecipesFor(RecipeTypesAS.ALTAR.get())) {
            if (outputPredicate.test(recipe.getResult())) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private static RecipeManager getClientManager() {
        ClientPacketListener conn;
        if ((conn = Minecraft.getInstance().getConnection()) != null) {
            return conn.getRecipeManager();
        }
        return null;
    }
}
