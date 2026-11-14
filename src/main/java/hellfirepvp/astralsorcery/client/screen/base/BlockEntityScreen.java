/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import hellfirepvp.astralsorcery.common.util.blockentity.NamedInventoryBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockEntityScreen
 * Created by HellFirePvP
 * Date: 02.08.2019 / 20:35
 */
//1.16.5: TileEntity -> 1.20.1: BlockEntity
public class BlockEntityScreen<T extends BlockEntity & NamedInventoryBlockEntity> extends WidthHeightScreen {

    private final T blockEntity;

    protected BlockEntityScreen(T blockEntity, int guiHeight, int guiWidth) {
        super(blockEntity.getDisplayName(), guiHeight, guiWidth);
        this.blockEntity = blockEntity;
    }

    public T getBlockEntity() {
        return blockEntity;
    }

    @Override
    public void tick() {
        super.tick();

        //1.16.5: Minecraft#world -> 1.20.1: Minecraft#level; World -> Level
        Level clWorld = Minecraft.getInstance().level;
        if (blockEntity.isRemoved() ||
                clWorld == null ||
                //1.16.5: World#getDimensionKey() -> 1.20.1: Level#dimension(); TileEntity#getWorld() -> BlockEntity#getLevel()
                !clWorld.dimension().equals(blockEntity.getLevel().dimension())) {
            this.onClose(); //1.16.5: Screen#closeScreen()
        }
    }
}
