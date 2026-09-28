/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageLiquidInfusion
 * Created by HellFirePvP
 * Date: 29.05.2020 / 00:22
 */
public class RenderPageLiquidInfusion extends RenderPageRecipeTemplate {

    private final LiquidInfusion recipe;

    public RenderPageLiquidInfusion(@Nullable ResearchNode node, int nodePage, LiquidInfusion recipe) {
        super(node, nodePage);
        this.recipe = recipe;
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    @Override
    public void render(PoseStack renderStack, float x, float y, float z, float pTicks, float mouseX, float mouseY) {
        this.clearFrameRectangles();

        this.renderRecipeGrid(renderStack, x, y, z, TexturesAS.TEX_GUI_BOOK_GRID_INFUSION);
        this.renderExpectedItemStackOutput(renderStack, x + 78, y + 25, z, 1.4F, this.recipe.getOutput(ItemStack.EMPTY));
        this.renderInfoStar(renderStack, x, y, z, pTicks);

        float renderX = x + 80;
        float renderY = y + 128;
        //1.16.5 addressed the block directly through BlocksAS; in 1.20.1 BlocksAS holds RegistryObject<Block>,
        //so the registered instance has to be unwrapped first.
        this.renderItemStack(renderStack, renderX, renderY + 15, z, 1.2F, new ItemStack(BlocksAS.INFUSER.get()));
        this.renderExpectedIngredientInput(renderStack, renderX, renderY, z, 1.2F, 0, this.recipe.getItemInput());

        BlockAtlasTexture.getInstance().bindTexture();
        //1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1: FluidType.BUCKET_VOLUME (Forge dropped FluidAttributes in 1.19)
        TextureAtlasSprite tas = RenderingUtils.getParticleTexture(new FluidStack(this.recipe.getLiquidInput(), FluidType.BUCKET_VOLUME));
        //1.16.5: DefaultVertexFormats -> 1.20.1: DefaultVertexFormat
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            renderStack.pushPose(); //1.16.5: MatrixStack#push()
            renderStack.translate(x, y, z);
            this.renderLiquidInput(buf, renderStack, tas, 1, 0);
            this.renderLiquidInput(buf, renderStack, tas, 2, 0);
            this.renderLiquidInput(buf, renderStack, tas, 3, 0);
            this.renderLiquidInput(buf, renderStack, tas, 1, 4);
            this.renderLiquidInput(buf, renderStack, tas, 2, 4);
            this.renderLiquidInput(buf, renderStack, tas, 3, 4);
            this.renderLiquidInput(buf, renderStack, tas, 0, 1);
            this.renderLiquidInput(buf, renderStack, tas, 0, 2);
            this.renderLiquidInput(buf, renderStack, tas, 0, 3);
            this.renderLiquidInput(buf, renderStack, tas, 4, 1);
            this.renderLiquidInput(buf, renderStack, tas, 4, 2);
            this.renderLiquidInput(buf, renderStack, tas, 4, 3);
            renderStack.popPose(); //1.16.5: MatrixStack#pop()
        });
    }

    //1.16.5: net.minecraft.client.renderer.BufferBuilder -> 1.20.1: com.mojang.blaze3d.vertex.BufferBuilder
    private void renderLiquidInput(BufferBuilder buf, PoseStack renderStack, TextureAtlasSprite tas, int x, int y) {
        RenderingGuiUtils.rect(buf, renderStack, 28 + x * 25.15F, 76 + y * 25.15F, 0, 22.3F, 22.3F)
                .tex(tas)
                .draw();
    }

    @Override
    public boolean propagateMouseClick(double mouseX, double mouseZ) {
        return this.handleBookLookupClick(mouseX, mouseZ);
    }

    @Override
    public void postRender(PoseStack renderStack, float x, float y, float z, float pTicks, float mouseX, float mouseY) {
        this.renderHoverTooltips(renderStack, mouseX, mouseY, z, this.recipe.getId());
        this.renderInfoStarTooltips(renderStack, x, y, z, mouseX, mouseY, (toolTip) -> {
            //1.16.5: new TranslationTextComponent(..) -> 1.20.1: Component.translatable(..)
            //1.16.5 read the fluid name off Fluid#getAttributes()#getDisplayName(FluidStack); Forge removed
            //FluidAttributes in 1.19, so the name now comes straight off the stack via FluidStack#getDisplayName().
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.liquid",
                    new FluidStack(this.recipe.getLiquidInput(), FluidType.BUCKET_VOLUME).getDisplayName()));
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.chance.format",
                    this.getInfuserChanceDescription(this.recipe.getConsumptionChance())));
            if (this.recipe.doesConsumeMultipleFluids()) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.multiple"));
            }
            if (!this.recipe.acceptsChaliceInput() && ResearchHelper.getClientProgress().getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT)) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.no_chalice"));
            }
            if (this.recipe.doesCopyNBTToOutputs()) {
                toolTip.add(Component.translatable("astralsorcery.journal.recipe.infusion.copy_nbt"));
            }
        });
    }
}
