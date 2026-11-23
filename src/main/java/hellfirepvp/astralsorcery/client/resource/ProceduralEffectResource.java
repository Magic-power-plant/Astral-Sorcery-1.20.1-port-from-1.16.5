package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.render.ProceduralEffectShader;
import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;

final class ProceduralEffectResource extends AbstractRenderableTexture.Full implements SpriteMaterial {
    private final ProceduralEffect effect;

    ProceduralEffectResource(ResourceLocation key, ProceduralEffect effect) {
        super(key);
        this.effect = effect;
    }

    @Override
    public void bind(Blending blending) {
        ProceduralEffectShader.bind(effect, blending);
    }

    @Override
    public void bindTexture() { bind(Blending.DEFAULT); }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        throw new UnsupportedOperationException("Procedural effects have no texture");
    }
}
