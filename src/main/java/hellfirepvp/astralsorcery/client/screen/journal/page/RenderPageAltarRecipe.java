/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.crafting.AltarUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageAltarRecipe
 * Created by HellFirePvP
 * Date: 12.10.2019 / 10:49
 */
public class RenderPageAltarRecipe extends RenderPageRecipeTemplate {

    //1.16.5 stored the SimpleAltarRecipe directly. This tree carries two parallel altar recipe classes that share
    //no supertype - common.crafting.AltarRecipe (JournalPageRecipe builds its page from that one) and the
    //1.16.5-named common.crafting.recipe.SimpleAltarRecipe (ScreenJournalConstellationDetail builds its page from
    //that one) - so the page keeps one constructor per type and reads the recipe through the view below. Every
    //view accessor is a per-frame delegation to the recipe, exactly as the 1.16.5 field accesses were, so nothing
    //is snapshotted at construction time.
    private final AltarRecipeView recipe;
    private final AbstractRenderableTexture gridTexture;

    public RenderPageAltarRecipe(@Nullable ResearchNode node, int nodePage, AltarRecipe recipe) {
        this(node, nodePage, new AltarRecipeWrapper(recipe));
    }

    public RenderPageAltarRecipe(@Nullable ResearchNode node, int nodePage, SimpleAltarRecipe recipe) {
        this(node, nodePage, new SimpleAltarRecipeWrapper(recipe));
    }

    private RenderPageAltarRecipe(@Nullable ResearchNode node, int nodePage, AltarRecipeView recipe) {
        super(node, nodePage);
        this.recipe = recipe;
        this.gridTexture = this.getGridTexture(recipe);
    }

    private AbstractRenderableTexture getGridTexture(AltarRecipeView recipe) {
        switch (recipe.getAltarType()) {
            case DISCOVERY:
                return TexturesAS.TEX_GUI_BOOK_GRID_T1;
            case ATTUNEMENT:
                return TexturesAS.TEX_GUI_BOOK_GRID_T2;
            case CONSTELLATION:
                return TexturesAS.TEX_GUI_BOOK_GRID_T3;
            case RADIANCE:
                return TexturesAS.TEX_GUI_BOOK_GRID_T4;
        }
        return TexturesAS.TEX_GUI_BOOK_GRID_T4;
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    @Override
    public void render(PoseStack renderStack, float x, float y, float z, float pTicks, float mouseX, float mouseY) {
        this.clearFrameRectangles();

        this.renderRecipeGrid(renderStack, x, y, z, this.gridTexture);
        this.renderExpectedItemStackOutput(renderStack, x + 78, y + 25, z, 1.4F,
                this.recipe.getOutputForRender(Collections.emptyList()));
        this.renderInfoStar(renderStack, x, y, z, pTicks);
        this.renderRequiredConstellation(renderStack, x, y, z, this.recipe.getFocusConstellation());

        int widthShift  = (AltarRecipeGrid.GRID_SIZE - recipe.getInputs().getWidth())  / 2;
        int heightShift = (AltarRecipeGrid.GRID_SIZE - recipe.getInputs().getHeight()) / 2;

        AltarType type = this.recipe.getAltarType();
        float recipeX = x + 30;
        float recipeY = y + 78;
        for (int xx = 0; xx < AltarRecipeGrid.GRID_SIZE; xx++) {
            for (int yy = 0; yy < AltarRecipeGrid.GRID_SIZE; yy++) {
                int slot = xx + yy * AltarRecipeGrid.GRID_SIZE;

                if (!type.hasSlot(slot)) {
                    continue;
                }

                int recipeIndex = (xx - widthShift) + (yy - heightShift) * AltarRecipeGrid.GRID_SIZE;
                if (recipeIndex >= 0 && recipeIndex < AltarRecipeGrid.MAX_INVENTORY_SIZE) {
                    float renderX = recipeX + 25 * xx;
                    float renderY = recipeY + 25 * yy;

                    this.renderExpectedIngredientInput(renderStack, renderX, renderY, z, 1.1F, recipeIndex * 20, this.recipe.getInputs().getIngredient(recipeIndex));
                }
            }
        }
        this.renderExpectedRelayInputs(renderStack, x, y, z + 150, this.recipe.getRelayInputs());
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseZ) {
        return this.handleRecipeNameCopyClick(mouseX, mouseZ, this.recipe.getId()) || this.handleBookLookupClick(mouseX, mouseZ);
    }

    @Override
    public void postRender(PoseStack renderStack, float x, float y, float z, float pTicks, float mouseX, float mouseY) {
        this.renderHoverTooltips(renderStack, mouseX, mouseY, z, this.recipe.getId());
        this.renderInfoStarTooltips(renderStack, x, y, z, mouseX, mouseY, (toolTip) -> {
            this.addAltarRecipeTooltip(this.recipe.getStarlightRequirement(), this.recipe.isUpgradeRecipe(), toolTip);
            this.addConstellationInfoTooltip(this.recipe.getFocusConstellation(), toolTip);
        });
        super.postRender(renderStack, x, y, z, pTicks, mouseX, mouseY);
    }

    //The set of recipe accessors the 1.16.5 page used on SimpleAltarRecipe, so both altar recipe classes of this
    //tree can drive the very same render path. Named after the 1.16.5 getters.
    private interface AltarRecipeView {

        AltarType getAltarType();

        AltarRecipeGrid getInputs();

        ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents);

        @Nullable
        IConstellation getFocusConstellation();

        List<WrappedIngredient> getRelayInputs();

        int getStarlightRequirement();

        boolean isUpgradeRecipe();

        ResourceLocation getId();
    }

