/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.collision.PlayerWalkableAir;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectAevitas
 * Created by HellFirePvP
 * Date: 11.03.2020 / 21:31
 */
//AS-PKG-01 (resolved): 1.16.5 nests the CustomCollisionHandler 'PlayerWalkableAir' in this class and declares
//the static helpers canSupportEffect(PlayerEntity)/isStandingOnAir(Entity) here (1.16.5
//MantleEffectAevitas.java:117-132,194-215). This tree has all three in common/util/collision/PlayerWalkableAir,
//which CollisionManager#init registers (called from CommonProxy#onCommonSetup) and which this class calls into
//at the two isStandingOnAir sites below - so the behaviour is wired, just not duplicated here.
public class MantleEffectAevitas extends MantleEffect {

    public static AevitasConfig CONFIG = new AevitasConfig();

    public MantleEffectAevitas() {
        super(ConstellationsAS.aevitas);
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    @Override
    protected void tickServer(Player player) {
        super.tickServer(player);

        if (PlayerWalkableAir.isStandingOnAir(player)) {
            AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerTravelTick.get().floatValue(), false);
        }

        int healChance = CONFIG.healChance.get();
        int foodChance = CONFIG.feedChance.get();
        if (healChance > 0 && rand.nextInt(healChance) == 0) {
            player.heal(CONFIG.healthPerCycle.get().floatValue());
        }
        if (foodChance > 0 && rand.nextInt(foodChance) == 0) {
            FoodData stats = player.getFoodData();
            if (stats.getFoodLevel() < 20 || stats.getSaturationLevel() < 5) {
                if (AlignmentChargeHandler.INSTANCE.hasCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFood.get())) {
                    // 1.16.5: FoodStats#addStats(int, float)
                    stats.eat(CONFIG.foodPerCycle.get().intValue(), 0.5F);
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCostPerFood.get(), false);
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickClient(Player player) {
        super.tickClient(player);

        this.playCapeSparkles(player, 0.1F);

        if (PlayerWalkableAir.isStandingOnAir(player)) {
            Vector3 center = Vector3.atEntityCorner(player).addY(0.15F);
            for (int i = 0; i < 5; i++) {
                Vector3 offset = Vector3.random().setY(0).normalize()
                        .multiply(rand.nextFloat() * 5).addY(rand.nextFloat() * -0.4F)
                        .add(center);
                FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(offset)
                        .setScaleMultiplier(0.25F + rand.nextFloat() * 0.15F)
                        .alpha(VFXAlphaFunction.PYRAMID)
                        .setMotion(Vector3.random().normalize().multiply(0.004F))
                        .setMaxAge(45 + rand.nextInt(20));
                if (rand.nextInt(3) == 0) {
                    p.color(VFXColorFunction.WHITE);
                } else {
                    p.color(VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_AEVITAS));
                }
            }
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    public static class AevitasConfig extends Config {

        private final int defaultHealChance = 80;
        private final int defaultFeedChance = 80;
        private final double defaultHealthPerCycle = 0.5F;
        private final double defaultFoodPerCycle = 1F;

        private final double defaultChargeCostPerTravelTick = 2.5F;
        private final int defaultChargeCostPerHeal = 100;
        private final int defaultChargeCostPerFood = 100;

        public ForgeConfigSpec.IntValue healChance;
        public ForgeConfigSpec.IntValue feedChance;
        public ForgeConfigSpec.DoubleValue healthPerCycle;
        public ForgeConfigSpec.DoubleValue foodPerCycle;

        public ForgeConfigSpec.DoubleValue chargeCostPerTravelTick;
        public ForgeConfigSpec.IntValue chargeCostPerHeal;
        public ForgeConfigSpec.IntValue chargeCostPerFood;

        public AevitasConfig() {
            super("aevitas");
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.healChance = cfgBuilder
                    .comment("Set the chance of '1 in <this value>' per tick to do 1 heal cycle. Amount healed per cycle is determined by 'healthPerCycle' config option. Set to 0 to disable.")
                    .translation(translationKey("healChance"))
                    .defineInRange("healChance", this.defaultHealChance, 0, Integer.MAX_VALUE);
            this.feedChance = cfgBuilder
                    .comment("Set the chance of '1 in <this value>' per tick to do 1 food cycle. Amount fed per cycle is determined by 'foodPerCycle' config option. Set to 0 to disable.")
                    .translation(translationKey("feedChance"))
                    .defineInRange("feedChance", this.defaultFeedChance, 0, Integer.MAX_VALUE);
            this.healthPerCycle = cfgBuilder
                    .comment("Set the amount of health recovered by health cycle.")
                    .translation(translationKey("healthPerCycle"))
                    .defineInRange("healthPerCycle", this.defaultHealthPerCycle, 0.0, 100.0);
            this.foodPerCycle = cfgBuilder
                    .comment("Set the amount of food recovered by food cycle.")
                    .translation(translationKey("foodPerCycle"))
                    .defineInRange("foodPerCycle", this.defaultFoodPerCycle, 0.0, 100.0);

            this.chargeCostPerTravelTick = cfgBuilder
                    .comment("Set the amount alignment charge consumed per tick when walking/standing in the air")
                    .translation(translationKey("chargeCostPerTravelTick"))
                    .defineInRange("chargeCostPerTravelTick", this.defaultChargeCostPerTravelTick, 0.0, 100.0);
            this.chargeCostPerFood = cfgBuilder
                    .comment("Set the amount alignment charge consumed per feed-cycle")
                    .translation(translationKey("chargeCostPerFood"))
                    .defineInRange("chargeCostPerFood", this.defaultChargeCostPerFood, 0, 1000);
            this.chargeCostPerHeal = cfgBuilder
                    .comment("Set the amount alignment charge consumed per heal-cycle")
                    .translation(translationKey("chargeCostPerHeal"))
                    .defineInRange("chargeCostPerHeal", this.defaultChargeCostPerHeal, 0, 1000);
        }
    }
}
