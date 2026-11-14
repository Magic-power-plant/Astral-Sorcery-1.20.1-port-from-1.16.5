/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.page;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.render.IDrawRenderTypeBuffer;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.client.resource.BlockAtlasTexture;
import hellfirepvp.astralsorcery.client.util.*;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupInfo;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarUpgradeRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchNode;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.IngredientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RenderPageRecipeTemplate
 * Created by HellFirePvP
 * Date: 12.10.2019 / 10:53
 */
public abstract class RenderPageRecipeTemplate extends RenderablePage {

    protected Map<Rectangle, Tuple<ItemStack, Ingredient>> thisFrameInputStacks = new HashMap<>();
    protected Tuple<Rectangle, ItemStack> thisFrameOuputStack = null;
    protected Rectangle thisFrameInfoStar = null;

    protected RenderPageRecipeTemplate(@Nullable ResearchNode node, int nodePage) {
        super(node, nodePage);
    }

    protected void clearFrameRectangles() {
        this.thisFrameInputStacks.clear();
        this.thisFrameOuputStack = null;
        this.thisFrameInfoStar = null;
    }

    public void renderRecipeGrid(PoseStack renderStack, float offsetX, float offsetY, float zLevel, AbstractRenderableTexture tex) {
        RenderSystem.enableBlend();
        Blending.DEFAULT.apply();
        tex.bindTexture();
        RenderingGuiUtils.drawRect(renderStack, offsetX + 25, offsetY, zLevel, 129, 202);
        RenderSystem.disableBlend();
    }

    public void renderExpectedIngredientInput(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float scale, long tickOffset, Ingredient ingredient) {
        ItemStack expected = IngredientHelper.getRandomVisibleStack(ingredient, ClientScheduler.getClientTick() + tickOffset);
        if (!expected.isEmpty()) {
            BlockAtlasTexture.getInstance().bindTexture();

            this.renderItemStack(renderStack, offsetX, offsetY, zLevel, scale, expected);
            this.thisFrameInputStacks.put(new Rectangle((int) offsetX, (int) offsetY, (int) (16 * scale), (int) (16 * scale)), new Tuple<>(expected, ingredient));
        }
    }

    public void renderExpectedIngredientInput(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float scale, long tickOffset, List<ItemStack> displayOptions) {
        int mod = (int) (((ClientScheduler.getClientTick() + tickOffset) / 20L) % displayOptions.size());
        //1.16.5: MathHelper -> 1.20.1: Mth
        ItemStack expected = displayOptions.get(Mth.clamp(mod, 0, displayOptions.size() - 1));
        if (!expected.isEmpty()) {
            BlockAtlasTexture.getInstance().bindTexture();

            this.renderItemStack(renderStack, offsetX, offsetY, zLevel, scale, expected);
            this.thisFrameInputStacks.put(new Rectangle((int) offsetX, (int) offsetY, (int) (16 * scale), (int) (16 * scale)), new Tuple<>(expected, null));
        }
    }

    //1.16.5 handed the SimpleAltarRecipe itself to this method. This tree carries two parallel altar recipe
    //classes - common.crafting.AltarRecipe (what JournalPageRecipe builds its page from) and the 1.16.5-named
    //common.crafting.recipe.SimpleAltarRecipe (what ScreenJournalConstellationDetail builds its page from) -
    //and they share no supertype, so the relay render below takes the relay ingredient list that both of them
    //hand out identically. The AltarRecipe overload is kept so existing callers stay source compatible.
    public void renderExpectedRelayInputs(PoseStack renderStack, float offsetX, float offsetY, float zLevel, AltarRecipe altarRecipe) {
        this.renderExpectedRelayInputs(renderStack, offsetX, offsetY, zLevel, altarRecipe.getRelayInputs());
    }

    public void renderExpectedRelayInputs(PoseStack renderStack, float offsetX, float offsetY, float zLevel, List<WrappedIngredient> ingredients) {
        float centerX = offsetX + 80;
        float centerY = offsetY + 128;

        float perc = (ClientScheduler.getClientTick() % 3000) / 3000F;

        int amt = ingredients.size();
        for (int i = 0; i < ingredients.size(); i++) {
            double part = ((double) i) / ((double) amt) * 2.0 * Math.PI; //Shift by half a period
            part = Mth.clamp(part, 0, 2.0 * Math.PI);
            part += (2.0 * Math.PI * perc) + Math.PI;
            double xAdd = Math.sin(part) * 75.0;
            double yAdd = Math.cos(part) * 75.0;

            renderExpectedIngredientInput(renderStack, (float) (centerX + xAdd), (float) (centerY + yAdd), zLevel, 1F, i * 20, ingredients.get(i).getIngredient());
        }
    }

