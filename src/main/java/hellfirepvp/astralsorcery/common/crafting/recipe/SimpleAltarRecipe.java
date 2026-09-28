/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe;

import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.lib.AltarRecipeEffectsAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomMatcherRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.CustomRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect.AltarRecipeEffect;
import hellfirepvp.astralsorcery.common.crafting.serializer.SimpleAltarRecipeSerializer;
import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SimpleAltarRecipe
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:31
 */
public class SimpleAltarRecipe extends CustomMatcherRecipe implements GatedRecipe.Progression {

    private final AltarType altarType;

    private int duration;
    private int starlightRequirement;
    private AltarRecipeGrid altarRecipeGrid;
    private final List<ItemStack> outputs = new LinkedList<>();
    private ResourceLocation customRecipeType = null;
    private IConstellation focusConstellation = null;
    private final List<WrappedIngredient> relayInputs = new LinkedList<>();

    private final Set<AltarRecipeEffect> craftingEffects = new HashSet<>();

    public SimpleAltarRecipe(ResourceLocation recipeId, AltarType altarType) {
        this(recipeId, altarType, altarType.getDefaultAltarCraftingDuration());
    }

    public SimpleAltarRecipe(ResourceLocation recipeId, AltarType altarType, int duration) {
        this(recipeId, altarType, duration, 0);
    }

    public SimpleAltarRecipe(ResourceLocation recipeId, AltarType altarType, int duration, int starlightRequirement) {
        this(recipeId, altarType, duration, starlightRequirement, AltarRecipeGrid.EMPTY);
    }

    public SimpleAltarRecipe(ResourceLocation recipeId, AltarType altarType, int duration, int starlightRequirement, AltarRecipeGrid grid) {
        super(recipeId);
        this.altarType = altarType;
        this.duration = duration;
        this.starlightRequirement = starlightRequirement;
        this.altarRecipeGrid = grid;

        this.addDefaultEffects();
    }

    //AS-ALT-02: deliberately empty, mirroring common/crafting/AltarRecipe#addDefaultEffects - see the full
    //reasoning there. In short: the shipped datapack already lists each recipe's effects and
    //SimpleAltarRecipeSerializer resolves them through RegistriesAS.REGISTRY_ALTAR_EFFECTS, and addAltarEffect
    //does not de-duplicate, so porting 1.16.5's fall-through switch on top of that appends a second copy of
    //every default effect (measured on a live server: astralsorcery:altar/observatory would go from 7 to 14).
    //Nothing in this tree constructs a SimpleAltarRecipe today - the live path is common/crafting/AltarRecipe,
    //which TileAltar drives (TODO_PORT[AS-ALT-03]) - so this body is unreachable either way. It is kept empty
    //rather than "correct per 1.16.5" precisely so that collapsing the two hierarchies cannot silently
    //reintroduce the double-add.
    private void addDefaultEffects() {
    }

    @Nonnull
    @Override
    public ResearchProgression getRequiredProgression() {
        return this.getAltarType().getAssociatedTier();
    }

    @Nullable
    public ResourceLocation getCustomRecipeType() {
        return this.customRecipeType;
    }

    public void setCustomRecipeType(@Nullable ResourceLocation customRecipeType) {
        this.customRecipeType = customRecipeType;
    }

    @Nullable
    public IConstellation getFocusConstellation() {
        return focusConstellation;
    }

    public void setInputs(AltarRecipeGrid inputGrid) {
        this.altarRecipeGrid = inputGrid;
    }

    public AltarRecipeGrid getInputs() {
        return altarRecipeGrid;
    }

    public Collection<AltarRecipeEffect> getCraftingEffects() {
        return craftingEffects;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setStarlightRequirement(int starlightRequirement) {
        this.starlightRequirement = starlightRequirement;
    }

    public int getStarlightRequirement() {
        return starlightRequirement;
    }

    public void onRecipeCompletion(TileAltar altar, ActiveSimpleAltarRecipe activeRecipe) {}

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
        ItemStack first = Iterables.getFirst(this.outputs, ItemStack.EMPTY);
        return ItemUtils.copyStackWithSize(first, first.getCount());
    }

