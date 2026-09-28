/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.BlockAltar;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarAttunement;
import hellfirepvp.astralsorcery.common.container.ContainerAltarConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerAltarDiscovery;
import hellfirepvp.astralsorcery.common.container.ContainerAltarTrait;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import hellfirepvp.astralsorcery.common.integration.jei.*;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationJEI
 * Created by HellFirePvP
 * Date: 25.07.2020 / 09:23
 */
//This class is only ever loaded on the client: JEI finds @JeiPlugin classes from
//mezz.jei.forge.startup.ForgePluginFinder, which only mezz.jei.forge.JustEnoughItemsClient calls, and
//mezz.jei.forge.JustEnoughItems creates that through DistExecutor.safeRunWhenOn(Dist.CLIENT, ..). Nothing in
//the mod references this class either, so it is also never loaded when JEI is absent - that is the presence
//guard here, the same one 1.16.5 relied on (common/base/Mods.JEI is only needed where common code has to ask).
//The class itself is deliberately *not* @OnlyIn(Dist.CLIENT), because none of its own code touches
//net.minecraft.client.**; everything that does lives in common/integration/jei, which is annotated.
@JeiPlugin
public class IntegrationJEI implements IModPlugin {

    public static final List<JEICategory<?>> CATEGORIES = new ArrayList<>();

    //1.16.5 held these as bare ResourceLocations (1.16.5 IntegrationJEI.java:52-59) and handed them to
    //addRecipes/addRecipeCatalyst/addRecipeTransferHandler. JEI 15 takes a
    //mezz.jei.api.recipe.RecipeType<T> - the id plus the recipe class - in all three places, so they are
    //declared as RecipeTypes here and the ids stay byte-for-byte the same ("astralsorcery:altar_discovery"
    //and friends), which is what the jei.category.astralsorcery.* lang keys are derived from.
    public static final RecipeType<AltarRecipe> CATEGORY_ALTAR_ATTUNEMENT = category("altar_attunement", AltarRecipe.class);
    public static final RecipeType<AltarRecipe> CATEGORY_ALTAR_CONSTELLATION = category("altar_constellation", AltarRecipe.class);
    public static final RecipeType<AltarRecipe> CATEGORY_ALTAR_DISCOVERY = category("altar_discovery", AltarRecipe.class);
    public static final RecipeType<AltarRecipe> CATEGORY_ALTAR_TRAIT = category("altar_trait", AltarRecipe.class);
    public static final RecipeType<LiquidInfusion> CATEGORY_INFUSER = category("infuser", LiquidInfusion.class);
    public static final RecipeType<LiquidInteraction> CATEGORY_LIQUID_INTERACTION = category("interaction", LiquidInteraction.class);
    public static final RecipeType<BlockTransmutation> CATEGORY_TRANSMUTATION = category("transmutation", BlockTransmutation.class);
    public static final RecipeType<WellLiquefaction> CATEGORY_WELL = category("well", WellLiquefaction.class);

    @Nullable
    public static IJeiRuntime runtime = null;

    private static <T> RecipeType<T> category(String path, Class<? extends T> recipeClass) {
        return new RecipeType<>(AstralSorcery.key(path), recipeClass);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registry) {
        registry.useNbtForSubtypes(
                ItemsAS.ATTUNED_ROCK_CRYSTAL.get(),
                ItemsAS.ATTUNED_CELESTIAL_CRYSTAL.get(),
                // 1.16.5: Item.getItemFromBlock(Block) -> Block#asItem() in 1.20.1, and the blocks are
                // RegistryObjects here.
                BlocksAS.ROCK_COLLECTOR_CRYSTAL.get().asItem(),
                BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get().asItem(),
                BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get().asItem(),
                BlocksAS.GEM_CRYSTAL_CLUSTER.get().asItem()
        );

        //1.16.5 passed a String-returning lambda (a JEI 7 ISubtypeInterpreter). JEI 15 splits that interface
        //in two: ISubtypeInterpreter#getSubtypeData returns an opaque key object, while
        //IIngredientSubtypeInterpreter#apply is still the String form and takes a UidContext. The String form
        //is the closer match for both of these, so it is the one used.
        //TODO_PORT[AS-INT-06] claimed these two needed cross-slice API this tree does not have. That is no
        //longer true and the claim is dropped: ItemResonator#getUpgrades(ItemStack) and
        //ItemResonator.ResonatorUpgrade#getAppendix() both exist (common/item/ItemResonator.java:320, :355),
        //and ItemMantle#getConstellation(ItemStack) exists (common/item/armor/ItemMantle.java:207) - it only
        //needs the RegistryObject unwrapped and cast, because ItemsAS.MANTLE is a RegistryObject<Item>.
        registry.registerSubtypeInterpreter(ItemsAS.RESONATOR.get(),
                (IIngredientSubtypeInterpreter<ItemStack>) (stack, context) -> ItemResonator.getUpgrades(stack)
                        .stream()
                        .map(ItemResonator.ResonatorUpgrade::getAppendix)
                        .collect(Collectors.joining(",")));
        registry.registerSubtypeInterpreter(ItemsAS.MANTLE.get(),
                (IIngredientSubtypeInterpreter<ItemStack>) (stack, context) -> Optional
                        .ofNullable(((ItemMantle) ItemsAS.MANTLE.get()).getConstellation(stack))
                        .map(IConstellation::getSimpleName)
                        .orElse("none"));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();

        if (CATEGORIES.isEmpty()) {
            CATEGORIES.add(new CategoryAltar(CATEGORY_ALTAR_DISCOVERY, "altar_discovery", (BlockAltar) BlocksAS.ALTAR_DISCOVERY.get(), guiHelper));
            CATEGORIES.add(new CategoryAltar(CATEGORY_ALTAR_ATTUNEMENT, "altar_attunement", (BlockAltar) BlocksAS.ALTAR_ATTUNEMENT.get(), guiHelper));
            CATEGORIES.add(new CategoryAltar(CATEGORY_ALTAR_CONSTELLATION, "altar_constellation", (BlockAltar) BlocksAS.ALTAR_CONSTELLATION.get(), guiHelper));
            CATEGORIES.add(new CategoryAltar(CATEGORY_ALTAR_TRAIT, "altar_trait", (BlockAltar) BlocksAS.ALTAR_RADIANCE.get(), guiHelper));
            CATEGORIES.add(new CategoryInfuser(guiHelper));
            CATEGORIES.add(new CategoryLiquidInteraction(guiHelper));
            CATEGORIES.add(new CategoryTransmutation(guiHelper));
            CATEGORIES.add(new CategoryWell(guiHelper));
        }

        CATEGORIES.forEach(registry::addRecipeCategories);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registry) {
        CATEGORIES.forEach(category -> addRecipes(registry, category));
    }

