/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.GatedRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.AltarRecipeEffectsAS;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraftforge.fml.LogicalSide;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipe
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:31
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 calls this common/crafting/recipe/SimpleAltarRecipe. Not renamed or moved
//because the package layout is a tree-wide decision.
//TODO_PORT[AS-ALT-03]: this tree carries two parallel altar-recipe classes. The reference-faithful port of
//1.16.5's SimpleAltarRecipe lives at common/crafting/recipe/SimpleAltarRecipe (it extends
//common/crafting/helper/CustomMatcherRecipe exactly as 1.16.5 does); this class is the divergent *live* one
//- common/crafting/RecipeSerializersAS binds "astralsorcery:altar" to AltarRecipeSerializer, TileAltar,
//ActiveAltarRecipe, the seven builtin recipes and the altar screens/renderers are all typed against it, and
//AltarRecipeTypeHandler's converters all produce subclasses of it. The two are siblings, not sub/supertypes,
//so they cannot be merged from inside this slice. Structural differences against 1.16.5, all of them a
//consequence of that split:
// - 1.16.5 extends CustomMatcherRecipe, which supplied getId, getType, getSerializer, canFit and the empty
//   crafting result. This class implements vanilla Recipe<Container> directly and resolves the type and the
//   serializer through the DeferredRegister holders, because common/crafting/RecipeTypesAS holds vanilla
//   RecipeTypes rather than 1.16.5's ResolvingRecipeType. matches(Container,Level), assemble,
//   canCraftInDimensions, getResultItem, getId, findRecipe, createContainer, getResult, the no-argument
//   getOutputs and the nested AltarContainer are the glue that replaces it.
// - 1.16.5's duration/starlightRequirement/grid/outputs are mutable and filled by
//   common/crafting/builder/SimpleAltarRecipeBuilder through setDuration/setStarlightRequirement/setInputs/
//   addOutput. That builder is fully ported and is typed against the reference-faithful
//   SimpleAltarRecipe, so those setters are not duplicated here; this class takes all five through its
//   constructor instead (which is what AltarRecipeSerializer and the builtin converters need).
// - 1.16.5's getOutputs(TileAltar) is getOutputsForAltar(TileAltar) here so that it does not collide with
//   the no-argument getOutputs() the serializer uses; 1.16.5's addRelayInput(Ingredient) takes a
//   WrappedIngredient here because this tree's relay inputs can be astralsorcery fluid ingredients.
public class AltarRecipe implements Recipe<Container>, GatedRecipe.Progression {

    private final ResourceLocation id;
    private final AltarType altarType;
    private final AltarRecipeGrid grid;
    private final List<ItemStack> outputs;
    private final int starlightRequired;
    private final int craftingTime;

    private ResourceLocation customRecipeType = null;
    @Nullable
    private IConstellation focusConstellation = null;

    private final List<WrappedIngredient> relayInputs = new ArrayList<>();

    private final Set<AltarRecipeEffect> craftingEffects = new HashSet<>();

    public AltarRecipe(ResourceLocation id, AltarType altarType, AltarRecipeGrid grid, List<ItemStack> outputs, int starlightRequired, int craftingTime) {
        this.id = id;
        this.altarType = altarType;
        this.grid = grid;
        this.outputs = new ArrayList<>(outputs);
        this.starlightRequired = starlightRequired;
        this.craftingTime = craftingTime;

        this.addDefaultEffects();
    }

