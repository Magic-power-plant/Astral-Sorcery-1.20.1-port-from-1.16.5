/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.base;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationDiscoveryBlockEntityScreen
 * Created by HellFirePvP
 * Date: 15.02.2020 / 08:47
 */
//1.16.5: TileEntity -> 1.20.1: BlockEntity
public abstract class ConstellationDiscoveryBlockEntityScreen<T extends BlockEntity, D extends ConstellationDiscoveryScreen.DrawArea> extends ConstellationDiscoveryScreen<D> {

    private final T blockEntity;

    /**
     * TODO_PORT[AS-SCR-02]: 1.16.5 declares this class as
     * {@code TileConstellationDiscoveryScreen<T extends TileEntity & NamedInventoryTile, D ...>} and derives the
     * screen title inside the constructor from {@code tile.getDisplayName()}
     * (1.16.5 client/screen/base/TileConstellationDiscoveryScreen.java:23-30). This tree's
     * {@code common/tile/TileTelescope} port dropped {@code implements NamedInventoryTile} /
     * {@code getDisplayName()}, so {@code ScreenTelescope} cannot satisfy the intersection bound and the whole
     * screen would not compile. The bound is therefore widened to {@code BlockEntity} and the title is handed in
     * explicitly by the two subclasses; the values they pass are exactly what the corresponding 1.16.5 tiles
     * returned from {@code getDisplayName()}, so behaviour is unchanged. Restore the intersection bound and the
     * {@code (T tile, int guiHeight, int guiWidth)} constructor once TileTelescope implements NamedInventoryTile
     * again (see externalChangesNeeded).
     */
    //1.16.5: ITextComponent -> 1.20.1: Component
    protected ConstellationDiscoveryBlockEntityScreen(T blockEntity, Component title, int guiHeight, int guiWidth) {
        super(title, guiHeight, guiWidth);
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
