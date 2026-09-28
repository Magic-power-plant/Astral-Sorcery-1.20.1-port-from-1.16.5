/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.event.CooldownSetEvent;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinCooldownTracker
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5: CooldownTracker#setCooldown(Item, int) / ServerCooldownTracker#player
 * 1.20.1: ItemCooldowns#addCooldown(Item, int) / ServerItemCooldowns#player (opened up by
 *         META-INF/accesstransformer.cfg -> public net.minecraft.world.item.ServerItemCooldowns f_43065_).
 */
@Mixin(ItemCooldowns.class)
public class MixinCooldownTracker {

    @ModifyVariable(method = "addCooldown", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public int fireCooldownEvent(int cooldownTicks) {
        ItemCooldowns tracker = (ItemCooldowns) (Object) this;
        if (tracker instanceof ServerItemCooldowns) {
            CooldownSetEvent event = new CooldownSetEvent(((ServerItemCooldowns) tracker).player, cooldownTicks);
            MinecraftForge.EVENT_BUS.post(event);
            cooldownTicks = Math.max(event.getResultCooldown(), 1);
        }
        return cooldownTicks;
    }

}