    public void renderExpectedItemStackOutput(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float scale, ItemStack stack) {
        if (!stack.isEmpty()) {
            BlockAtlasTexture.getInstance().bindTexture();

            this.renderItemStack(renderStack, offsetX, offsetY, zLevel, scale, stack);
            this.thisFrameOuputStack = new Tuple<>(new Rectangle((int) offsetX, (int) offsetY, (int) (16 * scale), (int) (16 * scale)), stack);
        }
    }

    protected void renderItemStack(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float scale, ItemStack stack) {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        //1.16.5 wrapped the item draw in RenderHelper#enableStandardItemLighting()/#disableStandardItemLighting().
        //RenderHelper became com.mojang.blaze3d.platform.Lighting in 1.17 and both methods were deleted with it -
        //they toggled the legacy fixed-function GL lighting state, which the core-shader pipeline of 1.20.1 has no
        //counterpart for. The diffuse lighting the item model actually needs is set up inside
        //RenderingUtils#renderTranslucentItemStackModelGUI (Lighting#setupForFlatItems/#setupFor3DItems around the
        //batch), which renderItemStackGUI below routes through, so the two calls drop out.

        renderStack.pushPose();
        renderStack.translate(offsetX, offsetY, zLevel);
        renderStack.scale(scale, scale, 1);
        RenderingUtils.renderItemStackGUI(renderStack, stack, null);
        renderStack.popPose();

        RenderSystem.depthMask(false);
    }

    public boolean handleRecipeNameCopyClick(double mouseX, double mouseZ, AltarRecipe recipe) {
        return this.handleRecipeNameCopyClick(mouseX, mouseZ, recipe.getId());
    }

