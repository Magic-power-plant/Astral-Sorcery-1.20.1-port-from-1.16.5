/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.client.registry.RegistryEffectTemplates;
import hellfirepvp.astralsorcery.client.registry.RegistryEffectTypes;
import hellfirepvp.astralsorcery.client.registry.RegistryRenderTypes;
import hellfirepvp.astralsorcery.client.registry.RegistrySprites;
import hellfirepvp.astralsorcery.client.registry.RegistryTextures;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalPerkTree;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AssetPreLoader
 * Created by HellFirePvP
 * Date: 11.07.2019 / 20:29
 *
 * 1.16.5 implemented Forge's ISelectiveResourceReloadListener and only ran on VanillaResourceType.TEXTURES.
 * Forge removed the selective resource reload API in 1.20.1, so the one-shot {@code initialized} flag is the
 * only gate left; behaviourally identical because the 1.16.5 body also only ever ran once.
 */
public class AssetPreLoader implements ResourceManagerReloadListener {

    public static final AssetPreLoader INSTANCE = new AssetPreLoader();

    private boolean initialized = false;

    private AssetPreLoader() {}

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        if (initialized) {
            return;
        }

        // AS-RES-05 (resolved): the six client asset registries of 1.16.5
        // AssetPreLoader#onResourceManagerReload, in the same order. The BLOCKED note that used to sit here said
        // all five client/registry/Registry* classes were "5-line stubs" with no members - that was true when it
        // was written and is long stale: RegistryTextures (154 lines), RegistrySprites (67),
        // RegistryRenderTypes (485), RegistryEffectTemplates (118), RegistryEffectTypes (32) and
        // ScreenJournalPerkTree#refreshDrawBuffer all exist and were simply never called.
        //
        // The consequence was not cosmetic. Every TexturesAS / SpritesAS / RenderTypesAS / EffectTemplatesAS
        // field stayed null, so opening the journal crashed the client outright:
        //   NullPointerException: ... because "TexturesAS.TEX_GUI_BACKGROUND_DEFAULT" is null
        //     at ScreenJournalProgressionRenderer.drawBackground(ScreenJournalProgressionRenderer.java:393)
        // observed on a real client launch before this was restored.
        //
        // AssetPreLoader.INSTANCE itself was already registered, from ClientProxy#registerReloadListeners on
        // RegisterClientReloadListenersEvent - only the body was empty.
        RegistryTextures.loadTextures();
        RegistrySprites.loadSprites();
        RegistryRenderTypes.init();
        RegistryEffectTemplates.init();
        RegistryEffectTypes.init();
        ScreenJournalPerkTree.refreshDrawBuffer();

        initialized = true;
    }

}
