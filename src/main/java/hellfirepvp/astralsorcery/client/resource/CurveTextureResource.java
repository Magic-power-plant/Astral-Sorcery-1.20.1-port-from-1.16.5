package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.render.EffectCurveShader;
import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class CurveTextureResource extends BindableResource implements SpriteMaterial {

    private boolean usesCurve;

    CurveTextureResource(String path) {
        super(path);
    }

    @Override
    protected ResourceLocation resolveTextureLocation() {
        ResourceLocation sheet = super.resolveTextureLocation();
        ResourceLocation curve = sheet.withPath(path -> "textures/effect_curves/" + path.substring("textures/effect/".length()));
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        usesCurve = manager.getResource(sheet).isEmpty() && manager.getResource(curve).isPresent();
        return usesCurve ? curve : sheet;
    }

    @Override
    public void applySpriteShader(Blending blending) {
        if (usesCurve) {
            EffectCurveShader.bind(blending);
        }
    }

}