    private static class AltarRecipeWrapper implements AltarRecipeView {

        private final AltarRecipe recipe;

        private AltarRecipeWrapper(AltarRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public AltarType getAltarType() {
            return this.recipe.getAltarType();
        }

        @Override
        public AltarRecipeGrid getInputs() {
            //1.16.5: SimpleAltarRecipe#getInputs() -> this class names the same grid getGrid()
            return this.recipe.getGrid();
        }

        @Override
        public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
            return this.recipe.getOutputForRender(inventoryContents);
        }

        @Nullable
        @Override
        public IConstellation getFocusConstellation() {
            return this.recipe.getFocusConstellation();
        }

        @Override
        public List<WrappedIngredient> getRelayInputs() {
            return this.recipe.getRelayInputs();
        }

        @Override
        public int getStarlightRequirement() {
            //1.16.5: SimpleAltarRecipe#getStarlightRequirement() -> this class names the same field getStarlightRequired()
            return this.recipe.getStarlightRequired();
        }

        @Override
        public boolean isUpgradeRecipe() {
            return this.recipe instanceof AltarUpgradeRecipe;
        }

        @Override
        public ResourceLocation getId() {
            return this.recipe.getId();
        }
    }

    private static class SimpleAltarRecipeWrapper implements AltarRecipeView {

        private final SimpleAltarRecipe recipe;

        private SimpleAltarRecipeWrapper(SimpleAltarRecipe recipe) {
            this.recipe = recipe;
        }

        @Override
        public AltarType getAltarType() {
            return this.recipe.getAltarType();
        }

        @Override
        public AltarRecipeGrid getInputs() {
            return this.recipe.getInputs();
        }

        @Override
        public ItemStack getOutputForRender(Iterable<ItemStack> inventoryContents) {
            return this.recipe.getOutputForRender(inventoryContents);
        }

        @Nullable
        @Override
        public IConstellation getFocusConstellation() {
            return this.recipe.getFocusConstellation();
        }

        @Override
        public List<WrappedIngredient> getRelayInputs() {
            return this.recipe.getRelayInputs();
        }

        @Override
        public int getStarlightRequirement() {
            return this.recipe.getStarlightRequirement();
        }

        @Override
        public boolean isUpgradeRecipe() {
            return this.recipe instanceof AltarUpgradeRecipe;
        }

        @Override
        public ResourceLocation getId() {
            return this.recipe.getId();
        }
    }
}
