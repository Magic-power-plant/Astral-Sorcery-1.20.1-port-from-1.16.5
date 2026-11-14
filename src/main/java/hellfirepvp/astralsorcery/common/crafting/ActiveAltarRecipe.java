/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.crafting.helper.RecipeHelper;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.helper.CraftingFocusStack;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.blockentity.starlight.SpectralRelayBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.item.util.ItemStackHandlerAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
//TODO_PORT[AS-PKG-01]: 1.16.5 calls this common/crafting/recipe/altar/ActiveSimpleAltarRecipe. Not renamed or
//moved because the package layout is a tree-wide decision.
//TODO_PORT[AS-ALT-03]: this tree carries two parallel active-altar-recipe classes. The reference-faithful port
//of 1.16.5's ActiveSimpleAltarRecipe lives at common/crafting/recipe/altar/ActiveSimpleAltarRecipe (typed
//against the reference-faithful common/crafting/recipe/SimpleAltarRecipe); this class is the divergent *live*
//one - TileAltar#activeRecipe, ResearchManager#informCraftedAltar, client/render/tile/RenderAltar and the
//three builtin upgrade recipes are all typed against it, and it wraps the live common/crafting/AltarRecipe.
//The two are siblings, not sub/supertypes, so they cannot be merged from inside this slice. Every remaining
//difference against 1.16.5 is a consequence of that split or of a call site outside this slice:
// - 1.16.5's field/accessor pair is recipeToCraft/getRecipeToCraft(); it is recipe/getRecipe() here because
//   RenderAltar, TileAltar and ResearchManager call getRecipe(). 1.16.5's getOutputs(TileAltar) is
//   getOutputsForAltar(TileAltar) on the live AltarRecipe, and getDuration() is getCraftingTime().
// - 1.16.5's public constructor is (SimpleAltarRecipe, int durationDivisor, UUID); the UUID and the divisor
//   are swapped here because common/tile/altar/TileAltar.java:307 (outside this slice) calls
//   new ActiveAltarRecipe(recipe, crafter.getUUID(), divisor).
// - 1.16.5's tryGetCraftingPlayerServer() is no-argument and resolves the server through
//   LogicalSidedProvider.INSTANCE (gone in 1.20.1); it takes the altar here because TileAltar,
//   ResearchManager and the builtin upgrade recipes all call it with one.
// - 1.16.5's deserialize(CompoundNBT, ActiveSimpleAltarRecipe) pulls the RecipeManager off
//   RecipeHelper.getRecipeManager(); it is handed in here because TileAltar#readCustomNBT resolves it (see
//   the VERIFY_PORT[AS-ALT-14] note there). The reference two-argument shape is restored as an overload.
public class ActiveAltarRecipe {

    private static final Random rand = new Random();

    private Map<Integer, Object> clientEffectContainer = new HashMap<>();

    //1.16.5 name: recipeToCraft
    private final AltarRecipe recipe;
    private final UUID playerCraftingUUID;
    private int ticksCrafting = 0;
    private int totalCraftingTime;

    private CraftingState state;
    private CompoundTag craftingData = new CompoundTag();

    private List<CraftingFocusStack> focusStacks = new LinkedList<>();

    private ActiveAltarRecipe(AltarRecipe recipeToCraft, UUID playerCraftingUUID) {
        this(recipeToCraft, playerCraftingUUID, 1);
    }

    public ActiveAltarRecipe(AltarRecipe recipeToCraft, UUID playerCraftingUUID, int durationDivisor) {
        Objects.requireNonNull(recipeToCraft);

        this.recipe = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
        this.state = CraftingState.ACTIVE;
        //1.16.5: recipeToCraft.getDuration() / durationDivisor. The Math.max(1, ..) has no 1.16.5 counterpart;
        //TileAltar#startCrafting always passes 2^n >= 1, so it only guards third-party callers from a /0.
        this.totalCraftingTime = recipeToCraft.getCraftingTime() / Math.max(1, durationDivisor);
    }

