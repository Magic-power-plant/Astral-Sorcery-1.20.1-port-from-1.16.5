/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin.client;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyEntityReach;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinGameRenderer
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 rename: GameRenderer#getMouseOver -&gt; GameRenderer#pick.
 * The targeted constant is still the first int 1 pushed by the method - verified with javap
 * against forge-1.20.1-47.4.20: pick(float) pushes iconst_3 for the "3 block" entity range
 * first, then iconst_1 at the "flag = true" store that arms the block-hit distance check
 * (the only int 1 constant before it is none; the fconst_1 further down is a float and is
 * therefore not a candidate for @Constant(intValue = 1)). require = 1 fails the load loudly
 * if that ever stops holding.
 */
@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @ModifyConstant(method = "pick", constant = @Constant(intValue = 1, ordinal = 0), require = 1)
    public int adjustDistanceCheck(int flagDoDistanceCheck) {
        PlayerProgress prog = ResearchHelper.getProgress(Minecraft.getInstance().player, LogicalSide.CLIENT);
        if (prog.isValid() && prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyEntityReach)) {
            return 0;
        }
        return flagDoDistanceCheck;
    }
}
