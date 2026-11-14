package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.ItemStarmetalIngot;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class EntityStarmetal extends EntityCustomItemReplacement implements InteractableEntity {

    public EntityStarmetal(EntityType<? extends EntityStarmetal> type, Level world) {
        super(type, world);
    }

    public EntityStarmetal(EntityType<? extends EntityStarmetal> type, Level world, double x, double y, double z) {
        super(type, world, x, y, z);
        this.setYRot(this.level().random.nextFloat() * 360.0F);
        this.setDeltaMovement(this.level().random.nextDouble() * 0.2D - 0.1D, 0.2D, this.level().random.nextDouble() * 0.2D - 0.1D);
    }

    public EntityStarmetal(EntityType<? extends EntityStarmetal> type, Level world, double x, double y, double z, ItemStack stack) {
        super(type, world, x, y, z, stack);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    public boolean hitByEntity(Entity entity) {
        if (!this.level().isClientSide() && entity instanceof ServerPlayer) {
            ItemStack held = ((ServerPlayer) entity).getItemInHand(InteractionHand.MAIN_HAND);
            if (!held.isEmpty() && held.getItem() instanceof ItemChisel) {

                ItemStack thisStack = this.getItem();
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemStarmetalIngot) {

                    boolean doDamage = false;
                    if (this.level().random.nextFloat() < 0.4F) {
                        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, held);
                        doDamage = this.createStardust(fortuneLevel);
                    }
                    if (doDamage || this.level().random.nextFloat() < 0.35F) {
                        held.hurtAndBreak(1, (Player) entity, (player) -> player.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
        return true;
    }

    private boolean createStardust(int fortuneLevel) {
        ItemStack created = new ItemStack(ItemsAS.STARDUST.get());
        ItemUtils.dropItemNaturally(level(), this.getX(), this.getY() + 0.25F, this.getZ(), created);

        float breakIngot = 0.90F;
        breakIngot -= Mth.clamp(fortuneLevel, 0, 10) * 0.06F;
        if (this.level().random.nextFloat() < breakIngot) {
            ItemStack thisStack = this.getItem();
            thisStack.shrink(1);
            this.setItem(thisStack);
        }
        return true;
    }

    @Override
    public void tick() {
        boolean onGround = this.onGround();
        super.tick();
        if (this.onGround() != onGround) {
            refreshDimensions();
        }
    }

    @Override
    public void setOnGround(boolean grounded) {
        boolean updateSize = onGround() != grounded;
        super.setOnGround(grounded);
        if (updateSize) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        if (!this.onGround()) {
            return EntityType.ITEM.getDimensions();
        }
        return this.getType().getDimensions();
    }
}
