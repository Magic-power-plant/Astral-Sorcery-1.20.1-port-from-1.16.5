/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Random;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectCustomTexture
 * Created by HellFirePvP
 * Date: 26.08.2019 / 19:18
 */
public abstract class EffectCustomTexture extends MobEffect {

    protected static final Random rand = new Random();
    private final Color colorAsObj;

    // 1.16.5 EffectType -> 1.20.1 MobEffectCategory
    public EffectCustomTexture(MobEffectCategory type, Color color) {
        super(type, color.getRGB());
        this.colorAsObj = color;
    }

    public void attachEventListeners(IEventBus bus) {}

    public abstract SpriteQuery getSpriteQuery();

    /**
     * 1.16.5 overrode Effect#renderInventoryEffect / Effect#renderHUDEffect directly; 1.20.1 Forge moved
     * both hooks onto IClientMobEffectExtensions, which is handed out through
     * {@link MobEffect#initializeClient(Consumer)} and is only ever loaded on the client.
     */
    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new IClientMobEffectExtensions() {
            @Override
            public boolean renderInventoryIcon(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, GuiGraphics guiGraphics, int x, int y, int blitOffset) {
                EffectCustomTexture.this.renderInventoryEffect(effect, gui, guiGraphics.pose(), x, y, blitOffset);
                return true;
            }

            @Override
            public boolean renderGuiIcon(MobEffectInstance effect, Gui gui, GuiGraphics guiGraphics, int x, int y, float z, float alpha) {
                EffectCustomTexture.this.renderHUDEffect(effect, gui, guiGraphics.pose(), x, y, z, alpha);
                return true;
            }
        });
    }

    // 1.16.5 DisplayEffectsScreen -> 1.20.1 EffectRenderingInventoryScreen, MatrixStack -> PoseStack
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, PoseStack renderStack, int x, int y, float z) {
        float wh = 18;
        float offsetX = x + 6;
        float offsetY = y + 7;
        float red =   ((float) this.colorAsObj.getRed())   / 255F;
        float green = ((float) this.colorAsObj.getGreen()) / 255F;
        float blue =  ((float) this.colorAsObj.getBlue())  / 255F;

        SpriteSheetResource ssr = getSpriteQuery().resolveSprite();
        ssr.bindTexture();

        Tuple<Float, Float> uvTpl = ssr.getUVOffset(ClientScheduler.getClientTick());
        // 1.16.5 DefaultVertexFormats -> 1.20.1 DefaultVertexFormat
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offsetX, offsetY, z, wh, wh)
                    .color(red, green, blue, 1F)
                    .tex(uvTpl.getA(), uvTpl.getB(), ssr.getUWidth(), ssr.getVWidth())
                    .draw();
        });
    }

    // 1.16.5 AbstractGui -> 1.20.1 Gui (the in-game overlay), MatrixStack -> PoseStack
    @OnlyIn(Dist.CLIENT)
    public void renderHUDEffect(MobEffectInstance effect, Gui gui, PoseStack renderStack, int x, int y, float z, float alpha) {
        float wh = 18;
        float offsetX = x + 3;
        float offsetY = y + 3;
        float red =   ((float) this.colorAsObj.getRed())   / 255F;
        float green = ((float) this.colorAsObj.getGreen()) / 255F;
        float blue =  ((float) this.colorAsObj.getBlue())  / 255F;

        SpriteSheetResource ssr = getSpriteQuery().resolveSprite();
        ssr.bindTexture();

        Tuple<Float, Float> uvTpl = ssr.getUVOffset(ClientScheduler.getClientTick());
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buf -> {
            RenderingGuiUtils.rect(buf, renderStack, offsetX, offsetY, z, wh, wh)
                    .color(red, green, blue, 1F)
                    .tex(uvTpl.getA(), uvTpl.getB(), ssr.getUWidth(), ssr.getVWidth())
                    .draw();
        });
    }
}
