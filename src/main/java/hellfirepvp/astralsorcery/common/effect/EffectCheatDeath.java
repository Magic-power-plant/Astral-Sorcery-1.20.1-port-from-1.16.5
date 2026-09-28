/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectCheatDeath
 * Created by HellFirePvP
 * Date: 26.08.2019 / 20:06
 */
public class EffectCheatDeath extends EffectCustomTexture {

    public EffectCheatDeath() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_CHEAT_DEATH);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.LOW, this::onDeath);
    }

    private void onDeath(LivingDeathEvent event) {
        // 1.16.5 LivingDeathEvent#getEntityLiving -> 1.20.1 LivingDeathEvent#getEntity
        LivingEntity le = event.getEntity();
        // 1.16.5 LivingEntity#isPotionActive -> 1.20.1 LivingEntity#hasEffect
        if (!le.level().isClientSide() && le.hasEffect(EffectsAS.EFFECT_CHEAT_DEATH.get())) {
            event.setCanceled(true);

            //1.16.5 removeActivePotionEffect returned the removed EffectInstance; 1.20.1 removeEffect only
            //returns a boolean, so the amplifier is read before the effect is dropped.
            int level = Objects.requireNonNull(le.getEffect(EffectsAS.EFFECT_CHEAT_DEATH.get())).getAmplifier();
            le.removeEffect(EffectsAS.EFFECT_CHEAT_DEATH.get());
            le.setHealth(Math.min(le.getMaxHealth(), 4 + level * 2));
            // 1.16.5 addPotionEffect(EffectInstance) -> 1.20.1 addEffect(MobEffectInstance)
            le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, true));
            le.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 500, 1, false, false, true));
            // 1.16.5 getEntitiesWithinAABB/grow -> 1.20.1 getEntitiesOfClass/inflate
            List<LivingEntity> others = le.level().getEntitiesOfClass(LivingEntity.class,
                    le.getBoundingBox().inflate(3), (e) -> e.isAlive() && e != le);
            for (LivingEntity lb : others) {
                // 1.16.5 Entity#setFire -> 1.20.1 Entity#setSecondsOnFire
                lb.setSecondsOnFire(10);
                // 1.16.5 LivingEntity#applyKnockback -> 1.20.1 LivingEntity#knockback
                lb.knockback(2F, lb.getX() - le.getX(), lb.getZ() - le.getZ());
            }
            //TODO particles
            //PktParticleEvent ev = new PktParticleEvent(PktParticleEvent.ParticleEventType.PHOENIX_PROC, new Vector3(le.getX(), le.getY(), le.getZ()));
            //PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(le.level(), le.blockPosition(), 32));
        }
    }

    @Override
    public SpriteQuery getSpriteQuery() {
        return new SpriteQuery(AssetLoader.TextureLocation.GUI, 1, 1, "effect", "cheat_death");
    }
}
