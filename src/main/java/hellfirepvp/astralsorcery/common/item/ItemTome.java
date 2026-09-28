package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBaseItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.container.ContainerProviders;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.item.base.PerkExperienceRevealer;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

// Reference: 1.16.5 ItemTome. The creative tab is applied centrally (CommonProxy item group) instead of Properties#group.
public class ItemTome extends Item implements PerkExperienceRevealer {

    public ItemTome(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (world.isClientSide() && !player.isShiftKeyDown()) {
            // BLOCKED_PORT[AS-ITM-05]: depends on AS-UI-01 (GuiType#deserialize returns null and ClientProxy has no client-side openGui override yet, so the journal does not open)
            AstralSorcery.getProxy().openGui(player, GuiType.TOME);
        } else if (!world.isClientSide() && player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            ContainerProviders.TomeProvider provider = new ContainerProviders.TomeProvider(player.getItemInHand(hand), player.getInventory().selected);
            NetworkHooks.openScreen(serverPlayer, provider, provider::writeScreenOpeningData);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockState blockstate = world.getBlockState(context.getClickedPos());
        if (blockstate.getBlock() instanceof LecternBlock) {
            return LecternBlock.tryPlaceBook(context.getPlayer(), world, context.getClickedPos(), blockstate, context.getItemInHand()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Nonnull
    public static Container getTomeStorage(ItemStack stack, Player player) {
        SimpleContainer inventory = new SimpleContainer(27);
        getStoredConstellations(stack, player).stream().map(cst -> {
            ItemStack cstPaper = new ItemStack(ItemsAS.CONSTELLATION_PAPER.get());
            if (cstPaper.getItem() instanceof ConstellationBaseItem) {
                ((ConstellationBaseItem) cstPaper.getItem()).setConstellation(cstPaper, cst);
            }
            return cstPaper;
        }).forEach(inventory::addItem);
        return inventory;
    }

    @Nonnull
    public static List<IConstellation> getStoredConstellations(ItemStack stack, Player player) {
        LinkedList<IConstellation> out = new LinkedList<>();

        PlayerProgress prog = ResearchHelper.getProgress(player, player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        if (prog.isValid()) {
            prog.getStoredConstellationPapers().stream()
                    .map(ConstellationRegistry::getConstellation)
                    .filter(Objects::nonNull)
                    .forEach(out::add);
        }
        return out;
    }
}
