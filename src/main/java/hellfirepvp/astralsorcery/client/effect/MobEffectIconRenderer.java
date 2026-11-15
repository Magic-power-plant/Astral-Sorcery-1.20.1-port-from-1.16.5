/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.client.util.RenderingGuiUtils;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.effect.mob.EffectCustomTexture;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

/** Inventory/HUD presentation for gameplay effects; never owns gameplay state or event listeners. */
@OnlyIn(Dist.CLIENT)
public final class MobEffectIconRenderer implements IClientMobEffectExtensions {

    private final EffectCustomTexture effect;

    public MobEffectIconRenderer(EffectCustomTexture effect) {
        this.effect = Objects.requireNonNull(effect, "effect");
    }

    @Override
    public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen,
                                       GuiGraphics graphics, int x, int y, int blitOffset) {
        renderIcon(graphics.pose(), x + 6, y + 7, blitOffset);
        return true;
    }

    @Override
    public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics,
                                 int x, int y, float z, float alpha) {
        // Preserve the original fully opaque icon, including during the vanilla expiry blink.
        renderIcon(graphics.pose(), x + 3, y + 3, z);
        return true;
    }

    private void renderIcon(PoseStack pose, float x, float y, float z) {
        int color = effect.getColor();
        float red = ((color >> 16) & 0xFF) / 255F;
        float green = ((color >> 8) & 0xFF) / 255F;
        float blue = (color & 0xFF) / 255F;
        SpriteSheetResource sprite = new SpriteQuery(AssetLoader.TextureLocation.GUI, 1, 1,
                "effect", effect.getIconName()).resolveSprite();
        sprite.bindTexture();
        Tuple<Float, Float> uv = sprite.getUVOffset(ClientScheduler.getClientTick());
        RenderingUtils.draw(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR_TEX, buffer -> {
            RenderingGuiUtils.rect(buffer, pose, x, y, z, 18, 18)
                    .color(red, green, blue, 1F)
                    .tex(uv.getA(), uv.getB(), sprite.getUWidth(), sprite.getVWidth())
                    .draw();
        });
    }
}
