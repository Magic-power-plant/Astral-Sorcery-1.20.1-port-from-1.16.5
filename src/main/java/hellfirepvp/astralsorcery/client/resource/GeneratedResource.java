/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import hellfirepvp.astralsorcery.common.util.data.NameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: GeneratedResource
 * Created by HellFirePvP
 * Date: 06.01.2021 / 11:00
 */
public class GeneratedResource extends BindableResource implements ReloadableResource {

    private final Supplier<BufferedImage> imageGen;
    private final boolean blur, clamp;

    GeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur, boolean clamp) {
        super(NameUtil.prefixPath(key, "dynamic_"));
        this.imageGen = imageGenerator;
        this.blur = blur;
        this.clamp = clamp;
    }

    @Override
    protected AbstractTexture allocateGlId() {
        if (AssetLibrary.isReloading()) {
            return null;
        }
        TextureManager mgr = Minecraft.getInstance().getTextureManager();
        AbstractTexture resource = mgr.getTexture(this.getKey(), null);
        if (resource != null) {
            return resource;
        }
        InMemoryTexture texture = new InMemoryTexture(this.imageGen, this.blur, this.clamp);
        //1.16.5: TextureManager.loadTexture(ResourceLocation, Texture) -> 1.20.1: TextureManager.register(...)
        mgr.register(this.getKey(), texture);
        return mgr.getTexture(this.getKey(), null);
    }

    private static class InMemoryTexture extends AbstractTexture {

        private final Supplier<BufferedImage> imageGen;
        private final boolean blur, clamp;

        private InMemoryTexture(Supplier<BufferedImage> imageGen, boolean blur, boolean clamp) {
            this.imageGen = imageGen;
            this.blur = blur;
            this.clamp = clamp;
        }

        //1.16.5: Texture.loadTexture(IResourceManager) -> 1.20.1: AbstractTexture.load(ResourceManager)
        @Override
        public void load(ResourceManager manager) throws IOException {
            //1.16.5: NativeImage.PixelFormat.RGBA -> 1.20.1: NativeImage.Format.RGBA
            NativeImage image = NativeImage.read(NativeImage.Format.RGBA, createMemInput());
            if (!RenderSystem.isOnRenderThreadOrInit()) {
                RenderSystem.recordRenderCall(() -> this.loadImage(image, this.blur, this.clamp));
            } else {
                this.loadImage(image, this.blur, this.clamp);
            }
        }

        private void loadImage(NativeImage imageIn, boolean blurIn, boolean clampIn) {
            //1.16.5: Texture.getGlTextureId() -> 1.20.1: AbstractTexture.getId()
            TextureUtil.prepareImage(this.getId(), 0, imageIn.getWidth(), imageIn.getHeight());
            //1.16.5: NativeImage.uploadTextureSub(...) -> 1.20.1: NativeImage.upload(...) (identical parameters)
            imageIn.upload(0, 0, 0, 0, 0, imageIn.getWidth(), imageIn.getHeight(), blurIn, clampIn, false, true);
        }

        private InputStream createMemInput() throws IOException {
            BufferedImage bufferedImage = imageGen.get();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            baos.close();
            return new ByteArrayInputStream(baos.toByteArray());
        }
    }
}
