/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect.mob;

import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import hellfirepvp.astralsorcery.common.entity.util.EntityUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectDropModifier
 * Created by HellFirePvP
 * Date: 26.08.2019 / 20:08
 */
public class EffectDropModifier extends EffectCustomTexture {

    public EffectDropModifier() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_DROP_MODIFIER);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.HIGH, this::onDrops);
    }

    private void onDrops(LivingDropsEvent event) {
        // 1.16.5 LivingDropsEvent#getEntityLiving -> 1.20.1 LivingDropsEvent#getEntity
        LivingEntity le = event.getEntity();
        // 1.16.5 MobEntity -> 1.20.1 Mob, GameRules.DO_MOB_LOOT -> GameRules.RULE_DOMOBLOOT
        if (le.level().isClientSide() ||
                !(le instanceof Mob) ||
                !(le.level() instanceof ServerLevel) ||
                !le.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return;
        }

        if (le.hasEffect(EffectsAS.EFFECT_DROP_MODIFIER.get())) {
            DamageSource src = event.getSource();

            //1.16.5 removeActivePotionEffect returned the removed EffectInstance; 1.20.1 removeEffect only
            //returns a boolean, so the amplifier is read before the effect is dropped.
            int amplifier = Objects.requireNonNull(le.getEffect(EffectsAS.EFFECT_DROP_MODIFIER.get())).getAmplifier();
            le.removeEffect(EffectsAS.EFFECT_DROP_MODIFIER.get());
            if (amplifier == 0) {
                event.getDrops().clear(); //Special case to void all items
            } else {
                for (int i = 0; i < amplifier; i++) {
                    // 1.16.5 LivingEntity#getAttackingEntity -> 1.20.1 LivingEntity#getKillCredit
                    List<ItemStack> loot = EntityUtils.generateLoot(le, rand, src, event.isRecentlyHit() ? le.getKillCredit() : null);
                    for (ItemStack stack : loot) {
                        if (stack.isEmpty()) {
                            continue;
                        }
                        //1.16.5 entityDropItem is @Nonnull in practice; 1.20.1 spawnAtLocation is @Nullable
                        //(it returns null for empty stacks or when the drop is suppressed), so the result is
                        //null-checked before it is added to the event's drop list.
                        // 1.16.5 Entity#entityDropItem -> 1.20.1 Entity#spawnAtLocation
                        ItemEntity itemEntity = le.spawnAtLocation(stack);
                        if (itemEntity != null) {
                            event.getDrops().add(itemEntity);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getIconName() {
        return "drop_modifier";
    }
}
