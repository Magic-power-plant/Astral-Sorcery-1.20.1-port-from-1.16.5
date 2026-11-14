/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.util.damage.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;



import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyAreaOfEffect
 * Created by HellFirePvP
 * Date: 25.08.2019 / 19:10
 */
public class KeyAreaOfEffect extends KeyAddEnchantment {

    public KeyAreaOfEffect(ResourceLocation name, float x, float y) {
        super(name, x, y);
        this.addEnchantment(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE, 2);
    }

    @Override
    public void attachListeners(LogicalSide side, IEventBus bus) {
        super.attachListeners(side, bus);
        bus.addListener(EventPriority.HIGH, this::onDamage);
    }

    //Restored from 1.16.5 (common/perk/node/key/KeyAreaOfEffect.java:54-95). The port left this method with an
    //unconditional `if (true) { return; }` at the top, i.e. the perk did nothing at all, and the reason is
    //visible in what else went missing: the EventFlags.SWEEP_ATTACK guard. Every target we damage below re-fires
    //LivingHurtEvent, which re-enters this same listener, so without both halves of that flag - the isSet() early
    //return AND the executeWithFlag wrapper - the first sweep recurses until the stack overflows. Both are back,
    //so the handler is safe to enable again.
    private void onDamage(LivingHurtEvent event) {
        if (EventFlags.SWEEP_ATTACK.isSet()) {
            return;
        }

        DamageSource source = event.getSource();
        //1.16.5 required `source instanceof IndirectEntityDamageSource`, a class deleted in 1.19.4 when damage
        //types became data-driven. "Indirect" now means the entity that dealt the damage is not the entity that
        //caused it - i.e. a projectile - which is exactly getDirectEntity() != getEntity().
        if (source.getDirectEntity() != null && source.getDirectEntity() != source.getEntity()
                && source.getEntity() instanceof Player) {
            Player player = (Player) source.getEntity();
            LogicalSide side = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, side);
            if (prog.getPerkData().hasPerkEffect(this)) {
                LivingEntity attacked = event.getEntity();

                float sweepingPercentage;
                Entity indirectSource = source.getDirectEntity();
                //1.16.5 read the thrown trident's own stack so a trident's Sweeping Edge counted; the port
                //stubbed this to `if (false)` with an always-empty ItemStack. ThrownTrident#tridentItem is
                //already exposed by META-INF/accesstransformer.cfg (f_37555_), so the real stack is reachable.
                if (indirectSource instanceof ThrownTrident) {
                    ItemStack tridentStack = ((ThrownTrident) indirectSource).tridentItem;
                    int sweepLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, tridentStack);
                    sweepingPercentage = sweepLevel > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(sweepLevel) : 0;
                } else {
                    //Was hardcoded to 0.5F, ignoring the player's actual Sweeping Edge level.
                    sweepingPercentage = EnchantmentHelper.getSweepingDamageRatio(player);
                }
                if (sweepingPercentage > 0) {
                    sweepingPercentage = PerkAttributeHelper.getOrCreateMap(player, side)
                            .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, sweepingPercentage);
                    float toApply = event.getAmount() * sweepingPercentage;

                    float range = 2.5F * PerkAttributeHelper.getOrCreateMap(player, side).getModifier(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);
                    EventFlags.SWEEP_ATTACK.executeWithFlag(() -> {
                        for (LivingEntity target : attacked.level().getEntitiesOfClass(LivingEntity.class,
                                attacked.getBoundingBox().inflate(range, range / 2F, range))) {
                            //Was `if (true)`, which let the sweep hit entities the player is not allowed to
                            //attack - team mates, peaceful-protected mobs, entities in a protected region.
                            if (MiscUtils.canPlayerAttackServer(player, target) && !player.equals(target)) {
                                DamageUtil.attackEntityFrom(target, source, toApply);
                            }
                        }
                    });
                }
            }
        }
    }
}

