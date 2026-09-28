/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.sky.ChainingSkyRenderer;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SkyRenderEventHandler
 * Created by HellFirePvP
 * Date: 12.01.2020 / 22:16
 */
public class SkyRenderEventHandler {

    /**
     * AS-CLI-13 (resolved): 1.16.5 installed the astral sky lazily from RenderWorldLastEvent, by reading
     * {@code DimensionRenderInfo#getSkyRenderHandler()} and swapping in a {@code ChainingSkyRenderer} wrapping the
     * previous handler. Forge 1.20.1 deleted {@code ISkyRenderHandler} and
     * {@code DimensionSpecialEffects#get/setSkyRenderHandler} entirely, so the install is no longer a per-frame
     * mutation: it is a one-off registration on the mod bus through {@code RegisterDimensionSpecialEffectsEvent},
     * attached from {@link hellfirepvp.astralsorcery.client.ClientProxy}.
     * <p>
     * The BLOCKED note that stood here claimed {@code ChainingSkyRenderer} was "still a stub implementing
     * neither". That is stale - it is a complete {@code DimensionSpecialEffects} subclass overriding
     * {@code renderSky(..)} and taking the wrapped instance in its constructor; it simply had no construction site,
     * so the starfield, the in-sky constellations, the astral sun and moon and the eclipse sky never rendered.
     * <p>
     * Every entry of the existing effects map is wrapped, rather than only {@code minecraft:overworld}, so that a
     * dimension added by another mod is still chained and that mod's own effects instance keeps being delegated to.
     * {@code ChainingSkyRenderer#renderSky} then decides per frame whether to draw the astral sky, using
     * {@code skyType()} plus the two config whitelists.
     */
    public static void onRegisterSkyEffects(RegisterDimensionSpecialEffectsEvent event) {
        //Snapshot first: register(..) writes into this same map, so iterating it directly would be a concurrent
        //modification, and re-wrapping an already-wrapped entry would nest ChainingSkyRenderers.
        Map<ResourceLocation, DimensionSpecialEffects> existing =
                new HashMap<>(DimensionSpecialEffects.EFFECTS);
        for (Map.Entry<ResourceLocation, DimensionSpecialEffects> entry : existing.entrySet()) {
            if (entry.getValue() instanceof ChainingSkyRenderer) {
                continue;
            }
            event.register(entry.getKey(), new ChainingSkyRenderer(entry.getValue()));
        }
    }

    // 1.16.5's onRender(RenderWorldLastEvent) existed ONLY to install the sky handler lazily, per frame. In 1.20.1
    // that install is a one-off mod-bus registration (see onRegisterSkyEffects above), so the per-frame hook has
    // nothing left to do and is gone along with its ClientProxy registration, rather than being kept as an empty
    // listener. (1.16.5 client/event/SkyRenderEventHandler.java:31-42)

    // 1.16.5: EntityViewRenderEvent.FogColors -> ViewportEvent.ComputeFogColor (same red/green/blue accessors)
    public static void onFog(ViewportEvent.ComputeFogColor event) {
        ClientLevel world = Minecraft.getInstance().level;
        if (world != null) {
            String strDimKey = world.dimension().location().toString();
            //AS-CLI-13 (resolved): 1.16.5 required
            //`world.func_239132_a_().getSkyRenderHandler() instanceof ChainingSkyRenderer` here, i.e. it only
            //dimmed the eclipse fog once the chaining sky renderer was actually installed. The 1.20.1 equivalent
            //is simply `world.effects() instanceof ChainingSkyRenderer`, now that the install is a real
            //registration. Without it the fog was dimmed during an eclipse even over a vanilla sky.
            //(1.16.5 client/event/SkyRenderEventHandler.java:52)
            if (world.effects() instanceof ChainingSkyRenderer &&
                    world.effects().skyType() == DimensionSpecialEffects.SkyType.NORMAL &&
                    RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey) &&
                    !RenderingConfig.CONFIG.dimensionsWithOnlyConstellationRendering.get().contains(strDimKey)) {

                WorldContext ctx = SkyHandler.getContext(world, LogicalSide.CLIENT);

                if (ctx != null && ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
                    float perc = ctx.getCelestialEventHandler().getSolarEclipsePercent();
                    perc = 0.05F + (perc * 0.95F);

                    event.setRed(event.getRed() * perc);
                    event.setGreen(event.getGreen() * perc);
                    event.setBlue(event.getBlue() * perc);
                }
            }
        }
    }

}