    //See renderExpectedRelayInputs: only the recipe id is read here, so the two parallel altar recipe classes
    //of this tree both reach the 1.16.5 behaviour through this overload.
    public boolean handleRecipeNameCopyClick(double mouseX, double mouseZ, ResourceLocation recipeId) {
        //1.16.5: Minecraft#gameSettings#showDebugInfo -> 1.20.1: Minecraft#options#renderDebug
        if (Minecraft.getInstance().options.renderDebug &&
                Screen.hasControlDown() &&
                this.thisFrameOuputStack.getA().contains(mouseX, mouseZ)) {
            String recipeName = recipeId.toString();
            //1.16.5: Minecraft#keyboardListener#setClipboardString -> 1.20.1: Minecraft#keyboardHandler#setClipboard
            Minecraft.getInstance().keyboardHandler.setClipboard(recipeName);
            //1.16.5: Entity#sendMessage(ITextComponent, UUID) with Util.DUMMY_UUID for "no sender".
            //1.20.1 split that into the sender-less Entity#sendSystemMessage(Component), so the UUID drops out.
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("astralsorcery.misc.ctrlcopy.copied", recipeName));
            return true;
        }
        return false;
    }

    public boolean handleBookLookupClick(double mouseX, double mouseZ) {
        for (Rectangle r : thisFrameInputStacks.keySet()) {
            if (r.contains(mouseX, mouseZ)) {
                ItemStack stack = thisFrameInputStacks.get(r).getA();
                BookLookupInfo info = BookLookupRegistry.findPage(Minecraft.getInstance().player, LogicalSide.CLIENT, stack);
                if (info != null &&
                        info.canSee(ResearchHelper.getProgress(Minecraft.getInstance().player, LogicalSide.CLIENT)) &&
                        !info.getResearchNode().equals(this.getResearchNode())) {
                    info.openGui();
                    return true;
                }
            }
        }
        if (this.thisFrameOuputStack != null) {
            if (this.thisFrameOuputStack.getA().contains(mouseX, mouseZ)) {
                ItemStack stack = this.thisFrameOuputStack.getB();
                BookLookupInfo info = BookLookupRegistry.findPage(Minecraft.getInstance().player, LogicalSide.CLIENT, stack);
                if (info != null &&
                        info.canSee(ResearchHelper.getProgress(Minecraft.getInstance().player, LogicalSide.CLIENT)) &&
                        !info.getResearchNode().equals(this.getResearchNode())) {
                    info.openGui();
                    return true;
                }
            }
        }
        return false;
    }

    public void renderInfoStar(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float pTicks) {
        renderStack.pushPose();
        renderStack.translate(offsetX + 140, offsetY + 20, zLevel);
        this.thisFrameInfoStar = RenderingDrawUtils.drawInfoStar(renderStack, IDrawRenderTypeBuffer.defaultBuffer(), 15F, pTicks);
        this.thisFrameInfoStar.translate((int) (offsetX + 140), (int) (offsetY + 20));
        renderStack.popPose();
    }

    public void renderRequiredConstellation(PoseStack renderStack, float offsetX, float offsetY, float zLevel, @Nullable IConstellation constellation) {
        if (constellation != null) {
            RenderSystem.enableBlend();
            Blending.DEFAULT.apply();
            RenderingConstellationUtils.renderConstellationIntoGUI(new Color(0xEEEEEE), constellation, renderStack,
                    Math.round(offsetX + 30), Math.round(offsetY + 78), zLevel,
                    125, 125, 2F, () -> 0.4F, true, false);
            RenderSystem.disableBlend();
        }
    }

    //1.16.5: ITextProperties -> 1.20.1: FormattedText
    public void renderInfoStarTooltips(PoseStack renderStack, float offsetX, float offsetY, float zLevel, float mouseX, float mouseY, Consumer<List<FormattedText>> tooltipProvider) {
        if (this.thisFrameInfoStar == null) {
            return;
        }

        if (this.thisFrameInfoStar.contains(mouseX, mouseY)) {
            List<FormattedText> toolTip = new ArrayList<>();
            tooltipProvider.accept(toolTip);
            if (!toolTip.isEmpty()) {
                zLevel += 600;
                RenderingDrawUtils.renderBlueTooltipComponents(renderStack, offsetX, offsetY, zLevel, toolTip, RenderablePage.getFontRenderer(), false);
                zLevel -= 600;
            }
        }
    }

    public void renderHoverTooltips(PoseStack renderStack, float mouseX, float mouseY, float zLevel, ResourceLocation recipeName) {
        List<FormattedText> toolTip = new LinkedList<>();
        addStackTooltip(mouseX, mouseY, recipeName, toolTip);

        if (!toolTip.isEmpty()) {
            zLevel += 800;
            RenderingDrawUtils.renderBlueTooltipComponents(renderStack, mouseX, mouseY, zLevel, toolTip, RenderablePage.getFontRenderer(), true);
            zLevel -= 800;
        }
    }

    protected void addAltarRecipeTooltip(AltarRecipe altarRecipe, List<FormattedText> toolTip) {
        //1.16.5: SimpleAltarRecipe#getStarlightRequirement() -> this tree names the very same field getStarlightRequired()
        this.addAltarRecipeTooltip(altarRecipe.getStarlightRequired(), altarRecipe instanceof AltarUpgradeRecipe, toolTip);
    }

    //See renderExpectedRelayInputs: the tooltip only needs the starlight requirement and whether the recipe is
    //an AltarUpgradeRecipe (a marker interface, as in 1.16.5), so both altar recipe classes route through here.
    protected void addAltarRecipeTooltip(int starlightRequirement, boolean upgradeRecipe, List<FormattedText> toolTip) {
        if (starlightRequirement > 0) {
            AltarType highestPossible = null;
            ProgressionTier reached = ResearchHelper.getClientProgress().getTierReached();
            for (AltarType type : AltarType.values()) {
                if ((highestPossible == null || !type.isThisLEThan(highestPossible)) &&
                        reached.isThisLaterOrEqual(type.getAssociatedTier().getRequiredProgress())) {
                    highestPossible = type;
                }
            }
            if (highestPossible != null) {
                long indexSel = (ClientScheduler.getClientTick() / 30) % (highestPossible.ordinal() + 1);
                AltarType typeSelected = AltarType.values()[((int) indexSel)];
                //1.16.5: ItemStack#getDisplayName() -> 1.20.1: ItemStack#getHoverName()
                FormattedText itemName = typeSelected.getAltarItemRepresentation().getHoverName();
                FormattedText starlightRequired = getAltarStarlightAmountDescription(itemName, starlightRequirement, typeSelected.getStarlightCapacity());
                FormattedText starlightRequirementDescription = Component.translatable("astralsorcery.journal.recipe.altar.starlight.desc");

                toolTip.add(starlightRequirementDescription);
                toolTip.add(starlightRequired);
            }
        }
        if (upgradeRecipe) {
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.altar.upgrade"));
        }
    }

    protected void addConstellationInfoTooltip(@Nullable IConstellation cst, List<FormattedText> toolTip) {
        if (cst != null) {
            toolTip.add(Component.translatable("astralsorcery.journal.recipe.constellation", cst.getConstellationName()));
        }
    }

    protected FormattedText getAltarStarlightAmountDescription(FormattedText altarName, float amountRequired, float maxAmount) {
        String base = "astralsorcery.journal.recipe.altar.starlight.";
        float perc = amountRequired / maxAmount;
        if (perc <= 0.1) {
            base += "lowest";
        } else if (perc <= 0.25) {
            base += "low";
        } else if (perc <= 0.5) {
            base += "avg";
        } else if (perc <= 0.75) {
            base += "more";
        } else if (perc <= 0.9) {
            base += "high";
        } else if (perc > 1) {
            base += "toomuch";
        } else {
            base += "highest";
        }
        return Component.translatable("astralsorcery.journal.recipe.altar.starlight.format",
                altarName,
                Component.translatable(base));
    }

    protected FormattedText getInfuserChanceDescription(float chance) {
        String base = "astralsorcery.journal.recipe.infusion.chance.";
        if (chance <= 0.3) {
            base += "low";
        } else if (chance <= 0.7) {
            base += "average";
        } else if (chance < 1) {
            base += "high";
        } else {
            base += "always";
        }
        return Component.translatable(base);
    }

    protected void addStackTooltip(float mouseX, float mouseY, ResourceLocation recipeName, List<FormattedText> tooltip) {
        for (Rectangle rect : thisFrameInputStacks.keySet()) {
            if (rect.contains(mouseX, mouseY)) {
                Tuple<ItemStack, Ingredient> inputInfo = thisFrameInputStacks.get(rect);
                addInputInformation(inputInfo.getA(), inputInfo.getB(), tooltip);
                return;
            }
        }
        if (this.thisFrameOuputStack.getA().contains(mouseX, mouseY)) {
            ItemStack stack = this.thisFrameOuputStack.getB();
            addInputInformation(stack, null, tooltip);

            if (Minecraft.getInstance().options.renderDebug) {
                //1.16.5: StringTextComponent#EMPTY -> 1.20.1: CommonComponents#EMPTY (same shared empty literal)
                tooltip.add(CommonComponents.EMPTY);
                //1.16.5: IFormattableTextComponent#mergeStyle(TextFormatting) -> 1.20.1: MutableComponent#withStyle(ChatFormatting)
                tooltip.add(Component.translatable("astralsorcery.misc.recipename", recipeName.toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
                tooltip.add(Component.translatable("astralsorcery.misc.ctrlcopy", recipeName.toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
            }
        }
    }

    protected void addInputInformation(ItemStack stack, @Nullable Ingredient stackIngredient, List<FormattedText> tooltip) {
        try {
            //1.16.5: ITooltipFlag.TooltipFlags.ADVANCED/NORMAL (an enum) -> 1.20.1: TooltipFlag.ADVANCED/NORMAL, the two
            //constants the TooltipFlag interface itself holds now that TooltipFlag.Default became a record.
            tooltip.addAll(stack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL));
        } catch (Exception exc) {
            tooltip.add(Component.translatable("astralsorcery.misc.tooltipError").withStyle(ChatFormatting.RED));
        }
        BookLookupInfo info = BookLookupRegistry.findPage(Minecraft.getInstance().player, LogicalSide.CLIENT, stack);
        if (info != null &&
                info.canSee(ResearchHelper.getProgress(Minecraft.getInstance().player, LogicalSide.CLIENT)) &&
                !info.getResearchNode().equals(this.getResearchNode())) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("astralsorcery.misc.craftInformation").withStyle(ChatFormatting.GRAY));
        }
        if (stackIngredient != null && Minecraft.getInstance().options.advancedItemTooltips) {
            //1.16.5 handed out an ITag<Item> and had to downcast to ITag.INamedTag to get at the tag id.
            //1.20.1 replaced the tag object with the always-named TagKey<Item>, so the instanceof narrows to the
            //null check IngredientHelper#guessTag already documents, and the id comes from TagKey#location().
            TagKey<Item> itemTag = IngredientHelper.guessTag(stackIngredient);
            if (itemTag != null) {
                tooltip.add(CommonComponents.EMPTY);
                tooltip.add(Component.translatable("astralsorcery.misc.input.tag",
                        itemTag.location().toString()).withStyle(ChatFormatting.GRAY));
            }
            if (stackIngredient instanceof FluidIngredient) {
                List<FluidStack> fluids = ((FluidIngredient) stackIngredient).getFluids();

                if (!fluids.isEmpty()) {
                    FormattedText cmp = null;
                    for (FluidStack f : fluids) {
                        //1.16.5: FluidStack#getFluid()#getAttributes()#getDisplayName(stack). Forge dropped
                        //FluidAttributes in 1.19; the name now comes straight off the stack via FluidStack#getDisplayName().
                        if (cmp == null) {
                            cmp = f.getDisplayName();
                        } else {
                            cmp = Component.translatable("astralsorcery.misc.input.fluid.chain", cmp, f.getDisplayName()).withStyle(ChatFormatting.GRAY);
                        }
                    }
                    tooltip.add(CommonComponents.EMPTY);
                    tooltip.add(Component.translatable("astralsorcery.misc.input.fluid", cmp).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }
}