    private void recoverContainedEffects(@Nullable ActiveAltarRecipe previous) {
        if (previous != null && previous.getRecipe().getId().equals(this.recipe.getId())) {
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

    /**
     * 1.16.5 counterpart: common/crafting/recipe/altar/ActiveSimpleAltarRecipe.java:110-114, which read
     * {@code LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)}. LogicalSidedProvider lost its INSTANCE
     * constant in 1.20.1; the altar's own level carries the same server and is already non-null on every
     * server-side call path, so the altar is handed in instead of reaching for the global holder.
     * 1.16.5's PlayerList#getPlayerByUUID is PlayerList#getPlayer(UUID) in 1.20.1.
     */
    @Nullable
    public ServerPlayer tryGetCraftingPlayerServer(AltarBlockEntity altar) {
        if (altar.getLevel() == null || altar.getLevel().isClientSide() || altar.getLevel().getServer() == null) {
            return null;
        }
        return altar.getLevel().getServer().getPlayerList().getPlayer(this.getPlayerCraftingUUID());
    }

    @OnlyIn(Dist.CLIENT)
    public <T> T getEffectContained(int index, Function<Integer, T> provider) {
        return (T) clientEffectContainer.computeIfAbsent(index, provider);
    }

    public void createItemOutputs(AltarBlockEntity altar, Consumer<ItemStack> output) {
        Consumer<ItemStack> informer = stack -> ResearchManager.informCraftedAltar(altar, this, stack);

        Consumer<ItemStack> handleCrafted = informer.andThen(output);
        //1.16.5: getRecipeToCraft().getOutputs(altar) - renamed to getOutputsForAltar(TileAltar) on the live
        //AltarRecipe so it does not collide with its no-argument getOutputs().
        this.getRecipe().getOutputsForAltar(altar).forEach(handleCrafted);
        this.getRecipe().onRecipeCompletion(altar, this);
    }

    /**
     * No 1.16.5 counterpart: 1.16.5's only caller is TileAltar#finishRecipe, which passes
     * {@code this::dropItemOnTop} into createItemOutputs (1.16.5 common/tile/altar/TileAltar.java:221).
     * This tree's TileAltar#finishRecipe calls createOutputs(this) instead, so the consumer is bound here.
     * <p>
     * The previous implementation of this method also wrapped its body in
     * {@code ForgeHooks.setCraftingPlayer(..)} / {@code ForgeHooks.setCraftingPlayer(null)}. That had no
     * 1.16.5 counterpart and was actively harmful: TileAltar#finishRecipe already sets the crafting player
     * around both createOutputs *and* consumeInputs (1.16.5 TileAltar.java:220-223), so clearing it at the
     * end of the output pass left consumeInputs - the half that actually resolves container items and
     * crafting remainders - running with a null crafting player.
     */
    public void createOutputs(AltarBlockEntity altar) {
        this.createItemOutputs(altar, altar::dropItemOnTop);
    }

    public void consumeInputs(AltarBlockEntity altar) {
        //1.16.5: TileAltar#getInventory() returned a TileInventory; this tree's TileAltar hands out an
        //ItemStackHandlerAS (an IItemHandlerModifiable), which offers the same
        //getStackInSlot/setStackInSlot pair used below.
        ItemStackHandlerAS inv = altar.getInventory();
        //1.16.5 name: getInputs()
        AltarRecipeGrid grid = this.recipe.getGrid();

        //AS-ALT-10 (resolved): ItemUtils.decrementItem(IItemHandler, slot, handleExcess) now implements
        //1.16.5's shrink-by-one semantics (1.16.5 common/util/item/ItemUtils.java:79-115) instead of
        //extracting the whole stack and dropping handleExcess on the floor.
        //This loop walked "for (int slot : grid.getInputSlots())" with an altar.hasSlot(slot) guard and an
        //isEmpty() skip. Both are dropped in favour of 1.16.5's flat walk over the whole inventory
        //(1.16.5 ActiveSimpleAltarRecipe.java:133-144), because getInputSlots() returns the *un-shifted*
        //grid indices: AltarRecipeGrid#containsInputs matches the grid at any x/z offset inside the 5x5
        //inventory (common/crafting/AltarRecipeGrid.java:80-92), so for an off-centre match those indices
        //are not the slots that actually hold the inputs and the wrong slots were consumed. Walking all 25
        //slots is offset-independent and consumes exactly the occupied ones, because a successful match
        //already requires every non-matched slot to be empty (AltarRecipeGrid#isGridEmpty, lines 131-141).
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
            SpectralRelayBlockEntity tar = MiscUtils.getBlockEntityAt(altar.getLevel(), input.getRealPosition(), SpectralRelayBlockEntity.class, true);
            if (tar != null) {
                ItemStackHandlerAS tarInventory = tar.getInventory();
                if (input.getInput() != null && input.getInput().getIngredient() instanceof FluidIngredient) {
                    ItemStack stack = tarInventory.getStackInSlot(0);
                    FluidActionResult far = FluidUtil.tryEmptyContainer(stack, VoidFluidHandler.INSTANCE, FluidType.BUCKET_VOLUME, null, true);
                    if (far.isSuccess()) {
                        tarInventory.setStackInSlot(0, far.getResult());
                    }
                    //This branch carried an "else { ItemUtils.decrementItem(tarInventory, 0, ..) }" that 1.16.5
                    //does not have: a fluid relay input whose container cannot be drained is left untouched
                    //there rather than being eaten (1.16.5 ActiveSimpleAltarRecipe.java:150-158).
                } else {
                    ItemUtils.decrementItem((IItemHandler) tarInventory, 0, altar::dropItemOnTop);
                }
                //A tar.markForUpdate() call used to follow here. It is redundant as well as absent from
                //1.16.5: TileSpectralRelay builds its inventory with an onContentsChanged override that
                //already calls markForUpdate() (common/tile/TileSpectralRelay.java:94-95).
            }
        }
    }

    public boolean matches(AltarBlockEntity altar, boolean ignoreStarlightRequirement, boolean testNecessaryRelayInputs) {
        if (!this.getRecipe().matches(LogicalSide.SERVER, this.tryGetCraftingPlayerServer(altar), altar, ignoreStarlightRequirement)) {
            return false;
        }

        List<WrappedIngredient> listIngredients = this.getRecipe().getRelayInputs();
        for (CraftingFocusStack stack : this.focusStacks) {
            if (stack.getStackIndex() >= 0 && stack.getStackIndex() < listIngredients.size()) {
                SpectralRelayBlockEntity relay = MiscUtils.getBlockEntityAt(altar.getLevel(), stack.getRealPosition(), SpectralRelayBlockEntity.class, true);
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

    public CraftingState tick(AltarBlockEntity altar) {
        if (recipe instanceof AltarCraftingProgress) {
            if (!((AltarCraftingProgress) recipe).tryProcess(altar, this, craftingData, ticksCrafting, totalCraftingTime)) {
                return CraftingState.WAITING;
            }
        }

        List<WrappedIngredient> iIngredients = this.getRecipe().getRelayInputs();

        if (!iIngredients.isEmpty()) {
            boolean shouldWait = tickCraftingRelayInputs(altar, iIngredients);
            if (shouldWait) {
                return CraftingState.WAITING;
            }
        }

        //A per-tick starlight drain used to sit here:
        //  int starlightRequired = recipe.getStarlightRequired();
        //  if (starlightRequired > 0) {
        //      int starlightPerTick = Math.max(1, starlightRequired / totalCraftingTime);
        //      if (altar.getStoredStarlight() < starlightPerTick) { return CraftingState.WAITING; }
        //      altar.consumeStarlight(starlightPerTick);
        //  }
        //It has no 1.16.5 counterpart (1.16.5 ActiveSimpleAltarRecipe.java:190-208 ticks nothing but the relay
        //inputs) and it fought the altar's own model: 1.16.5's altar starlight is a transient per-tick pool
        //that gatherStarlight rebuilds every tick and publishes two ticks later through
        //DeferredStarlightStorage, and recipes only ever *test* getStoredStarlight() in
        //SimpleAltarRecipe#matches. Draining it made TileAltar#doCraftingCycle's own matches(..) check fail
        //two ticks later, which aborts the craft, so high-starlight recipes could never complete.
        //TileAltar#consumeStarlight (VERIFY_PORT[AS-ALT-13] there) existed only for this call and is now
        //unused; removing it is reported as an external change.

        ticksCrafting++;
        return CraftingState.ACTIVE;
    }

    private boolean tickCraftingRelayInputs(AltarBlockEntity altar, List<WrappedIngredient> iIngredients) {
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
                    SpectralRelayBlockEntity tar = MiscUtils.getBlockEntityAt(altar.getLevel(), at, SpectralRelayBlockEntity.class, true);
                    if (tar == null) { // We were lied to. lol.
                        waitMissingInputs = true;
                        continue;
                    }
                    found = new CraftingFocusStack(index, input, at);
                    this.focusStacks.add(found);
                }
                SpectralRelayBlockEntity tar = MiscUtils.getBlockEntityAt(altar.getLevel(), found.getRealPosition(), SpectralRelayBlockEntity.class, true);
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

    //1.16.5 name: getRecipeToCraft()
    public AltarRecipe getRecipe() {
        return recipe;
    }

    public boolean isFinished() {
        return ticksCrafting >= totalCraftingTime;
    }

    //No 1.16.5 counterpart; 1.16.5's consumers all divide getTicksCrafting() by getTotalCraftingTime()
    //themselves. Kept because it is public API of this tree and the infusion twin
    //(common/crafting/ActiveLiquidInfusionRecipe#getProgress) carries the same helper.
    public float getProgress() {
        return totalCraftingTime > 0 ? (float) ticksCrafting / totalCraftingTime : 0;
    }

    /**
     * 1.16.5 signature: deserialize(CompoundNBT, ActiveSimpleAltarRecipe previous). The RecipeManager is an
     * extra argument here because this tree's TileAltar#readCustomNBT resolves it (1.16.5 read the manager off
     * RecipeHelper.getRecipeManager() inside this method); {@code previous} carries the client-side crafting
     * effect container across a re-read of the tile, exactly as in 1.16.5.
     */
    @Nullable
    public static ActiveAltarRecipe deserialize(CompoundTag compound, RecipeManager mgr, @Nullable ActiveAltarRecipe previous) {
        if (mgr == null) {
            return null;
        }

        //1.16.5: new ResourceLocation(String) - deprecated in this Forge version, ResourceLocation.parse is
        //the replacement and throws the very same ResourceLocationException on a malformed id.
        ResourceLocation recipeKey = ResourceLocation.parse(compound.getString("recipeToCraft"));
        //1.16.5: RecipeManager#getRecipe(ResourceLocation) - renamed to byKey in 1.20.1.
        Optional<?> recipe = mgr.byKey(recipeKey);
        if (!recipe.isPresent() || !(recipe.get() instanceof AltarRecipe)) {
            //1.16.5: AstralSorcery.log - the logger field is named LOG in this tree.
            AstralSorcery.LOG.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }
        AltarRecipe altarRecipe = (AltarRecipe) recipe.get();
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

        ActiveAltarRecipe task = new ActiveAltarRecipe(altarRecipe, uuidCraft);
        task.ticksCrafting = tick;
        //The saved duration used to be run back through the constructor as
        //"int divisor = recipe.getCraftingTime() / Math.max(1, totalTime);", which loses it to integer
        //division: a 200-tick recipe saved at totalCraftingTime=150 came back with divisor 1 and therefore
        //totalCraftingTime 200. 1.16.5 assigns the stored value straight onto the task
        //(1.16.5 ActiveSimpleAltarRecipe.java:295-297), which is what the private single-divisor constructor
        //above exists for.
        task.totalCraftingTime = total;
        task.setState(state);
        task.craftingData = compound.getCompound("craftingData");
        task.focusStacks = stacks;
        task.recoverContainedEffects(previous);
        return task;
    }

    /**
     * No 1.16.5 counterpart - 1.16.5 always had a {@code previous} to hand. Kept so that
     * common/tile/altar/TileAltar.java:578 (outside this slice) keeps compiling; passing the previously
     * active recipe in is reported as an external change, because without it the client-side crafting
     * effect container is rebuilt from scratch on every tile re-read.
     */
    @Nullable
    public static ActiveAltarRecipe deserialize(CompoundTag compound, RecipeManager mgr) {
        return deserialize(compound, mgr, null);
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag compound = new CompoundTag();
        //The key used to be "recipe"; 1.16.5 writes "recipeToCraft" and so does the infusion twin
        //common/crafting/ActiveLiquidInfusionRecipe. Both halves of the pair live in this class, so the
        //rename is self-contained (an in-progress altar craft saved by an older build of this tree is
        //dropped on load, which is what deserialize already did for any unresolvable recipe).
        compound.putString("recipeToCraft", getRecipe().getId().toString());
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
