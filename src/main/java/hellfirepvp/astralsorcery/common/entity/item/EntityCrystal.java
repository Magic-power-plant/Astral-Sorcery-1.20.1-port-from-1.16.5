package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalProperty;
import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import hellfirepvp.astralsorcery.common.item.ItemChisel;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

public class EntityCrystal extends EntityItemExplosionResistant implements InteractableEntity {

    public EntityCrystal(EntityType<? extends EntityCrystal> type, Level world) {
        super(type, world);
    }

    public EntityCrystal(EntityType<? extends EntityCrystal> type, Level world, double x, double y, double z) {
        super(type, world, x, y, z);
    }

    public EntityCrystal(EntityType<? extends EntityCrystal> type, Level world, double x, double y, double z, ItemStack stack) {
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
                if (!thisStack.isEmpty() && thisStack.getItem() instanceof ItemCrystalBase) {

                    CrystalAttributes thisAttributes = ((ItemCrystalBase) thisStack.getItem()).getAttributes(thisStack);
                    if (thisAttributes != null) {

                        boolean doDamage = false;
                        if (this.level().random.nextFloat() < 0.35F) {
                            int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, held);
                            doDamage = this.splitCrystal(thisAttributes, fortuneLevel);
                        }
                        if (doDamage || this.level().random.nextFloat() < 0.35F) {
                            held.hurtAndBreak(1, (Player) entity, (player) -> player.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean splitCrystal(CrystalAttributes thisAttributes, int fortuneLevel) {
        ItemCrystalBase newBase = ((ItemCrystalBase) this.getItem().getItem()).getInertDuplicateItem();
        if (newBase == null) {
            return false;
        }
        ItemStack created = new ItemStack(newBase);
        if (created.isEmpty()) {
            return false;
        }
        int maxSplit = Mth.ceil(thisAttributes.getTotalTierLevel() / 2F);
        if (maxSplit >= thisAttributes.getTotalTierLevel()) {
            return false;
        }

        int lostModifiers = 0;
        if (maxSplit > 1 && this.level().random.nextFloat() < (0.6F / (fortuneLevel + 1))) {
            lostModifiers++;
            if (maxSplit > 2 && this.level().random.nextFloat() < (0.2F / (fortuneLevel + 1))) {
                lostModifiers++;
            }
        }

        CrystalAttributes resultThisAttributes = thisAttributes;
        CrystalAttributes.Builder resultSplitAttributes = CrystalAttributes.Builder.newBuilder(false);
        for (int i = 0; i < maxSplit; i++) {
            CrystalProperty prop = MiscUtils.getRandomEntry(resultThisAttributes.getProperties(), this.level().random);
            if (prop == null) {
                break;
            }
            resultThisAttributes = resultThisAttributes.modifyLevel(prop, -1);
            if (lostModifiers > 0) {
                lostModifiers--;
            } else {
                resultSplitAttributes.addProperty(prop, 1);
            }
        }

        ((ItemCrystalBase) this.getItem().getItem()).setAttributes(this.getItem(), resultThisAttributes);
        newBase.setAttributes(created, resultSplitAttributes.build());
        ItemUtils.dropItemNaturally(level(), this.getX(), this.getY() + 0.25F, this.getZ(), created);
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.tickCount + 10 >= this.lifespan) {
            this.lifespan = 0;
        }
    }
}