    //AS-ALT-02 (resolved, but NOT by porting the 1.16.5 switch): this stays empty on purpose.
    //
    //1.16.5 SimpleAltarRecipe.java:90-104 adds the default effects here in an intentional fall-through switch
    //over getAltarType(). That is unnecessary in this tree, because the shipped datapack already bakes exactly
    //those effects into each recipe's "effects" array and SimpleAltarRecipeSerializer resolves them through
    //RegistriesAS.REGISTRY_ALTAR_EFFECTS - a path that does not touch the AltarRecipeEffectsAS handles at all.
    //
    //Measured on a running dedicated server before any change here: all 117 altar recipes already carried their
    //effects, none had an empty set, and the per-type counts were exactly what the 1.16.5 switch produces -
    //DISCOVERY 1..2, ATTUNEMENT 2..5, CONSTELLATION 5..8, RADIANCE 7..10. astralsorcery:altar/marble_pillar is a
    //one-line example: its json lists built_in_effect_discovery_central_beam and nothing else.
    //
    //addAltarEffect is a plain list add with no de-duplication, so running the switch on top of the datapack
    //would append a second copy of every default effect: marble_pillar would render the discovery beam twice and
    //astralsorcery:altar/observatory would go from 7 effects to 14. The handles being null was a real latent bug
    //and RegistryRecipeTypes#initAltarEffects now assigns all 20, but the fix for that is the assignment, not
    //this switch.
    private void addDefaultEffects() {
    }

    //No 1.16.5 counterpart: 1.16.5 resolved altar recipes through RecipeTypesAS.TYPE_ALTAR (a
    //ResolvingRecipeType) via SimpleAltarRecipeContext. This tree resolves them through the vanilla
    //RecipeManager, so the lookup lives on the recipe class.
    @Nullable
    public static AltarRecipe findRecipe(Level level, TileAltar altar, @Nullable Player player) {
        var optional = level.getRecipeManager()
                .getRecipeFor(RecipeTypesAS.ALTAR.get(), new AltarContainer(altar), level);
        AltarRecipe recipe = optional.orElse(null);
        if (recipe == null) {
            return null;
        }
        if (player != null && !recipe.matches(player, altar, false)) {
            return null;
        }
        return recipe;
    }

    public static AltarContainer createContainer(TileAltar altar) {
        return new AltarContainer(altar);
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!(container instanceof AltarContainer altarContainer)) {
            return false;
        }
        TileAltar altar = altarContainer.getAltar();

        // Check altar type compatibility
        if (!altarType.isThisLEThan(altar.getAltarType())) {
            return false;
        }

        if (!matchesFocusConstellation(altar)) {
            return false;
        }

        // Check starlight requirement
        if (altar.getStoredStarlight() < starlightRequired) {
            return false;
        }

