/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar;

import hellfirepvp.astralsorcery.AstralSorcery;
//1.16.5 imported AltarCraftingProgress and AltarRecipeGrid from this very package; this tree keeps both as
//top-level classes in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.AltarCraftingProgress;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.helper.CraftingFocusStack;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.ItemStackHandlerAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveSimpleAltarRecipe
 * Created by HellFirePvP
 * Date: 13.08.2019 / 20:59
 */
public class ActiveSimpleAltarRecipe {

    private static final Random rand = new Random();

    private Map<Integer, Object> clientEffectContainer = new HashMap<>();

    private final SimpleAltarRecipe recipeToCraft;
    private final UUID playerCraftingUUID;
    private int ticksCrafting = 0;
    private int totalCraftingTime;

    private CraftingState state;
    private CompoundTag craftingData = new CompoundTag();

    private List<CraftingFocusStack> focusStacks = new LinkedList<>();

    private ActiveSimpleAltarRecipe(SimpleAltarRecipe recipeToCraft, UUID playerCraftingUUID) {
        this(recipeToCraft, 1, playerCraftingUUID);
    }

    public ActiveSimpleAltarRecipe(SimpleAltarRecipe recipeToCraft, int durationDivisor, UUID playerCraftingUUID) {
        Objects.requireNonNull(recipeToCraft);

        this.recipeToCraft = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
        this.state = CraftingState.ACTIVE;
        this.totalCraftingTime = recipeToCraft.getDuration() / durationDivisor;
    }

    private void recoverContainedEffects(@Nullable ActiveSimpleAltarRecipe previous) {
        if (previous != null && previous.getRecipeToCraft().getId().equals(this.recipeToCraft.getId())) {
            this.clientEffectContainer.putAll(previous.clientEffectContainer);
        }
    }

    public CompoundTag getCraftingData() {
        return craftingData;
    }

    public UUID getPlayerCraftingUUID() {
        return playerCraftingUUID;
    }

    public void setState(CraftingState state) {
        this.state = state;
    }

    public CraftingState getState() {
        return state;
    }

    public List<CraftingFocusStack> getFocusStacks() {
        return focusStacks;
    }

