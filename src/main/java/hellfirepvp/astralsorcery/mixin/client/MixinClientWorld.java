/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin.client;

import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinClientWorld
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 renames (verified with javap against net.minecraft.client.multiplayer.ClientLevel):
 *   ClientWorld#getSunBrightness(float) -&gt; ClientLevel#getSkyDarken(float) (still returns float and
 *                                          is still the client-side "how bright is the sky" hook)
 *   World#getDimensionKey().getLocation() -&gt; Level#dimension().location()
 */
@Mixin(ClientLevel.class)
public class MixinClientWorld {

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    public void solarEclipseSunBrightness(float partialTicks, CallbackInfoReturnable<Float> cir) {
        Level world = (Level) (Object) this;

        WorldContext ctx = SkyHandler.getContext(world, LogicalSide.CLIENT);
        String strDimKey = world.dimension().location().toString();
        if (ctx != null &&
                RenderingConfig.CONFIG.dimensionsWithSkyRendering.get().contains(strDimKey) &&
                ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            float perc = ctx.getCelestialEventHandler().getSolarEclipsePercent();
            perc = 0.05F + (perc * 0.95F);

            cir.setReturnValue(cir.getReturnValueF() * perc);
        }
    }

}