        // Check grid inputs
        return grid.containsInputs(altar.getInventory(), true);
    }

    /**
     * 1.16.5 counterpart: common/crafting/recipe/SimpleAltarRecipe.java:189-217, ported verbatim below.
     * <p>
     * The side is what decides whether the research gate is read from the server progress map or from the
     * synced client progress; 1.16.5 handed it in through SimpleAltarRecipeContext and
     * client/screen/base/ScreenContainerAltar passed LogicalSide.CLIENT (1.16.5 ScreenContainerAltar.java:50)
     * while common/crafting/recipe/altar/ActiveSimpleAltarRecipe passed LogicalSide.SERVER. This tree only had
     * the three-argument form below, which hardcodes LogicalSide.SERVER and therefore under-reported recipes
     * on the client (see TODO_PORT[AS-SCR-01] in ScreenContainerAltar), so the reference signature is
     * restored here and the three-argument form delegates to it.
     */
    public boolean matches(LogicalSide side, Player crafter, TileAltar altar, boolean ignoreStarlightRequirement) {
        if (crafter == null) {
            return false;
        }
        boolean hasProgress;
        if (side.isClient()) {
            hasProgress = this.hasProgressionClient();
        } else {
            hasProgress = this.hasProgressionServer(crafter);
        }
        if (!hasProgress) {
            return false;
        }

        if (!this.getAltarType().isThisLEThan(altar.getAltarType())) {
            return false;
        }
        if (!this.matchesFocusConstellation(altar)) {
            return false;
        }
        if (!ignoreStarlightRequirement && altar.getStoredStarlight() < this.getStarlightRequired()) {
            return false;
        }

        return this.grid.containsInputs(altar.getInventory(), true);
    }

    /**
     * Server-side shorthand for {@link #matches(LogicalSide, Player, TileAltar, boolean)}. No 1.16.5
     * counterpart - it exists because this tree's callers (ActiveAltarRecipe, findRecipe above and
     * client/screen/base/ScreenContainerAltar) have no SimpleAltarRecipeContext to carry the side in.
     */
    public boolean matches(Player player, TileAltar altar, boolean ignoreStarlightRequirement) {
        return this.matches(LogicalSide.SERVER, player, altar, ignoreStarlightRequirement);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= AltarRecipeGrid.GRID_SIZE && height >= AltarRecipeGrid.GRID_SIZE;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializersAS.ALTAR.get();
    }

    @Override
    public RecipeType<?> getType() {
        //1.16.5: RecipeTypesAS.TYPE_ALTAR.getType(), where TYPE_ALTAR was a
        //ResolvingRecipeType<IItemHandler, SimpleAltarRecipe, SimpleAltarRecipeContext> held in common/lib.
        //TODO_PORT[AS-ALT-04]: re-point at a ResolvingRecipeType once common/crafting/helper/ResolvingRecipeType
        // is finished and common/lib gains TYPE_ALTAR.
        return RecipeTypesAS.ALTAR.get();
    }

    public AltarType getAltarType() {
        return altarType;
    }

    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return altarType.getAssociatedTier();
    }

    //1.16.5 name: getInputs()
    public AltarRecipeGrid getGrid() {
        return grid;
    }

    //No 1.16.5 counterpart; the serializer and the builtin converters need the raw output list.
    public List<ItemStack> getOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    //1.16.5 name: getStarlightRequirement()
    public int getStarlightRequired() {
        return starlightRequired;
    }

    //1.16.5 name: getDuration()
    public int getCraftingTime() {
        return craftingTime;
    }

    //No 1.16.5 counterpart; convenience for the single-output case.
    public ItemStack getResult() {
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).copy();
    }

    @Nullable
    public ResourceLocation getCustomRecipeType() {
        return customRecipeType;
    }

    public void setCustomRecipeType(@Nullable ResourceLocation customRecipeType) {
        this.customRecipeType = customRecipeType;
    }

    /**
     * No 1.16.5 counterpart. 1.16.5's convertToThis(..) factories also built a fresh instance from
     * (id, altarType, duration, starlightRequirement, grid) and dropped everything else
     * (1.16.5 common/crafting/recipe/altar/builtin/AttunementUpgradeRecipe.java:44-46), which was safe
     * because both 1.16.5 call sites converted immediately after construction - before the custom type, the
     * outputs, the focus constellation, the relay inputs and the effects were read. This tree's call sites
     * (AltarRecipeSerializer#fromJson / #fromNetwork) convert at the very same point, so in practice this
     * copies nothing; it exists so that a later converter call cannot silently drop that state.
     */
    protected void copyCommonPropertiesFrom(AltarRecipe other) {
        this.setCustomRecipeType(other.getCustomRecipeType());
        this.setFocusConstellation(other.getFocusConstellation());
        for (WrappedIngredient input : other.getRelayInputs()) {
            this.addRelayInput(input);
        }
        for (AltarRecipeEffect effect : other.getCraftingEffects()) {
            this.addAltarEffect(effect);
        }
    }

    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
    }

    /**
     * 1.16.5 counterpart: common/crafting/recipe/SimpleAltarRecipe.java:221 (serializeAdditionalJson), the
     * write-side twin of deserializeAdditionalJson. Its only 1.16.5 caller is
     * SimpleAltarRecipe#write(JsonObject) (1.16.5 lines 281-285), which the recipe *builder* / data provider
     * path uses; this tree's AltarRecipe has no JSON-write path at all yet, so the hook is currently only
     * implemented, never invoked. It is declared here so the seven builtin subclasses can carry their
     * reference-faithful overrides instead of silently dropping them.
     * TODO_PORT[AS-ALT-11]: call this from an AltarRecipe#write(JsonObject) once this tree's altar path
     * gains one (1.16.5 SimpleAltarRecipe.java:265-306) - the reference-faithful
     * common/crafting/recipe/SimpleAltarRecipe already has that method and calls the hook.
     */
    public void serializeAdditionalJson(JsonObject recipeObject) {
    }

    public void writeRecipeSync(FriendlyByteBuf buffer) {
    }

    public void readRecipeSync(FriendlyByteBuf buffer) {
    }

    @Nullable
    public IConstellation getFocusConstellation() {
        return focusConstellation;
    }

    public void setFocusConstellation(@Nullable IConstellation focusConstellation) {
        this.focusConstellation = focusConstellation;
    }

    //1.16.5 inlined this in matches(..) (1.16.5 common/crafting/recipe/SimpleAltarRecipe.java:206-211); it is
    //a private helper here because the vanilla matches(Container, Level) glue above needs the same check.
    private boolean matchesFocusConstellation(TileAltar altar) {
        if (this.getFocusConstellation() == null) {
            return true;
        }
        IConstellation focus = altar.getFocusedConstellation();
        return focus != null && focus.equals(this.getFocusConstellation());
    }

    @Nonnull
    public List<WrappedIngredient> getRelayInputs() {
        return Collections.unmodifiableList(relayInputs);
    }

    //1.16.5 signature: addRelayInput(Ingredient) - this tree wraps the ingredient one level earlier so that
    //astralsorcery fluid ingredients can be relay inputs too.
    public void addRelayInput(WrappedIngredient input) {
        this.relayInputs.add(input);
    }

    public void onRecipeCompletion(TileAltar altar, ActiveAltarRecipe activeRecipe) {
    }

    //1.16.5 marks this @OnlyIn(Dist.CLIENT) (1.16.5 common/crafting/recipe/SimpleAltarRecipe.java:156-161).
    //It is plain common code here because this tree reaches it from common/data/journal/JournalPageRecipe,
    //which is loaded on both sides; the body only reads the recipe's own output list, so it is side-safe.
    @Nonnull
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack first = outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0);
        return first.copy();
    }

    //1.16.5 name: getOutputs(TileAltar) - renamed so it does not collide with the no-argument getOutputs()
    //above. 1.16.5 built the copy through MiscUtils.transformList + ItemUtils.copyStackWithSize, which is
    //ItemStack#copy per element.
    @Nonnull
    public List<ItemStack> getOutputsForAltar(TileAltar altar) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack output : outputs) {
            result.add(output.copy());
        }
        return result;
    }

    public Collection<AltarRecipeEffect> getCraftingEffects() {
        return Collections.unmodifiableCollection(craftingEffects);
    }

    public void addAltarEffect(AltarRecipeEffect effect) {
        this.craftingEffects.add(effect);
    }

    /**
     * No 1.16.5 counterpart. 1.16.5 matched altar recipes through a ResolvingRecipeType against the altar's
     * IItemHandler directly; vanilla's RecipeManager in 1.20.1 only accepts a Container, so the altar
     * inventory is mirrored into one for the lookup in {@link #findRecipe(Level, TileAltar, Player)}.
     */
    private static class AltarContainer extends SimpleContainer {
        private final TileAltar altar;

        private AltarContainer(TileAltar altar) {
            super(AltarRecipeGrid.MAX_INVENTORY_SIZE);
            this.altar = altar;
            for (int i = 0; i < AltarRecipeGrid.MAX_INVENTORY_SIZE; i++) {
                setItem(i, altar.getInventory().getStackInSlot(i));
            }
        }

        private TileAltar getAltar() {
            return altar;
        }
    }
}
