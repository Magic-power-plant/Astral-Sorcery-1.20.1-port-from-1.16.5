package hellfirepvp.astralsorcery.common.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerTileEntity<T extends BlockEntity> extends AbstractContainerMenu {

    protected final T tileEntity;

    protected ContainerTileEntity(@Nullable MenuType<?> type, int id, T tileEntity) {
        super(type, id);
        this.tileEntity = tileEntity;
    }

    public T getTileEntity() {
        return this.tileEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
