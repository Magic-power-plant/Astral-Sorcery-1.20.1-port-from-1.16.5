/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectOctans;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinLivingEntity
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5 -&gt; 1.20.1 renames: EquipmentSlotType -&gt; EquipmentSlot,
 * LivingEntity#getItemStackFromSlot -&gt; LivingEntity#getItemBySlot.
 * LivingEntity#getWaterSlowDown kept its name.
 */
@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    @Inject(method = "getWaterSlowDown", at = @At("HEAD"), cancellable = true)
    public void preventWaterSlowdown(CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
            if (MantleEffectOctans.shouldPreventWaterSlowdown(entity.getItemBySlot(EquipmentSlot.CHEST), entity)) {
                cir.setReturnValue(0.92F);
            }
        }
    }
}
