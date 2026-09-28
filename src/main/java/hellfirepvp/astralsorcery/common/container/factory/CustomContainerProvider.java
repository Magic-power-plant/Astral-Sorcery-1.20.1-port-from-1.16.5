/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.container.factory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomContainerProvider
 * Created by HellFirePvP
 * Date: 10.08.2019 / 09:11
 */
// 1.16.5 Container -> 1.20.1 AbstractContainerMenu, INamedContainerProvider -> MenuProvider
public abstract class CustomContainerProvider<C extends AbstractContainerMenu> implements MenuProvider {

    // 1.16.5 ContainerType -> 1.20.1 MenuType
    private final MenuType<C> type;

    public CustomContainerProvider(MenuType<C> type) {
        this.type = type;
    }

    @Override
    public Component getDisplayName() {
        //1.16.5 read the name straight off the registry entry (ContainerType#getRegistryName()); registry
        //entries no longer know their own name in 1.20.1, so the key is looked up in the registry instead.
        ResourceLocation key = ForgeRegistries.MENU_TYPES.getKey(this.type);
        // 1.16.5 TranslationTextComponent("screen.%s.%s", ...) -> Component#translatable with args
        return Component.translatable("screen.%s.%s", key.getNamespace(), key.getPath());
    }

    @Nonnull
    @Override
    public abstract C createMenu(int id, Inventory plInventory, Player player);

    // 1.16.5 PacketBuffer -> 1.20.1 FriendlyByteBuf
    protected abstract void writeExtraData(FriendlyByteBuf buf);

    public void openFor(ServerPlayer player) {
        // 1.16.5 NetworkHooks.openGui -> 1.20.1 NetworkHooks.openScreen
        NetworkHooks.openScreen(player, this, this::writeExtraData);
    }
}