    /**
     * 1.16.5 inlined this in registerRecipes as
     * {@code registry.addRecipes(recipes, category.getUid())} (1.16.5 IntegrationJEI.java:103-107). JEI 15's
     * addRecipes(RecipeType&lt;T&gt;, List&lt;T&gt;) ties the two arguments together with a type variable, and
     * CATEGORIES is a List&lt;JEICategory&lt;?&gt;&gt;, so the wildcard has to be captured by a generic method
     * before the call type-checks. Everything else is 1.16.5's.
     */
    private static <T extends Recipe<?>> void addRecipes(IRecipeRegistration registry, JEICategory<T> category) {
        List<T> recipes = category.getRecipes();
        // The comparator's parameter is spelled out because T is a wildcard capture at the call site.
        recipes.sort(Comparator.comparing((Recipe<?> recipe) -> recipe.getId().toString()));
        registry.addRecipes(category.getRecipeType(), recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        //1.16.5 listed the eight (stack, category) pairs by hand (1.16.5 IntegrationJEI.java:112-119). Each
        //category now carries its own catalyst - the very stack it already built its icon from - so the list
        //cannot drift out of step with the categories and this walks CATEGORIES like registerRecipes does.
        CATEGORIES.forEach(category -> registry.addRecipeCatalyst(category.getCatalyst(), category.getRecipeType()));
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
        //1.16.5 also pulled an IStackHelper off the registry and handed it to every handler; JEI 15's own
        //transfer handler resolves that internally, so only the transfer helper is needed here.
        IRecipeTransferHandlerHelper transferHelper = registry.getTransferHelper();

        //A lower-tier category deliberately gets a handler for every altar container that can craft it, which
        //is why ContainerAltarAttunement/Constellation/Trait also appear under the T1 block. The int is the
        //number of crafting slots the container binds.

        // T1 recipes
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarDiscovery.class,
                CATEGORY_ALTAR_DISCOVERY, transferHelper, 9), CATEGORY_ALTAR_DISCOVERY);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarAttunement.class,
                CATEGORY_ALTAR_DISCOVERY, transferHelper, 13), CATEGORY_ALTAR_DISCOVERY);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarConstellation.class,
                CATEGORY_ALTAR_DISCOVERY, transferHelper, 21), CATEGORY_ALTAR_DISCOVERY);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarTrait.class,
                CATEGORY_ALTAR_DISCOVERY, transferHelper, 25), CATEGORY_ALTAR_DISCOVERY);

        // T2 recipes
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarAttunement.class,
                CATEGORY_ALTAR_ATTUNEMENT, transferHelper, 13), CATEGORY_ALTAR_ATTUNEMENT);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarConstellation.class,
                CATEGORY_ALTAR_ATTUNEMENT, transferHelper, 21), CATEGORY_ALTAR_ATTUNEMENT);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarTrait.class,
                CATEGORY_ALTAR_ATTUNEMENT, transferHelper, 25), CATEGORY_ALTAR_ATTUNEMENT);

        // T3 recipes
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarConstellation.class,
                CATEGORY_ALTAR_CONSTELLATION, transferHelper, 21), CATEGORY_ALTAR_CONSTELLATION);
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarTrait.class,
                CATEGORY_ALTAR_CONSTELLATION, transferHelper, 25), CATEGORY_ALTAR_CONSTELLATION);

        // T4 recipes
        registry.addRecipeTransferHandler(new TieredAltarRecipeTransferHandler<>(ContainerAltarTrait.class,
                CATEGORY_ALTAR_TRAIT, transferHelper, 25), CATEGORY_ALTAR_TRAIT);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    /**
     * No 1.16.5 counterpart - JEI 7 had no such callback. JEI 15 tears the runtime down on a resource reload
     * and builds a new one, so without this the static field would keep the dead one alive.
     */
    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return AstralSorcery.key("jei_integration");
    }
}
