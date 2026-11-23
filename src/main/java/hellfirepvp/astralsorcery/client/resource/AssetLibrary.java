/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.sky.astral.AstralSkyRenderer;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AssetLibrary
 * Created by HellFirePvP
 * Date: 09.08.2016 / 11:00
 *
 * 1.16.5 implemented Forge's ISelectiveResourceReloadListener and filtered on VanillaResourceType.TEXTURES.
 * Forge removed the selective resource reload API in 1.20.1, so this listener reacts to every client resource
 * reload; the {@code reloading} re-entrancy guard is kept unchanged.
 */
public class AssetLibrary implements ResourceManagerReloadListener {

    public static AssetLibrary INSTANCE = new AssetLibrary();
    private static boolean reloading = false;

    private static final Map<AssetLoader.SubLocation, Map<String, AbstractRenderableTexture>> loadedTextures = new HashMap<>();
    private static final Map<ResourceLocation, BindableResource> namedTextures = new HashMap<>();
    private static final Map<ResourceLocation, GeneratedResource> dynamicTextures = new HashMap<>();
    private static final List<ReloadableResource> reloadableResources = new ArrayList<>();

    private AssetLibrary() {}

    public static Supplier<AbstractRenderableTexture> loadReference(AssetLoader.TextureLocation location, String... path) {
        return new CacheReference<>(() -> loadTexture(location, path));
    }

    public static AbstractRenderableTexture loadTexture(AssetLoader.TextureLocation location, String... path) {
        String name = String.join("/", path);
        if (name.endsWith(".png")) {
            throw new IllegalArgumentException("Tried to loadTexture with appended .png from the AssetLibrary!");
        }
        return loadedTextures.computeIfAbsent(location, key -> new HashMap<>())
                .computeIfAbsent(name, key -> loadTexture(AssetLoader.textureId(location, key)));
    }

    public static AbstractRenderableTexture loadCurveTexture(String... path) {
        return loadTexture(AssetLoader.TextureLocation.EFFECT, path);
    }

    /** Resolves a data-only texture identifier using the same reload lifecycle as named assets. */
    public static AbstractRenderableTexture loadTexture(ResourceLocation texture) {
        return namedTextures.computeIfAbsent(texture, key -> {
            BindableResource resource = key.getNamespace().equals(AstralSorcery.MODID) && key.getPath().startsWith("textures/effect/")
                    ? new CurveTextureResource(key.toString()) : new BindableResource(key.toString());
            reloadableResources.add(resource);
            return resource;
        });
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur) {
        return loadGeneratedResource(key, imageGenerator, blur, false);
    }

    public static GeneratedResource loadGeneratedResource(ResourceLocation key, Supplier<BufferedImage> imageGenerator, boolean blur, boolean clamp) {
        if (dynamicTextures.containsKey(key)) {
            return dynamicTextures.get(key);
        }
        GeneratedResource resource = new GeneratedResource(key, imageGenerator, blur, clamp);
        reloadableResources.add(resource);
        dynamicTextures.put(key, resource);
        return resource;
    }

    public static boolean isReloading() {
        return reloading;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        //1.16.5 guarded with `reloading || !resourcePredicate.test(VanillaResourceType.TEXTURES)`; the selective
        //predicate no longer exists on 1.20.1 (see class javadoc).
        if (reloading) {
            return;
        }
        reloading = true;
        AstralSorcery.LOG.info("[AssetLibrary] Refreshing and Invalidating Resources");
        reloadableResources.forEach(ReloadableResource::invalidateAndReload);
        reloading = false;

        //Reload buffer during next render
        //AS-RES-03 (resolved): the note claimed AstralSkyRenderer was "still a stub without an INSTANCE field or
        //reset() method"; INSTANCE is at AstralSkyRenderer:73 and reset() at :83. Without this the astral sky kept
        //rendering from vertex buffers built against the previous, now-invalidated resources after a resource
        //reload or a resource-pack change.
        AstralSkyRenderer.INSTANCE.reset();
        AstralSorcery.LOG.info("[AssetLibrary] Successfully reloaded library.");
    }

}
