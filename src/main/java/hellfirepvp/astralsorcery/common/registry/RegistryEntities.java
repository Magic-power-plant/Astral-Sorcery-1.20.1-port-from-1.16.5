/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityEmpty;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityGrapplingHook;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntityItemHighlighted;
import hellfirepvp.astralsorcery.client.render.entity.RenderEntitySpectralTool;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static hellfirepvp.astralsorcery.common.lib.EntityTypesAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryEntities
 * Created by HellFirePvP
 * Date: 17.08.2019 / 08:47
 */
// Port notes for 1.16.5 common/registry/RegistryEntities.java (165 lines):
//
//   init()                 (1.16.5 ref:49-136)  -> common/lib/EntityTypesAS (EntityTypesAS.ENTITIES,
//                                                  a DeferredRegister<EntityType<?>>). Re-building the twelve
//                                                  EntityType.Builder chains here would register every entity
//                                                  type a second time, so they are not repeated - the builder
//                                                  settings EntityTypesAS currently drops are reported as an
//                                                  external change on common/lib/EntityTypesAS, see
//                                                  TODO_PORT[AS-REG-10] below.
//   register(String, Builder) (ref:159-164)     -> DeferredRegister#register. EntityType.Builder#build(String)
//                                                  still exists, but EntityType#setRegistryName and
//                                                  AstralSorcery.getProxy().getRegistryPrimer().register(type)
//                                                  are both gone (IForgeRegistryEntry was deleted in 1.19).
//   initAttributes(EntityAttributeCreationEvent) (ref:138-141) -> ported below. Forge kept the event; only
//                                                  AttributeModifierMap.MutableAttribute#create() was renamed to
//                                                  AttributeSupplier.Builder#build(). 1.16.5 subscribed to it
//                                                  from PrimerEventHandler; here the class subscribes itself to
//                                                  the mod bus so no wiring outside common/registry is needed.
//   initClient()           (ref:143-157)        -> ported below against EntityRenderersEvent.RegisterRenderers.
//                                                  net.minecraftforge.fml.client.registry.RenderingRegistry and
//                                                  IRenderFactory were removed in 1.17; the renderer providers are
//                                                  now EntityRendererProvider<T> (constructor reference taking an
//                                                  EntityRendererProvider.Context), and the event is fired on the
//                                                  mod bus - ClientProxy already adds initClient as a listener.
//
//TODO_PORT[AS-REG-10]: the per-entity EntityType.Builder settings of 1.16.5 init() (ref:49-136) are not applied by
// the 1.20.1 common/lib/EntityTypesAS, which only calls .sized(..). Dropped for every one of the twelve types:
// noSummon() (1.16.5 disableSummoning, on all but FLARE), fireImmune() (1.16.5 immuneToFire, on NOCTURNAL_SPARK,
// ILLUMINATION_SPARK, FLARE, SPECTRAL_TOOL, OBSERVATORY_HELPER, GRAPPLING_HOOK), setUpdateInterval(1),
// setShouldReceiveVelocityUpdates(true), setTrackingRange(16/32/64) and setCustomClientFactory(..) - without the
// custom client factory the spark/flare/tool/hook entities are spawned client-side through the vanilla
// EntityType.EntityFactory instead of their (spawnEntity, world) constructors. MobCategory and the hitbox sizes
// also differ from the reference (1.16.5 used MobCategory.MISC for all twelve; sizes 0.1/0.4/0.6x0.8/0.5/0,0),
// and two registry names were changed (1.16.5 "item_highlighted" -> "item_highlight", 1.16.5 "item_starmetal" ->
// "item_starmetal_ingot"). All of that lives in common/lib/EntityTypesAS and is reported as an external change.
//
//VERIFY_PORT[AS-REG-11]: EntityTypesAS registers SHOOTING_STAR and ITEM_DAZZLING_GEM as entity types; 1.16.5 only
// declared those two fields and never assigned them (RegistryEntities#init never built them), so there is no
// reference renderer to port for either. They are consequently left without an entity renderer here, which means
// EntityRenderDispatcher falls back to its "no renderer" path if one is ever spawned.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryEntities {

    private RegistryEntities() {}

    @SubscribeEvent
    public static void initAttributes(EntityAttributeCreationEvent event) {
        // 1.16.5: AttributeModifierMap.MutableAttribute#create() -> 1.20.1: AttributeSupplier.Builder#build()
        event.put(FLARE.get(), EntityFlare.createAttributes().build());
        event.put(SPECTRAL_TOOL.get(), EntitySpectralTool.createAttributes().build());
    }

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
