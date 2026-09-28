/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.jei;

import hellfirepvp.astralsorcery.common.container.ContainerAltarBase;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.RecipeTransferResult;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MappedRecipeTransferHandler
 * Created by HellFirePvP
 * Date: 05.09.2020 / 15:35
 */
//1.16.5 implemented IRecipeTransferHandler<C> by hand and did the whole job itself: it counted the layout's
//non-empty input ingredients, built the available-stack map, called mezz.jei.transfer.RecipeTransferUtil to
//match them and sent a mezz.jei.network.packets.PacketRecipeTransfer
//(1.16.5 TieredAltarRecipeTransferHandler.java:22-26, 97-179). Not one of those five types is published API,
//and all of them moved into mezz.jei.common.* / mezz.jei.library.* in JEI 15, so reaching for them again would
//mean compiling against JEI's implementation.
//
//JEI 15 exposes the same machinery through mezz.jei.api.recipe.transfer.IRecipeTransferInfo plus
//IRecipeTransferHandlerHelper#createUnregisteredRecipeTransferHandler, which hands back JEI's own
//BasicRecipeTransferHandler bound to that info - i.e. the matching, the "inventory full" / "missing items"
//errors, the JEI-on-server check and the transfer packet, all behind the API. What is left for this class is
//exactly the altar-specific part 1.16.5 wrote by hand:
//
//  1. the slot mapping. getRecipeSlots must return the container slots in the same order as the layout's
//     RecipeIngredientRole.INPUT slot views, because JEI pairs them by index
//     (mezz.jei.common.transfer.RecipeTransferUtil#getRecipeTransferOperations). CategoryAltar#setRecipe emits
//     one input slot per occupied 5x5 grid index in ascending order, so the mapping is
//     CategoryAltar#occupiedGridSlots -> ContainerAltarBase#translateIndex -> container slot, which is what
//     1.16.5 used translateIndex for as well.
//  2. dropping the relay inputs. They are the extra ingredients laid on Spectral Relays around the altar, not
//     slots of the altar itself, and CategoryAltar#setRecipe appends them after the grid. 1.16.5 removed them
//     with "if (entry.getKey() < 25)" (1.16.5 ibid. :121-126); here the input slot views are truncated to the
//     grid count and re-wrapped with IRecipeTransferHandlerHelper#createRecipeSlotsView.
//
//AS-INT-07 is therefore closed: no JEI implementation class is referenced.
@OnlyIn(Dist.CLIENT)
public class TieredAltarRecipeTransferHandler<C extends ContainerAltarBase> implements IRecipeTransferHandler<C, AltarRecipe> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * ContainerAltarBase binds the 36 player slots first and the altar's own slots from 36 upwards; see the
     * class comment there and ContainerAltarTrait's slot-layout note.
     */
    private static final int PLAYER_SLOT_COUNT = 36;

    private final Class<C> containerClass;
    private final RecipeType<AltarRecipe> recipeType;
    private final IRecipeTransferHandlerHelper handlerHelper;
    private final int maxListSize;
    private final IRecipeTransferHandler<C, AltarRecipe> delegate;

    /**
     * 1.16.5 signature: TieredAltarRecipeTransferHandler(Class<C>, IStackHelper, IRecipeTransferHandlerHelper, int)
     * The IStackHelper is gone because JEI's own handler resolves it internally, and the category's
     * RecipeType takes its place - IRecipeTransferHandler#getRecipeType is mandatory in JEI 15.
     *
     * @param maxListSize the number of crafting slots this altar container binds (9, 13, 21 or 25)
     */
    public TieredAltarRecipeTransferHandler(Class<C> containerClass,
                                            RecipeType<AltarRecipe> recipeType,
                                            IRecipeTransferHandlerHelper handlerHelper,
                                            int maxListSize) {
        this.containerClass = containerClass;
        this.recipeType = recipeType;
        this.handlerHelper = handlerHelper;
        this.maxListSize = maxListSize;
        this.delegate = handlerHelper.createUnregisteredRecipeTransferHandler(new AltarTransferInfo());
    }

    @Override
    public Class<? extends C> getContainerClass() {
        return this.containerClass;
    }

    @Override
    public Optional<MenuType<C>> getMenuType() {
        // 1.16.5 identified a handler by its container class alone. MenuTypesAS does hold a MenuType per altar
        // tier, but it is declared as MenuType<ContainerAltarX> rather than MenuType<C>, so it cannot be
        // threaded through this type parameter without an unchecked cast; the container class is what JEI
        // matches on for a hand-written IRecipeTransferHandler anyway.
        return Optional.empty();
    }

    @Override
    public RecipeType<AltarRecipe> getRecipeType() {
        return this.recipeType;
    }

    /**
     * The path JEI actually drives (mezz.jei.common.transfer.RecipeTransferService calls this overload, not
     * the six-argument one). Taking it means the delegate can send the modern
     * PacketRecipeTransferWithResult and report the server's answer back through the context; with a null
     * context it silently falls back to the pre-result packets instead.
     */
    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(IRecipeTransferContext<AltarRecipe, C> context, boolean doTransfer) {
        C container = context.getContainer();
        AltarRecipe recipe = context.getRecipe();
        IRecipeSlotsView gridOnly = gridInputsOnly(container, recipe, context.getRecipeSlots());
        if (gridOnly == null) {
            return this.handlerHelper.createInternalError();
        }
        return this.delegate.transferRecipe(new GridOnlyContext(context, gridOnly), doTransfer);
    }

    /**
     * Still abstract on IRecipeTransferHandler in JEI 15.62 even though it is deprecated for removal, so it
     * has to be implemented; the default IRecipeTransferHandler#transferRecipe(IRecipeTransferContext, boolean)
     * forwards here, which is the only way this is reached now that the overload above is overridden.
     */
    @SuppressWarnings("removal")
    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(C container, AltarRecipe recipe, IRecipeSlotsView recipeSlots,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        IRecipeSlotsView gridOnly = gridInputsOnly(container, recipe, recipeSlots);
        if (gridOnly == null) {
            return this.handlerHelper.createInternalError();
        }
        return this.delegate.transferRecipe(container, recipe, gridOnly, player, maxTransfer, doTransfer);
    }

    /**
     * The layout's input slot views with the relay inputs cut off the end, or {@code null} when the layout
     * does not line up with the recipe at all (1.16.5 answered those cases with
     * handlerHelper.createInternalError(), at TieredAltarRecipeTransferHandler.java:75-77 and :112-115).
     */
    @Nullable
    private IRecipeSlotsView gridInputsOnly(C container, AltarRecipe recipe, IRecipeSlotsView recipeSlots) {
        if (!this.containerClass.isAssignableFrom(container.getClass())) {
            return null;
        }

        int gridInputCount = CategoryAltar.occupiedGridSlots(recipe).size();
        List<IRecipeSlotView> inputs = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
        if (inputs.size() < gridInputCount) {
            LOGGER.error("Recipe {} declares {} grid inputs but its layout only has {} input slots",
                    recipe.getId(), gridInputCount, inputs.size());
            return null;
        }
        if (gridInputCount > this.maxListSize) {
            LOGGER.error("Recipe Transfer does not work for container {}", container.getClass());
            return null;
        }

        // Remove the relay inputs from the input grid.
        if (inputs.size() == gridInputCount) {
            return recipeSlots;
        }
        return this.handlerHelper.createRecipeSlotsView(new ArrayList<>(inputs.subList(0, gridInputCount)));
    }

    /**
     * Hands the delegate the caller's own transfer context with only the recipe slots swapped out, so the
     * transfer id, the screen and completeRecipeTransfer keep pointing at the transfer JEI started.
     */
    private class GridOnlyContext implements IRecipeTransferContext<AltarRecipe, C> {

        private final IRecipeTransferContext<AltarRecipe, C> parent;
        private final IRecipeSlotsView recipeSlots;

        private GridOnlyContext(IRecipeTransferContext<AltarRecipe, C> parent, IRecipeSlotsView recipeSlots) {
            this.parent = parent;
            this.recipeSlots = recipeSlots;
        }

        @Override
        public IRecipeSlotsView getRecipeSlots() {
            return this.recipeSlots;
        }

        @Override
        public int getTransferId() {
            return this.parent.getTransferId();
        }

        @Override
        public void completeRecipeTransfer(RecipeTransferResult result) {
            this.parent.completeRecipeTransfer(result);
        }

        @Override
        public AltarRecipe getRecipe() {
            return this.parent.getRecipe();
        }

        @Override
        public RecipeType<AltarRecipe> getRecipeType() {
            return this.parent.getRecipeType();
        }

        @Override
        public C getContainer() {
            return this.parent.getContainer();
        }

        @Override
        public AbstractContainerScreen<C> getScreen() {
            return this.parent.getScreen();
        }

        @Override
        public Player getPlayer() {
            return this.parent.getPlayer();
        }

        @Override
        public boolean isMaxTransfer() {
            return this.parent.isMaxTransfer();
        }
    }

    /**
     * Describes the altar container to JEI's own transfer handler. 1.16.5 read the recipe's tier off the
     * layout's category and the altar's tier off the tile entity but never actually compared the two
     * (1.16.5 TieredAltarRecipeTransferHandler.java:79-83 - both locals are dead there); the comparison those
     * lines were plainly meant to drive is written here, in {@link #canHandle}, because JEI 15 needs a
     * canHandle answer anyway and a lower-tier altar genuinely cannot host a higher-tier recipe.
     */
    private class AltarTransferInfo implements IRecipeTransferInfo<C, AltarRecipe> {

        @Override
        public Class<? extends C> getContainerClass() {
            return TieredAltarRecipeTransferHandler.this.containerClass;
        }

        @Override
        public Optional<MenuType<C>> getMenuType() {
            return TieredAltarRecipeTransferHandler.this.getMenuType();
        }

        @Override
        public RecipeType<AltarRecipe> getRecipeType() {
            return TieredAltarRecipeTransferHandler.this.recipeType;
        }

        @Override
        public boolean canHandle(C container, AltarRecipe recipe) {
            return altarType(container).filter(altarTier -> {
                if (!altarTier.isThisGEThan(recipe.getAltarType())) {
                    return false;
                }
                for (int gridSlot : CategoryAltar.occupiedGridSlots(recipe)) {
                    if (craftingSlot(container, altarTier, gridSlot) == null) {
                        return false;
                    }
                }
                return true;
            }).isPresent();
        }

        @Override
        public List<Slot> getRecipeSlots(C container, AltarRecipe recipe) {
            List<Slot> slots = new ArrayList<>();
            AltarType altarTier = altarType(container).orElse(null);
            if (altarTier == null) {
                return slots;
            }
            for (int gridSlot : CategoryAltar.occupiedGridSlots(recipe)) {
                Slot slot = craftingSlot(container, altarTier, gridSlot);
                if (slot == null) {
                    // canHandle already rejected this pairing; returning a short list rather than a list with
                    // holes keeps JEI's index pairing honest if it is ever called anyway.
                    return new ArrayList<>();
                }
                slots.add(slot);
            }
            return slots;
        }

        @Override
        public List<Slot> getInventorySlots(C container, AltarRecipe recipe) {
            // 1.16.5 built this as a HashMap keyed by Slot#slotNumber over container.inventorySlots.subList(0, 36).
            return container.slots.subList(0, PLAYER_SLOT_COUNT);
        }
    }

    private static Optional<AltarType> altarType(ContainerAltarBase container) {
        TileAltar altar = container.getTileEntity();
        return altar == null ? Optional.empty() : Optional.of(altar.getAltarType());
    }

    /**
     * The container slot that holds the given 5x5 grid index, or {@code null} when this altar has no such slot.
     * ContainerAltarBase#translateIndex is the altar-side half of the mapping; it is only defined for indices
     * the tier actually exposes (AltarType lists them), and the SlotItemHandler cross-check makes sure the
     * slot really is backed by that grid index rather than trusting the arithmetic.
     */
    @Nullable
    private Slot craftingSlot(ContainerAltarBase container, AltarType altarTier, int gridSlot) {
        if (!altarTier.hasSlot(gridSlot)) {
            return null;
        }
        int craftingIndex = container.translateIndex(gridSlot);
        if (craftingIndex < 0 || craftingIndex >= this.maxListSize) {
            return null;
        }
        int slotNumber = PLAYER_SLOT_COUNT + craftingIndex;
        if (slotNumber >= container.slots.size()) {
            // 1.16.5 TieredAltarRecipeTransferHandler.java:191-195
            LOGGER.error("Recipes Transfer references slot {} outside of the inventory's size {}", slotNumber, container.slots.size());
            return null;
        }
        Slot slot = container.slots.get(slotNumber);
        if (!(slot instanceof SlotItemHandler handlerSlot) || handlerSlot.getSlotIndex() != gridSlot) {
            return null;
        }
        return slot;
    }
}
