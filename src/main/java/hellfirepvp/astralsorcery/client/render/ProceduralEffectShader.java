package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.client.resource.ProceduralEffect;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ProceduralEffectShader {

    private static final Map<String, ShaderInstance> SHADERS = new HashMap<>();

    private ProceduralEffectShader() {}

    public static void register(RegisterShadersEvent event) {
        try {
            for (String program : Arrays.stream(ProceduralEffect.values()).map(effect -> effect.program).distinct().toList()) {
                var resources = event.getResourceProvider();
                var coefficients = AstralSorcery.key("shaders/include/" + program.substring("effect_".length()) + "_coefficients.glsl");
                boolean usesProfiles = GL11.glGetInteger(GL31.GL_MAX_UNIFORM_BLOCK_SIZE) >= EffectProfileShader.PAGE_BYTES
                        && resources.getResource(coefficients).isPresent();
                for (String suffix : new String[]{"", "_additive"}) {
                    String name = program + suffix;
                    Blending blending = suffix.isEmpty() ? Blending.DEFAULT : Blending.ADDITIVE_ALPHA;
                    var shader = usesProfiles ? new EffectProfileShader(resources, AstralSorcery.key(program + "_buffered" + suffix), coefficients, blending)
                            : new EffectMaterialShader(resources, AstralSorcery.key(name), blending);
                    event.registerShader(shader, loaded -> SHADERS.put(name, loaded));
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not register procedural effect shaders", exception);
        }
    }

    public static void bind(ProceduralEffect effect, Blending blending) {
        ShaderInstance program = SHADERS.get(blending == Blending.ADDITIVE_ALPHA ? effect.program + "_additive" : effect.program);
        program.getUniform("Effect").set((float) effect.columns, (float) effect.rows, (float) effect.family, (float) effect.variant);
        if (program.getUniform("Palette") != null) {
            program.getUniform("Palette").set((effect.rgb >> 16 & 255) / 255f, (effect.rgb >> 8 & 255) / 255f, (effect.rgb & 255) / 255f);
        }
        RenderSystem.setShader(() -> program);
    }
}
