package hellfirepvp.astralsorcery.common.container;

import hellfirepvp.astralsorcery.common.blockentity.ObservatoryBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.lib.MenuTypesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.RegistryObject;

// Reference: 1.16.5 container/factory/CustomContainerProvider and the Container*Provider classes.
// Display names follow CustomContainerProvider#getDisplayName: "screen.<namespace>.<menu registry path>".
public class ContainerProviders {

    private ContainerProviders() {}

    /**
     * Mirrors 1.16.5 CustomContainerProvider#getDisplayName for the given menu type.
     */
    public static Component displayNameFor(RegistryObject<? extends MenuType<?>> type) {
        ResourceLocation key = type.getId();
        return Component.translatable(String.format("screen.%s.%s", key.getNamespace(), key.getPath()));
    }

    public static RegistryObject<? extends MenuType<?>> altarMenuType(AltarType altarType) {
        return switch (altarType) {
            case DISCOVERY -> MenuTypesAS.ALTAR_DISCOVERY;
            case ATTUNEMENT -> MenuTypesAS.ALTAR_ATTUNEMENT;
            case CONSTELLATION -> MenuTypesAS.ALTAR_CONSTELLATION;
            case RADIANCE -> MenuTypesAS.ALTAR_RADIANCE;
        };
    }

    public static class AltarProvider implements MenuProvider {
        private final BlockPos pos;
        private final AltarType altarType;
        private final Component displayName;

        public AltarProvider(BlockPos pos, AltarType altarType) {
            this(pos, altarType, displayNameFor(altarMenuType(altarType)));
        }

        public AltarProvider(BlockPos pos, AltarType altarType, Component displayName) {
            this.pos = pos;
            this.altarType = altarType;
            this.displayName = displayName;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            Level world = player.level();
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof AltarBlockEntity altarBlockEntity) {
                return switch (altarType) {
                    case DISCOVERY -> new ContainerAltarDiscovery(id, inv, altarBlockEntity);
                    case ATTUNEMENT -> new ContainerAltarAttunement(id, inv, altarBlockEntity);
                    case CONSTELLATION -> new ContainerAltarConstellation(id, inv, altarBlockEntity);
                    case RADIANCE -> new ContainerAltarTrait(id, inv, altarBlockEntity);
                };
            }
            return null;
        }

        public void writeScreenOpeningData(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(pos);
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }
    }

    public static class AltarDiscoveryProvider implements MenuProvider {
        private final BlockPos pos;
        private final Component displayName;

        public AltarDiscoveryProvider(BlockPos pos) {
            this(pos, displayNameFor(MenuTypesAS.ALTAR_DISCOVERY));
        }

        public AltarDiscoveryProvider(BlockPos pos, Component displayName) {
            this.pos = pos;
            this.displayName = displayName;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            Level world = player.level();
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof AltarBlockEntity altarBlockEntity) {
                return new ContainerAltarDiscovery(id, inv, altarBlockEntity);
            }
            return null;
        }

        public void writeScreenOpeningData(FriendlyByteBuf buffer) {
            buffer.writeBlockPos(pos);
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }
    }

    public static class ObservatoryProvider implements MenuProvider {
        private final BlockPos pos;
        private final Component displayName;

        public ObservatoryProvider(BlockPos pos) {
            this(pos, displayNameFor(MenuTypesAS.OBSERVATORY));
        }

        public ObservatoryProvider(BlockPos pos, Component displayName) {
            this.pos = pos;
            this.displayName = displayName;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            ObservatoryBlockEntity obs = MiscUtils.getBlockEntityAt(player.level(), pos, ObservatoryBlockEntity.class, true);
            return obs != null ? new ContainerObservatory(obs, id) : null;
        }

        public void writeScreenOpeningData(FriendlyByteBuf buffer) {
            ByteBufUtils.writePos(buffer, pos);
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }
    }

    public static class TomeProvider implements MenuProvider {
        private final ItemStack stackTome;
        private final int slotTome;
        private final Component displayName;

        public TomeProvider(ItemStack stackTome, int slotTome) {
            this(stackTome, slotTome, displayNameFor(MenuTypesAS.TOME));
        }

        public TomeProvider(ItemStack stackTome, int slotTome, Component displayName) {
            this.stackTome = stackTome;
            this.slotTome = slotTome;
            this.displayName = displayName;
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            return new ContainerTome(id, inv, player, stackTome, slotTome);
        }

        public void writeScreenOpeningData(FriendlyByteBuf buffer) {
            buffer.writeItem(this.stackTome);
            buffer.writeInt(this.slotTome);
        }

        @Override
        public Component getDisplayName() {
            return displayName;
        }
    }
}
