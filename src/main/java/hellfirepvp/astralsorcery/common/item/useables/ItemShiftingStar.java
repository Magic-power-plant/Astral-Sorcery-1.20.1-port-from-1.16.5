/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.useables;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.item.base.PerkExperienceRevealer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemShiftingStar
 * Created by HellFirePvP
 * Date: 22.02.2020 / 21:39
 */
// 1.16.5 built its own Properties (maxStackSize(1) + group(CommonProxy.ITEM_GROUP_AS)).
// 1.20.1 has no Properties#group, so the properties are supplied by ItemsAS and the creative tab is applied centrally.
public class ItemShiftingStar extends Item implements PerkExperienceRevealer {

    // 1.16.5 used the inherited static Item#random, which no longer exists in 1.20.1.
    private static final Random random = new Random();

    public ItemShiftingStar(Properties properties) {
        super(properties);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        IConstellation cst = this.getBaseConstellation();
        if (cst != null) {
            if (ResearchHelper.getClientProgress().hasConstellationDiscovered(cst)) {
                // 1.16.5 getConstellationName() returned IFormattableTextComponent; 1.20.1 returns an immutable Component, hence copy().
                tooltip.add(cst.getConstellationName().copy().withStyle(ChatFormatting.BLUE));
            } else {
                tooltip.add(Component.translatable("astralsorcery.misc.noinformation").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        playerIn.startUsingItem(handIn);
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        if (!worldIn.isClientSide() && entityLiving instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) entityLiving;
            IMajorConstellation cst = this.getBaseConstellation();
            if (cst != null) {
                PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
                if (!prog.isValid() || !prog.wasOnceAttuned() || !prog.hasConstellationDiscovered(cst)) {
                    return stack;
                }

                double perkExp = prog.getPerkData().getPerkExp();
                if (ResearchManager.setAttunedConstellation(player, cst)) {
                    //AS-ITM-11 (resolved): 1.16.5 ItemShiftingStar:94. setAttunedConstellation deliberately zeroes
                    //perk exp and persists that in the same call, so this restore is what makes re-attuning
                    //non-destructive. It must stay immediately after the re-attune and before the message.
                    //The old note claimed ResearchManager had no setExp(Player, long) and that
                    //PlayerPerkData#setExp was out of reach; the first is fixed, and the second was never true -
                    //ResearchManager is in PlayerPerkData's own package.
                    ResearchManager.setExp(player, Mth.lfloor(perkExp));
                    player.sendSystemMessage(Component.translatable("astralsorcery.progress.switch.attunement").withStyle(ChatFormatting.BLUE));
                    SoundHelper.playSoundAround(SoundEvents.GLASS_BREAK, worldIn, entityLiving.blockPosition(), 1F, 1F);
                    return ItemStack.EMPTY;
                }
            } else if (ResearchManager.setAttunedConstellation(player, null)) {
                player.sendSystemMessage(Component.translatable("astralsorcery.progress.remove.attunement").withStyle(ChatFormatting.BLUE));
                SoundHelper.playSoundAround(SoundEvents.GLASS_BREAK, worldIn, entityLiving.blockPosition(), 1F, 1F);
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    // 1.16.5 Item#onUsingTick(ItemStack, LivingEntity, int); 1.20.1 vanilla Item#onUseTick(Level, LivingEntity, ItemStack, int).
    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (player.level().isClientSide()) {
            this.playUseEffects(player, getUseDuration(stack) - count, getUseDuration(stack));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playUseEffects(LivingEntity player, int tick, int total) {
        IMajorConstellation cst = this.getBaseConstellation();
        if (cst == null) {
            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.atEntityCorner(player).addY(player.getBbHeight() / 2))
                    .setMotion(new Vector3(-0.1 + random.nextFloat() * 0.2, 0.01, -0.1 + random.nextFloat() * 0.2))
                    .setScaleMultiplier(0.2F + random.nextFloat());
            if (random.nextBoolean()) {
                p.color(VFXColorFunction.WHITE);
            }
        } else {
            float percCycle = (float) ((((float) (tick % total)) / ((float) total)) * 2 * Math.PI);
            int parts = 5;
            for (int i = 0; i < parts; i++) {
                float angleSwirl = 75F;
                Vector3 center = Vector3.atEntityCorner(player).addY(player.getBbHeight() / 2);
                Vector3 v = Vector3.RotAxis.X_AXIS.clone();
                float originalAngle = (((float) i) / ((float) parts)) * 360F;
                double angle = originalAngle + (Mth.sin(percCycle) * angleSwirl);
                v.rotate(-Math.toRadians(angle), Vector3.RotAxis.Y_AXIS).normalize().multiply(4);
                Vector3 pos = center.clone().add(v);
                Vector3 mot = center.clone().subtract(pos).normalize().multiply(0.1);

                FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .setScaleMultiplier(0.25F + random.nextFloat() * 0.4F)
                        .setMotion(mot)
                        .setMaxAge(50);
                if (random.nextInt(4) == 0) {
                    p.color(VFXColorFunction.WHITE);
                } else if (random.nextInt(3) == 0) {
                    p.color(VFXColorFunction.constant(cst.getConstellationColor().brighter()));
                } else {
                    p.color(VFXColorFunction.constant(cst.getConstellationColor()));
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return this.getBaseConstellation() == null ? 60 : 100;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Nullable
    public IMajorConstellation getBaseConstellation() {
        return null;
    }
}