    @Nonnull
    public List<ItemStack> getOutputs(TileAltar altar) {
        return MiscUtils.transformList(this.outputs, stack -> ItemUtils.copyStackWithSize(stack, stack.getCount()));
    }

    public void setFocusConstellation(IConstellation focusConstellation) {
        this.focusConstellation = focusConstellation;
    }

    @Nonnull
    public List<WrappedIngredient> getRelayInputs() {
        return relayInputs;
    }

    public boolean addRelayInput(Ingredient i) {
        return this.relayInputs.add(new WrappedIngredient(i));
    }

    public void addAltarEffect(AltarRecipeEffect effect) {
        this.craftingEffects.add(effect);
    }

    public void addOutput(ItemStack output) {
        this.outputs.add(ItemUtils.copyStackWithSize(output, output.getCount()));
    }

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
        if (this.getFocusConstellation() != null) {
            IConstellation focus = altar.getFocusedConstellation();
            if (focus == null || !focus.equals(this.getFocusConstellation())) {
                return false;
            }
        }
        if (!ignoreStarlightRequirement && altar.getStoredStarlight() < this.getStarlightRequirement()) {
            return false;
        }

        return this.altarRecipeGrid.containsInputs(altar.getInventory(), true);
    }

    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {}

    public void serializeAdditionalJson(JsonObject recipeObject) {}

    public void writeRecipeSync(FriendlyByteBuf buf) {}

    public void readRecipeSync(FriendlyByteBuf buf) {}

    public static SimpleAltarRecipe read(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        AltarType type = ByteBufUtils.readEnumValue(buffer, AltarType.class);
        int duration = buffer.readInt();
        int starlight = buffer.readInt();
        AltarRecipeGrid grid = AltarRecipeGrid.read(buffer);
        SimpleAltarRecipe recipe = new SimpleAltarRecipe(recipeId, type, duration, starlight, grid);
        ResourceLocation customType = ByteBufUtils.readOptional(buffer, ByteBufUtils::readResourceLocation);
        if (customType != null) {
            //TODO_PORT[AS-ALT-03]: 1.16.5 additionally re-typed the recipe here
            // (1.16.5 common/crafting/recipe/SimpleAltarRecipe.java:235):
            //   recipe = AltarRecipeTypeHandler.convert(recipe, customType);
            // This tree's common/crafting/AltarRecipeTypeHandler is declared as
            // <T extends AltarRecipe> T convert(AltarRecipe, ResourceLocation) because its seven builtin
            // converters (AttunementUpgradeRecipe etc.) all extend the divergent common.crafting.AltarRecipe,
            // which is a sibling of - not a supertype of - SimpleAltarRecipe. Re-pointing the handler at
            // SimpleAltarRecipe would break AltarRecipeSerializer and the builtin recipes, so the custom
            // type is only recorded on the recipe here; the subclass swap is deferred.
            recipe.setCustomRecipeType(customType);
        }

        List<ItemStack> outputs = ByteBufUtils.readList(buffer, ByteBufUtils::readItemStack);
        outputs.forEach(recipe::addOutput);
        //1.16.5 used ByteBufUtils.readRegistryEntry, which resolved the value through
        //GameRegistry.findRegistry(IForgeRegistryEntry#getRegistryType). IForgeRegistryEntry is gone in
        //1.20.1, so the registry name is written/read explicitly and resolved against the same registry
        //the value came from - identical wire content (a ResourceLocation) and identical result.
        recipe.setFocusConstellation(ByteBufUtils.readOptional(buffer,
                buf -> ConstellationRegistry.getConstellation(ByteBufUtils.readResourceLocation(buf))));
        ByteBufUtils.readList(buffer, Ingredient::fromNetwork).forEach(recipe::addRelayInput);
        List<AltarRecipeEffect> effects = ByteBufUtils.readList(buffer,
                buf -> RegistriesAS.REGISTRY_ALTAR_EFFECTS.getValue(ByteBufUtils.readResourceLocation(buf)));
        for (AltarRecipeEffect effect : effects) {
            recipe.addAltarEffect(effect);
        }
        recipe.readRecipeSync(buffer);
        return recipe;
    }

    public final void write(FriendlyByteBuf buffer) {
        ByteBufUtils.writeEnumValue(buffer, this.getAltarType());
        buffer.writeInt(this.getDuration());
        buffer.writeInt(this.getStarlightRequirement());
        this.getInputs().write(buffer);
        ByteBufUtils.writeOptional(buffer, this.getCustomRecipeType(), ByteBufUtils::writeResourceLocation);

        ByteBufUtils.writeCollection(buffer, this.outputs, ByteBufUtils::writeItemStack);
        //See read(): the 1.16.5 ByteBufUtils.writeRegistryEntry is replaced by an explicit registry name.
        ByteBufUtils.writeOptional(buffer, this.getFocusConstellation(),
                (buf, cst) -> ByteBufUtils.writeResourceLocation(buf, cst.getRegistryName()));
        ByteBufUtils.writeCollection(buffer, this.getRelayInputs(), (buf, ingredient) -> ingredient.getIngredient().toNetwork(buf));
        ByteBufUtils.writeCollection(buffer, this.getCraftingEffects(),
                (buf, effect) -> ByteBufUtils.writeResourceLocation(buf, effect.getRegistryName()));
        this.writeRecipeSync(buffer);
    }

    public final void write(JsonObject object) {
        object.addProperty("altar_type", this.getAltarType().ordinal());
        object.addProperty("duration", this.getDuration());
        object.addProperty("starlight", this.getStarlightRequirement());
        this.getInputs().serialize(object);

        if (this.getCustomRecipeType() != null) {
            object.addProperty("recipe_class", this.getCustomRecipeType().toString());
        }

        JsonArray outputs = new JsonArray();
        for (ItemStack output : this.outputs) {
            outputs.add(JsonHelper.serializeItemStack(output));
        }
        object.add("output", outputs);

        JsonObject options = new JsonObject();
        this.serializeAdditionalJson(options);
        if (!options.entrySet().isEmpty()) {
            object.add("options", options);
        }

        if (this.getFocusConstellation() != null) {
            object.addProperty("focus_constellation", this.getFocusConstellation().getRegistryName().toString());
        }

        if (!this.getRelayInputs().isEmpty()) {
            JsonArray inputs = new JsonArray();
            for (WrappedIngredient traitInput : this.getRelayInputs()) {
                //1.16.5: Ingredient#serialize() - renamed to toJson() in 1.20.1
                inputs.add(traitInput.getIngredient().toJson());
            }
            object.add("relay_inputs", inputs);
        }

        if (!this.getCraftingEffects().isEmpty()) {
            JsonArray effects = new JsonArray();
            for (AltarRecipeEffect effect : this.getCraftingEffects()) {
                effects.add(effect.getRegistryName().toString());
            }
            object.add("effects", effects);
        }
    }

    public AltarType getAltarType() {
        return altarType;
    }

    @Override
    public RecipeType<?> getType() {
        //1.16.5: RecipeTypesAS.TYPE_ALTAR.getType(), where TYPE_ALTAR was a
        //ResolvingRecipeType<IItemHandler, SimpleAltarRecipe, SimpleAltarRecipeContext> held in common/lib.
        //This tree registers the altar recipe type through the DeferredRegister in
        //common/crafting/RecipeTypesAS; RecipeType identity - which is all Recipe#getType() is used for -
        //is the same object either way.
        //TODO_PORT[AS-ALT-04]: re-point at a ResolvingRecipeType once common/crafting/helper/ResolvingRecipeType
        // is finished (its findRecipe/getAllRecipes are still stubs) and common/lib gains TYPE_ALTAR.
        return RecipeTypesAS.ALTAR.get();
    }

    @Override
    public CustomRecipeSerializer<?> getSerializer() {
        //1.16.5: RecipeSerializersAS.ALTAR_RECIPE_SERIALIZER (common/lib). common/lib is not owned by this
        //slice and this tree's common/crafting/RecipeSerializersAS already binds the "altar" serializer id
        //to the divergent AltarRecipeSerializer, so the instance is held on the serializer class itself.
        return SimpleAltarRecipeSerializer.INSTANCE;
    }
}
