package hellfirepvp.astralsorcery.common.entity.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EntityCustomItemReplacement extends ItemEntity {

    @Nullable
    private ItemEntity replacedEntity;

    public EntityCustomItemReplacement(EntityType<? extends EntityCustomItemReplacement> type, Level world) {
        super(type, world);
    }

    public EntityCustomItemReplacement(EntityType<? extends EntityCustomItemReplacement> type, Level worldIn, double x, double y, double z) {
        super(type, worldIn);
        this.setPos(x, y, z);
    }

    public EntityCustomItemReplacement(EntityType<? extends EntityCustomItemReplacement> type, Level worldIn, double x, double y, double z, ItemStack stack) {
        super(type, worldIn);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.lifespan = stack.isEmpty() ? 6000 : stack.getEntityLifespan(worldIn);
    }

    public void setReplacedEntity(@Nullable ItemEntity replacedEntity) {
        this.replacedEntity = replacedEntity;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        if (this.replacedEntity != null &&
                this.tickCount < 5 &&
                !this.replacedEntity.isAlive()) {
            this.discard();
        }
    }
}
