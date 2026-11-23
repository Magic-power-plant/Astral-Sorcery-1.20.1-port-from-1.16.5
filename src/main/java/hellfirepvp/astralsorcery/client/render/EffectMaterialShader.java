package hellfirepvp.astralsorcery.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

class EffectMaterialShader extends ShaderInstance {
    private final Blending blending;

    EffectMaterialShader(ResourceProvider resources, ResourceLocation program, Blending blending) throws IOException {
        super(resources, program, DefaultVertexFormat.POSITION_COLOR_TEX);
        this.blending = blending;
    }

    @Override
    public void apply() {
        super.apply();
        RenderSystem.enableBlend();
        blending.apply();
    }
}
