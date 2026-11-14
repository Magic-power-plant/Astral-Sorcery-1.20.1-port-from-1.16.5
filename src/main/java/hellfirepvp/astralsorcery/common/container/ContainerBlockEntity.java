package hellfirepvp.astralsorcery.common.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerBlockEntity<T extends BlockEntity> extends AbstractContainerMenu {

    protected final T blockEntity;

    protected ContainerBlockEntity(@Nullable MenuType<?> type, int id, T blockEntity) {
        super(type, id);
        this.blockEntity = blockEntity;
    }

    public T getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
