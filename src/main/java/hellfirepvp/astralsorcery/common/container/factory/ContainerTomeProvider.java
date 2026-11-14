/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import hellfirepvp.astralsorcery.common.container.ContainerTome;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.IContainerFactory;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ContainerTomeProvider
 * Created by HellFirePvP
 * Date: 10.08.2019 / 09:15
 */
public class ContainerTomeProvider extends CustomContainerProvider<ContainerTome> {

    private final ItemStack stackTome;
    private final int slotTome;

    public ContainerTomeProvider(ItemStack stackTome, int slotTome) {
        //1.16.5 holds the ContainerType in a plain static field; the 1.20.1 tree registers menu types
        //through a DeferredRegister, so the RegistryObject has to be resolved here.
        super(MenuTypesAS.TOME.get());
        this.stackTome = stackTome;
        this.slotTome = slotTome;
    }

    @Override
    protected void writeExtraData(FriendlyByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.stackTome);
        buf.writeInt(this.slotTome);
    }

    @Nonnull
    @Override
    public ContainerTome createMenu(int id, Inventory plInventory, Player player) {
        return new ContainerTome(id, plInventory, player, this.stackTome, this.slotTome);
    }

    private static ContainerTome createFromPacket(int id, Inventory plInventory, FriendlyByteBuf data) {
        ItemStack tome = ByteBufUtils.readItemStack(data);
        int slot = data.readInt();
        return new ContainerTome(id, plInventory, plInventory.player, tome, slot);
    }

    // 1.16.5 net.minecraftforge.fml.network.IContainerFactory -> 1.20.1 net.minecraftforge.network.IContainerFactory
    public static class Factory implements IContainerFactory<ContainerTome> {

        @Override
        public ContainerTome create(int windowId, Inventory inv, FriendlyByteBuf data) {
            return ContainerTomeProvider.createFromPacket(windowId, inv, data);
        }
    }

}
