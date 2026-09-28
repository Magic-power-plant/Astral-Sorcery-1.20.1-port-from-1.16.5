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
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectBleed
 * Created by HellFirePvP
 * Date: 26.08.2019 / 19:53
 */
public class EffectBleed extends EffectCustomTexture {

    public EffectBleed() {
        super(MobEffectCategory.HARMFUL, ColorsAS.EFFECT_BLEED);
    }

    // 1.16.5 Effect#isReady -> 1.20.1 MobEffect#isDurationEffectTick
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    // 1.16.5 Effect#performEffect -> 1.20.1 MobEffect#applyEffectTick
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        //1.16.5 read the server off LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER); that constant is
        //gone in 1.20.1, so the running server comes from Forge's server lifecycle hooks instead.
        if (entity instanceof Player &&
                !entity.level().isClientSide() &&
                entity.level() instanceof ServerLevel &&
                !ServerLifecycleHooks.getCurrentServer().isPvpAllowed()) {
            return;
        }
        DamageUtil.shotgunAttack(entity, e -> DamageUtil.attackEntityFrom(e, CommonProxy.DAMAGE_SOURCE_BLEED, 0.5F * (amplifier + 1)));
    }

    @Override
    public SpriteQuery getSpriteQuery() {
        return new SpriteQuery(AssetLoader.TextureLocation.GUI, 1, 1, "effect", "bleed");
    }
}