    @Nullable
    public Player tryGetCraftingPlayerServer() {
        //1.16.5: LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER). LogicalSidedProvider lost its
        //INSTANCE constant in 1.20.1; ServerLifecycleHooks.getCurrentServer() is the replacement this tree
        //already uses elsewhere (see common/util/RecipeHelper and common/data/research/ResearchHelper). It
        //can be null off-server, which the 1.16.5 call could not be, hence the extra guard.
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) {
            return null;
        }
        //1.16.5: PlayerList#getPlayerByUUID - renamed to getPlayer(UUID) in 1.20.1.
        return srv.getPlayerList().getPlayer(this.getPlayerCraftingUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public <T> T getEffectContained(int index, Function<Integer, T> provider) {
        return (T) clientEffectContainer.computeIfAbsent(index, provider);
    }

    public void createItemOutputs(TileAltar altar, Consumer<ItemStack> output) {
        //BLOCKED_PORT[AS-ALT-09]: 1.16.5 body (1.16.5 common/crafting/recipe/altar/ActiveSimpleAltarRecipe.java:122-124):
        //  Consumer<ItemStack> informer = stack -> ResearchManager.informCraftedAltar(altar, this, stack);
        //  Consumer<ItemStack> handleCrafted = informer.andThen(output);
        // This tree's common/data/research/ResearchManager only declares
        // informCraftedAltar(TileAltar, ActiveAltarRecipe, ItemStack) - typed against the divergent live
        // common.crafting.ActiveAltarRecipe, which is a sibling of (not a supertype of) this class - and it
        // forwards to AdvancementsAS.ALTAR_CRAFT.trigger(crafter, recipe.getRecipe(), crafted), typed the
        // same way. common/data/research is not owned by this slice, so the research-progression and
        // advancement hook cannot be invoked from here yet. The outputs themselves are still handed to the
        // consumer in recipe order and onRecipeCompletion still fires.
        Consumer<ItemStack> handleCrafted = output;
        this.getRecipeToCraft().getOutputs(altar).forEach(handleCrafted);
        this.getRecipeToCraft().onRecipeCompletion(altar, this);
    }

    public void consumeInputs(TileAltar altar) {
        //1.16.5: TileAltar#getInventory() returned a TileInventory; this tree's TileAltar hands out an
        //ItemStackHandlerAS (an IItemHandlerModifiable), which offers the same
        //getStackInSlot/setStackInSlot pair used below.
        ItemStackHandlerAS inv = altar.getInventory();
        AltarRecipeGrid grid = this.recipeToCraft.getInputs();

        //AS-ALT-10 (resolved): ItemUtils.decrementItem(IItemHandler, slot, handleExcess) now implements
        //1.16.5's shrink-by-one semantics (1.16.5 common/util/item/ItemUtils.java:79-115) instead of
        //extracting the whole stack and dropping handleExcess on the floor.
        for (int slot = 0; slot < AltarRecipeGrid.MAX_INVENTORY_SIZE; slot++) {
            Ingredient input = grid.getIngredient(slot);
            if (input instanceof FluidIngredient) {
                ItemStack stack = inv.getStackInSlot(slot);
                //1.16.5: FluidAttributes.BUCKET_VOLUME - moved to FluidType.BUCKET_VOLUME in 1.20.1.
                FluidActionResult far = FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, FluidType.BUCKET_VOLUME, null, true);
                if (far.isSuccess()) {
                    inv.setStackInSlot(slot, far.getResult());
                }
            } else {
                ItemUtils.decrementItem((IItemHandler) inv, slot, altar::dropItemOnTop);
            }
        }

        for (CraftingFocusStack input : this.focusStacks) {
            //1.16.5: altar.getWorld() - World was renamed to Level and the getter to getLevel() in 1.20.1.
            TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), input.getRealPosition(), TileSpectralRelay.class, true);
            if (tar != null) {
                ItemStackHandlerAS tarInventory = tar.getInventory();
                if (input.getInput() != null && input.getInput().getIngredient() instanceof FluidIngredient) {
                    ItemStack stack = tarInventory.getStackInSlot(0);
                    FluidActionResult far = FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, FluidType.BUCKET_VOLUME, null, true);
                    if (far.isSuccess()) {
                        tarInventory.setStackInSlot(0, far.getResult());
                    }
                } else {
                    ItemUtils.decrementItem((IItemHandler) tarInventory, 0, altar::dropItemOnTop);
                }
            }
        }
    }

    public boolean matches(TileAltar altar, boolean ignoreStarlightRequirement, boolean testNecessaryRelayInputs) {
        if (!this.getRecipeToCraft().matches(LogicalSide.SERVER, this.tryGetCraftingPlayerServer(), altar, ignoreStarlightRequirement)) {
            return false;
        }

        List<WrappedIngredient> listIngredients = this.getRecipeToCraft().getRelayInputs();
        for (CraftingFocusStack stack : this.focusStacks) {
            if (stack.getStackIndex() >= 0 && stack.getStackIndex() < listIngredients.size()) {
                TileSpectralRelay relay = MiscUtils.getTileAt(altar.getLevel(), stack.getRealPosition(), TileSpectralRelay.class, true);
                if (relay == null) {
                    return false;
                }
                if (testNecessaryRelayInputs) {
                    WrappedIngredient ingredient = listIngredients.get(stack.getStackIndex());
                    if (!ingredient.getIngredient().test(relay.getInventory().getStackInSlot(0))) {
                        return false;
                    }
                }
            } else {
                //The recipe changed?
                return false;
            }
        }
        return true;
    }
    //True if the recipe progressed, false if it's stuck

    public CraftingState tick(TileAltar altar) {
        if (recipeToCraft instanceof AltarCraftingProgress) {
            if (!((AltarCraftingProgress) recipeToCraft).tryProcess(altar, this, craftingData, ticksCrafting, totalCraftingTime)) {
                return CraftingState.WAITING;
            }
        }

        List<WrappedIngredient> iIngredients = this.getRecipeToCraft().getRelayInputs();

        if (!iIngredients.isEmpty()) {
            boolean shouldWait = tickCraftingRelayInputs(altar, iIngredients);
            if (shouldWait) {
                return CraftingState.WAITING;
            }
        }

        ticksCrafting++;
        return CraftingState.ACTIVE;
    }

    private boolean tickCraftingRelayInputs(TileAltar altar, List<WrappedIngredient> iIngredients) {
        //Start the surrounding stack intake past 66%
        int part = totalCraftingTime / 3;
        //Every input is evenly spread
        int cttPart = part / (iIngredients.size()) + 1;


        boolean waitMissingInputs = false;
        for (int i = 0; i < iIngredients.size(); i++) {
            WrappedIngredient input = iIngredients.get(i);
            int index = i;

            int offset = part + (index * cttPart);
            if (this.ticksCrafting >= offset) {
                CraftingFocusStack found = MiscUtils.iterativeSearch(this.focusStacks, fStack -> fStack.getStackIndex() == index);
                if (found == null) {
                    Set<BlockPos> relays = altar.nearbyRelays();
                    relays.removeIf(pos -> MiscUtils.contains(this.focusStacks, fs -> fs.getRealPosition().equals(pos)));
                    //No unused relay found for a new focus-stack..
                    if (relays.isEmpty()) {
                        waitMissingInputs = true;
                        continue;
                    }
                    BlockPos at = MiscUtils.getRandomEntry(relays, rand);
                    TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), at, TileSpectralRelay.class, true);
                    if (tar == null) { // We were lied to. lol.
                        waitMissingInputs = true;
                        continue;
                    }
                    found = new CraftingFocusStack(index, input, at);
                    this.focusStacks.add(found);
                }
                TileSpectralRelay tar = MiscUtils.getTileAt(altar.getLevel(), found.getRealPosition(), TileSpectralRelay.class, true);
                if (tar == null) {
                    waitMissingInputs = true;
                    continue;
                }
                if (!input.getIngredient().test(tar.getInventory().getStackInSlot(0))) {
                    waitMissingInputs = true;
                }
            }
        }
        return waitMissingInputs;
    }

    public int getTicksCrafting() {
        return ticksCrafting;
    }

    public int getTotalCraftingTime() {
        return totalCraftingTime;
    }

    public SimpleAltarRecipe getRecipeToCraft() {
        return recipeToCraft;
    }

    public boolean isFinished() {
        return ticksCrafting >= totalCraftingTime;
    }

    @Nullable
    public static ActiveSimpleAltarRecipe deserialize(CompoundTag compound, @Nullable ActiveSimpleAltarRecipe previous) {
        RecipeManager mgr = RecipeHelper.getRecipeManager();
        if (mgr == null) {
            return null;
        }

        //1.16.5: new ResourceLocation(String) - deprecated in this Forge version, ResourceLocation.parse is
        //the replacement and throws the very same ResourceLocationException on a malformed id.
        ResourceLocation recipeKey = ResourceLocation.parse(compound.getString("recipeToCraft"));
        //1.16.5: RecipeManager#getRecipe(ResourceLocation) - renamed to byKey in 1.20.1.
        Optional<?> recipe = mgr.byKey(recipeKey);
        if (!recipe.isPresent() || !(recipe.get() instanceof SimpleAltarRecipe)) {
            //1.16.5: AstralSorcery.log - the logger field is named LOG in this tree.
            AstralSorcery.LOG.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }
        SimpleAltarRecipe altarRecipe = (SimpleAltarRecipe) recipe.get();
        //1.16.5: CompoundNBT#getUniqueId - renamed to getUUID in 1.20.1.
        UUID uuidCraft = compound.getUUID("playerCraftingUUID");
        int tick = compound.getInt("ticksCrafting");
        int total = compound.getInt("totalCraftingTime");
        CraftingState state = CraftingState.values()[compound.getInt("state")];
        List<CraftingFocusStack> stacks = new LinkedList<>();
        //1.16.5: Constants.NBT.TAG_COMPOUND - the NBT type ids live on net.minecraft.nbt.Tag in 1.20.1.
        ListTag listStacks = compound.getList("focusStacks", Tag.TAG_COMPOUND);
        for (int i = 0; i < listStacks.size(); i++) {
            stacks.add(new CraftingFocusStack(listStacks.getCompound(i)));
        }

        ActiveSimpleAltarRecipe task = new ActiveSimpleAltarRecipe(altarRecipe, uuidCraft);
        task.ticksCrafting = tick;
        task.totalCraftingTime = total;
        task.setState(state);
        task.craftingData = compound.getCompound("craftingData");
        task.focusStacks = stacks;
        task.recoverContainedEffects(previous);
        return task;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag compound = new CompoundTag();
        compound.putString("recipeToCraft", getRecipeToCraft().getId().toString());
        //1.16.5: CompoundNBT#putUniqueId - renamed to putUUID in 1.20.1.
        compound.putUUID("playerCraftingUUID", getPlayerCraftingUUID());
        compound.putInt("ticksCrafting", getTicksCrafting());
        compound.putInt("totalCraftingTime", getTotalCraftingTime());
        compound.putInt("state", getState().ordinal());
        compound.put("craftingData", craftingData);

        ListTag list = new ListTag();
        for (CraftingFocusStack stack : this.focusStacks) {
            list.add(stack.serialize());
        }
        compound.put("focusStacks", list);
        return compound;
    }

    public static enum CraftingState {

        ACTIVE, //All valid, continuing to craft.

        WAITING //Potentially waiting for user interaction. Recipe itself is fully valid.

    }

}
