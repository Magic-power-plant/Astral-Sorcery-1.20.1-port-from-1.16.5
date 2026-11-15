/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import hellfirepvp.astralsorcery.client.render.entity.RenderEntityEmpty;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityGrapplingHook;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityItemHighlighted;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntitySpectralTool;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

import static hellfirepvp.astralsorcery.common.lib.EntityTypesAS.*;

/** Renderer bindings for the entity types declared in EntityTypesAS. */
@OnlyIn(Dist.CLIENT)
public final class RegistryEntityRenderers {
    private RegistryEntityRenderers() {}

    @OnlyIn(Dist.CLIENT)
    public static void initClient(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NOCTURNAL_SPARK.get(), RenderEntityEmpty::new);
        event.registerEntityRenderer(ILLUMINATION_SPARK.get(), RenderEntityEmpty::new);
        event.registerEntityRenderer(FLARE.get(), RenderEntityEmpty::new);
        event.registerEntityRenderer(SPECTRAL_TOOL.get(), RenderEntitySpectralTool::new);

        event.registerEntityRenderer(ITEM_HIGHLIGHT.get(), RenderEntityItemHighlighted::new);
        event.registerEntityRenderer(ITEM_EXPLOSION_RESISTANT.get(), RenderEntityItemHighlighted::new);
        event.registerEntityRenderer(ITEM_CRYSTAL.get(), RenderEntityItemHighlighted::new);
        // 1.16.5: manager -> new ItemRenderer(manager, Minecraft.getInstance().getItemRenderer()) - the 1.16.5
        // net.minecraft.client.renderer.entity.ItemRenderer (the ItemEntity renderer) is 1.20.1's
        // net.minecraft.client.renderer.entity.ItemEntityRenderer, which pulls the item renderer off the context
        // itself. The starmetal ingot deliberately uses the plain vanilla item entity renderer, not the
        // highlighted one.
        event.registerEntityRenderer(ITEM_STARMETAL_INGOT.get(), ItemEntityRenderer::new);

        event.registerEntityRenderer(OBSERVATORY_HELPER.get(), RenderEntityEmpty::new);
        event.registerEntityRenderer(GRAPPLING_HOOK.get(), RenderEntityGrapplingHook::new);

        //Neither type exists in 1.16.5, so there is no 1.16.5 line to port - but both are registered in this tree
        //and EntityRenderDispatcher#shouldRender dereferences getRenderer(..) with no null check, so an unrendered
        //entity type crashes the client the moment one exists (e.g. /summon astralsorcery:shooting_star).
        //SHOOTING_STAR follows 1.21.1 client/lib/RenderersAS.java:75 (RenderEntityEmpty). ITEM_DAZZLING_GEM
        //extends EntityItemExplosionResistant, which is already rendered by RenderEntityItemHighlighted above.
        event.registerEntityRenderer(SHOOTING_STAR.get(), RenderEntityEmpty::new);
        event.registerEntityRenderer(ITEM_DAZZLING_GEM.get(), RenderEntityItemHighlighted::new);
    }
}
