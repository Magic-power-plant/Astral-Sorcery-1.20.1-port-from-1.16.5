package hellfirepvp.astralsorcery.common.entity.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityDazzlingGem extends EntityItemExplosionResistant {

    public EntityDazzlingGem(EntityType<? extends EntityDazzlingGem> type, Level world) {
        super(type, world);
    }

    public EntityDazzlingGem(EntityType<? extends EntityDazzlingGem> type, Level world, double x, double y, double z) {
        super(type, world, x, y, z);
    }

    public EntityDazzlingGem(EntityType<? extends EntityDazzlingGem> type, Level world, double x, double y, double z, ItemStack stack) {
        super(type, world, x, y, z, stack);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && this.tickCount + 10 >= this.lifespan) {
            this.lifespan = 0;
        }
    }
}
