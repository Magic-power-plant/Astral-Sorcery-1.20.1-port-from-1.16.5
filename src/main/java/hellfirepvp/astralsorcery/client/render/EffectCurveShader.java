package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class EffectCurveShader {

    private static ShaderInstance shader;
    private static ShaderInstance additiveShader;

    private EffectCurveShader() {}

    public static void register(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), AstralSorcery.key("effect_curve"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), loaded -> shader = loaded);
            event.registerShader(new ShaderInstance(event.getResourceProvider(), AstralSorcery.key("effect_curve_additive"),
                    DefaultVertexFormat.POSITION_COLOR_TEX), loaded -> additiveShader = loaded);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not register effect curve shaders", exception);
        }
    }

    public static void bind(Blending blending) {
        RenderSystem.setShader(() -> blending == Blending.ADDITIVE_ALPHA ? additiveShader : shader);
    }
}
