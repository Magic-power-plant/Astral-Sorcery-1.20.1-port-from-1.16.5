/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.container.ContainerAltarBase;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.item.util.ItemStackHandlerAS;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ScreenContainerAltar
 * Created by HellFirePvP
 * Date: 15.08.2019 / 17:08
 */
public abstract class ScreenContainerAltar<T extends ContainerAltarBase> extends ScreenCustomContainer<T> {

    //1.16.5: PlayerInventory -> 1.20.1: Inventory; ITextComponent -> Component
    public ScreenContainerAltar(T screenContainer, Inventory inv, Component name, int width, int height) {
        super(screenContainer, inv, name, width, height);
    }

    /**
     * 1.16.5 ran this through {@code RecipeTypesAS.TYPE_ALTAR.findRecipe(new SimpleAltarRecipeContext(player,
     * LogicalSide.CLIENT, altar).setIgnoreStarlightRequirement(flag))}, i.e. "first altar recipe whose inputs,
     * altar tier, focus constellation and - unless ignored - starlight level fit, judged against the *client*
     * player progress". This tree replaced SimpleAltarRecipe/SimpleAltarRecipeContext with
     * {@link AltarRecipe}, whose static {@code findRecipe(Level, TileAltar, Player)} hardcodes
     * "do not ignore starlight", so the scan is written out here to keep the flag.
     * <p>
     * AS-SCR-01 (resolved): this passed through the three-argument {@code matches(Player, TileAltar, boolean)},
     * which hardcodes {@code LogicalSide.SERVER}. On the client the player is a LocalPlayer, not a ServerPlayer,
     * so {@code ResearchHelper.getProgress(player, SERVER)} fell through to {@code new PlayerProgressTestAccess()}
     * - an empty progress object - and {@code hasProgressionServer} failed for every recipe. This method therefore
     * returned null on every client frame, which silently killed all four altar screens' ghost output preview and
     * the blue starlight-deficit overlay in {@link #renderStarlightBar}. The side-aware overload restores 1.16.5
     * ScreenContainerAltar.java:50, and client progress really is synced (PktSyncKnowledge ->
     * ResearchHelper.updateClientResearch), so it resolves.
     */
    @Nullable
    public AltarRecipe findRecipe(boolean ignoreStarlightRequirement) {
        AltarBlockEntity ta = getMenu().getBlockEntity();
        Level level = ta.getLevel();
        if (level == null) {
            return null;
        }
        Player player = Minecraft.getInstance().player;
        for (AltarRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypesAS.ALTAR.get())) {
            if (recipe.matches(LogicalSide.CLIENT, player, ta, ignoreStarlightRequirement)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * 1.16.5 handed {@code TileAltar#getInventory()} straight to {@code SimpleAltarRecipe#getOutputForRender(Iterable)}
     * in every altar screen: its TileInventory extended ItemStackHandler <i>and</i> implemented
     * {@code Iterable<ItemStack>}, handing out the backing slot list's iterator verbatim
     * ({@code return this.stacks.iterator()}). This tree's {@link ItemStackHandlerAS} is not Iterable, so the very
     * same sequence - every slot in slot order, empty stacks included - is materialized here instead. Lives on the
     * shared base so the four altar screens keep reading like the reference.
     */
    protected ItemStack getOutputForRender(AltarRecipe recipe) {
        ItemStackHandlerAS inventory = this.getMenu().getBlockEntity().getInventory();
        List<ItemStack> inventoryContents = new ArrayList<>(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventoryContents.add(inventory.getStackInSlot(i));
        }
        return recipe.getOutputForRender(inventoryContents);
    }

    //1.16.5: ContainerScreen#drawGuiContainerBackgroundLayer -> 1.20.1: AbstractContainerScreen#renderBg
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.enableDepthTest();
        this.renderGuiBackground(guiGraphics.pose(), partialTicks, mouseX, mouseY);
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
    }

    //1.16.5: MatrixStack -> 1.20.1: PoseStack
    protected void renderStarlightBar(PoseStack renderStack, int offsetX, int offsetZ, int width, int height) {
        AltarBlockEntity altar = this.getMenu().getBlockEntity();

        //1.16.5 bracketed this method with RenderSystem.disableAlphaTest()/enableAlphaTest(). The fixed-function
        //alpha test is gone in 1.20.1 - the position_color_tex core shader used by RenderingGuiUtils does the alpha
        //handling itself - so both calls drop out without changing the result.

        TexturesAS.TEX_BLACK.bindTexture();
        //1.16.5: DefaultVertexFormats -> 1.20.1: com.mojang.blaze3d.vertex.DefaultVertexFormat
        //1.16.5: ContainerScreen#guiLeft/#guiTop -> 1.20.1: AbstractContainerScreen#leftPos/#topPos
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, leftPos + offsetX, topPos + offsetZ, this.getBlitOffset(), width, height)
                    .draw();
        });

        float percFilled;
        Color barColor;
        if (altar.hasMultiblock()) {
            percFilled = altar.getAmbientStarlightPercent();
            barColor = Color.WHITE;
        } else {
            percFilled = 1.0F;
            barColor = Color.RED;
        }

        if (percFilled > 0) {
            SpriteSheetResource spriteStarlight = SpritesAS.SPR_STARLIGHT_STORE;
            spriteStarlight.getResource().bindTexture();

            int tick = altar.getTicksExisted();
            Tuple<Float, Float> uvOffset = spriteStarlight.getUVOffset(tick);
            RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                RenderingGuiUtils.rect(buf, renderStack, leftPos + offsetX, topPos + offsetZ, this.getBlitOffset(), (int) (width * percFilled), height)
                        .tex(uvOffset.getA(), uvOffset.getB(), spriteStarlight.getULength() * percFilled, spriteStarlight.getVLength())
                        .color(barColor)
                        .draw();
            });

            if (altar.hasMultiblock()) {
                //1.16.5: SimpleAltarRecipe -> this tree: AltarRecipe
                AltarRecipe aar = findRecipe(true);
                if (aar != null) {
                    //1.16.5: SimpleAltarRecipe#getStarlightRequirement() -> AltarRecipe#getStarlightRequired()
                    int req = aar.getStarlightRequired();
                    int has = altar.getStoredStarlight();
                    if (has < req) {
                        int max = altar.getAltarType().getStarlightCapacity();
                        float percReq = (float) (req - has) / (float) max;
                        int from = (int) (width * percFilled);
                        int to = (int) (width * percReq);

                        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
                            RenderingGuiUtils.rect(buf, renderStack, leftPos + offsetX + from, topPos + offsetZ, this.getBlitOffset(), to, height)
                                    .tex(uvOffset.getA() + spriteStarlight.getULength() * percFilled, uvOffset.getB(), spriteStarlight.getULength() * percReq, spriteStarlight.getVLength())
                                    .color(0.2F, 0.5F, 1.0F, 0.4F)
                                    .draw();
                        });
                    }
                }
            }
        }
    }

    public abstract void renderGuiBackground(PoseStack renderStack, float partialTicks, int mouseX, int mouseY);
}
