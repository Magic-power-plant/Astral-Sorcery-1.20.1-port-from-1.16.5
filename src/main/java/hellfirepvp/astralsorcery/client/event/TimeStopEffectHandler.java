/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import hellfirepvp.astralsorcery.common.util.time.TimeStopController;

import hellfirepvp.astralsorcery.client.effect.source.TimeStopEffects;

import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.data.sync.client.ClientTimeFreezeEffects;
import hellfirepvp.astralsorcery.common.util.time.TimeStopEffectHelper;
import hellfirepvp.astralsorcery.common.ITickHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

import java.util.EnumSet;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TimeStopEffectClientHandler
 * Created by HellFirePvP
 * Date: 22.02.2020 / 20:56
 */
public class TimeStopEffectHandler implements ITickHandler {

    public static final TimeStopEffectHandler INSTANCE = new TimeStopEffectHandler();

    private TimeStopEffectHandler() {}

    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() || !TimeStopController.isFrozenDirectly(entity)
                || !entity.isAlive() || entity.getHealth() <= 0) return;
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon
                && dragon.getPhaseManager().getCurrentPhase().getPhase()
                == net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.DYING) return;
        int count = (int) net.minecraft.util.Mth.sqrt(entity.getBbWidth() * entity.getBbHeight());
        for (int i = 0; i < count; i++) {
            if (entity.level().random.nextInt(5) == 0) TimeStopEffects.playEntityParticles(entity);
        }
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        // 1.16.5: Minecraft#world -> 1.20.1: Minecraft#level
        if (Minecraft.getInstance().level == null) {
            return;
        }

        SyncDataHolder.executeClient(SyncDataHolder.DATA_TIME_FREEZE_EFFECTS, ClientTimeFreezeEffects.class, effects -> {
            List<TimeStopEffectHelper> zoneEffects = effects.getTimeStopEffects(Minecraft.getInstance().level);
            zoneEffects.forEach(TimeStopEffects::playClientTickEffect);
        });
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "TimeStop EffectHandler";
    }
}
