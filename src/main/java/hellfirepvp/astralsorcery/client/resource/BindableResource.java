/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BindableResource
 * Created by HellFirePvP
 * Date: 07.05.2016 / 00:50
 */
@OnlyIn(Dist.CLIENT)
public class BindableResource extends AbstractRenderableTexture.Full implements ReloadableResource {

    //1.16.5: net.minecraft.client.renderer.texture.Texture -> 1.20.1: AbstractTexture
    private AbstractTexture resource = null;
    private String path = null;

    protected BindableResource(ResourceLocation key) {
        super(key);
    }

    BindableResource(String path) {
        this(AstralSorcery.key(path.replaceAll("[^a-zA-Z0-9\\.\\-]", "_")));
        this.path = path;
        allocateGlId();
    }

    public String getPath() {
        return path;
    }

    public SpriteSheetResource asSpriteSheet(int rows, int columns) {
        return new SpriteSheetResource(this, rows, columns);
    }

    public void invalidateAndReload() {
        //1.16.5: TextureManager.deleteTexture(ResourceLocation) -> 1.20.1: TextureManager.release(ResourceLocation)
        Minecraft.getInstance().getTextureManager().release(this.getKey());
        this.resource = null;
    }

    protected AbstractTexture allocateGlId() {
        if (AssetLibrary.isReloading()) {
            return null;
        }
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        //1.20.1's single-argument TextureManager.getTexture() lazily registers a SimpleTexture for the queried
        //location instead of returning null like 1.16.5 did; the two-argument overload is the faithful probe.
        AbstractTexture resource = mgr.getTexture(this.getKey(), null);
        if (resource != null) {
            return resource;
        }
        //1.16.5: TextureManager.loadTexture(ResourceLocation, Texture) -> 1.20.1: TextureManager.register(...)
        mgr.register(this.getKey(), new SimpleTexture(new ResourceLocation(this.getPath())));
        return mgr.getTexture(this.getKey(), null);
    }

    @Override
    public void bindTexture() {
        if (AssetLibrary.isReloading()) {
            return; //we do nothing but wait.
        }
        if (this.resource == null) {
            this.resource = allocateGlId();
        }
        if (this.resource == null) {
            return;
        }
        //1.16.5: RenderSystem.bindTexture(glTextureId) fed the fixed-function texture unit. 1.20.1 core shaders
        //sample from the shader texture slots, so the loaded GL id has to be pushed into sampler 0 instead.
        //VERIFY_PORT[AS-RES-03]: confirm in-game that dynamically bound AS textures still appear on effects that
        //draw through RenderTypeDecorator.wrapSetup (BatchRenderContext/FXSpritePlane/FXFacingSprite).
        RenderSystem.setShaderTexture(0, this.resource.getId());
    }

    @Override
    public RenderStateShard.TextureStateShard asState() {
        return new RenderStateShard.TextureStateShard(this.getKey(), false, false) {
            @Override
            public void setupRenderState() {
                //1.16.5 additionally called RenderSystem.enableTexture(); the fixed-function texture toggle no
                //longer exists in 1.20.1 and binding into sampler 0 is sufficient.
                BindableResource.this.bindTexture();
                //Deliberate divergence from 1.16.5, which called resource.setBlurMipmap(false, false)
                //unconditionally and therefore NPEd whenever bindTexture() bailed out (AssetLibrary.isReloading()
                //or a failed allocateGlId() both leave `resource` null). Guarded here; every non-null path is
                //behaviourally identical to the reference.
                if (BindableResource.this.resource != null) {
                    BindableResource.this.resource.setBlurMipmap(false, false);
                }
            }
        };
    }
}
