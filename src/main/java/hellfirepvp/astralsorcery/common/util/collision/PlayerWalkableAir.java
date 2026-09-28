package hellfirepvp.astralsorcery.common.util.collision;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectAevitas;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.LogicalSide;

import java.util.List;

public class PlayerWalkableAir implements CustomCollisionHandler {

    private static final AABB FULL_BOX = new AABB(BlockPos.ZERO);

    @Override
    public boolean shouldAddCollisionFor(Entity entity) {
        if (!(entity instanceof Player player) || player.getAbilities().flying) {
            return false;
        }
        return ItemMantle.getEffect((LivingEntity) entity, ConstellationsAS.aevitas) != null &&
                canSupportEffect(player);
    }

    @Override
    public void addCollision(Entity entity, AABB testBox, List<AABB> additionalCollision) {
        int yOffset = 1;
        if (entity.getPose() == Pose.CROUCHING && isStandingOnAir(entity)) {
            yOffset = 2;
        }
        additionalCollision.add(FULL_BOX.move(entity.getX(), Math.floor(entity.getY()) - yOffset, entity.getZ()));
    }

    //1.16.5: MantleEffectAevitas#canSupportEffect(PlayerEntity) - moved here together with the
    //handler itself, which 1.16.5 nested inside MantleEffectAevitas.
    public static boolean canSupportEffect(Player player) {
        LogicalSide side = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
        PlayerProgress progress = ResearchHelper.getProgress(player, side);
        return progress.doPerkAbilities() &&
                progress.hasConstellationDiscovered(ConstellationsAS.aevitas) &&
                AlignmentChargeHandler.INSTANCE.hasCharge(player, side,
                        MantleEffectAevitas.CONFIG.chargeCostPerTravelTick.get().floatValue());
    }

    public static boolean isStandingOnAir(Entity entity) {
        if (entity.onGround()) {
            BlockPos at = entity.blockPosition().below();
            return entity.level().getBlockState(at).isAir();
        }
        return false;
    }
}
