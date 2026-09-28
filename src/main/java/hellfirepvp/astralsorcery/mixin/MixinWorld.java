/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinWorld
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 renames (verified with javap against net.minecraft.world.level.Level):
 *   World#calculateInitialSkylight() -&gt; Level#updateSkyBrightness()
 *   World#skylightSubtracted          -&gt; Level#skyDarken (private int)
 *   World#getDimensionKey()           -&gt; Level#dimension()
 *   ResourceLocation#getLocation()    -&gt; ResourceKey#location()
 */
@Mixin(Level.class)
public class MixinWorld {

    @Shadow
    private int skyDarken;

    @Inject(method = "updateSkyBrightness", at = @At("RETURN"), cancellable = true)
    public void solarEclipseSunBrightnessServer(CallbackInfo ci) {
        Level world = (Level) (Object) this;

        WorldContext ctx = SkyHandler.getContext(world);
        //Unused in 1.16.5 as well - kept verbatim so the ported body stays a 1:1 match.
        String strDimKey = world.dimension().location().toString();
        if (ctx != null &&
                ctx.getCelestialEventHandler().getSolarEclipse().isActiveNow()) {
            this.skyDarken = 11 - Math.round(ctx.getCelestialEventHandler().getSolarEclipsePercent() * 11F);
        }
    }

}
