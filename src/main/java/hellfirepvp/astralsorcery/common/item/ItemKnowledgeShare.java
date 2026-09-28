/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktProgressionUpdate;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemKnowledgeShare
 * Created by HellFirePvP
 * Date: 16.08.2019 / 20:10
 */
// 1.16.5 built its own Properties (maxStackSize(1) + group(CommonProxy.ITEM_GROUP_AS)).
// 1.20.1 has no Properties#group, so the properties are supplied by ItemsAS and the tab comes from CreativeTabItem.
public class ItemKnowledgeShare extends Item implements CreativeTabItem {

    public ItemKnowledgeShare(Properties properties) {
        super(properties);
    }

    // 1.16.5 fillItemGroup: a blank share and a creative (maximize-everything) share in the main tab.
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(new ItemStack(this));

        ItemStack creative = new ItemStack(this);
        setCreative(creative);
        output.accept(creative);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (isCreative(stack)) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed.creative").withStyle(ChatFormatting.LIGHT_PURPLE));
            return;
        }
        if (getKnowledge(stack) == null) {
            tooltip.add(Component.translatable("astralsorcery.misc.knowledge.missing").withStyle(ChatFormatting.GRAY));
        } else {
            MutableComponent name = getKnowledgeOwnerName(stack);
            if (name != null) {
                tooltip.add(Component.translatable("astralsorcery.misc.knowledge.inscribed", name).withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() || world.isClientSide() || !(held.getItem() instanceof ItemKnowledgeShare)) {
            return InteractionResultHolder.success(held);
        }
        if (!isCreative(held) && (player.isShiftKeyDown() || getKnowledge(held) == null)) {
            tryInscribeKnowledge(held, player);
        } else {
            tryGiveKnowledge(held, player);
        }
        return InteractionResultHolder.success(held);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (stack.isEmpty() || player == null || context.getLevel().isClientSide() || !(stack.getItem() instanceof ItemKnowledgeShare)) {
            return InteractionResult.SUCCESS;
        }
        if (!isCreative(stack) && (player.isShiftKeyDown() || getKnowledge(stack) == null)) {
            tryInscribeKnowledge(stack, player);
        } else {
            tryGiveKnowledge(stack, player);
        }
        return InteractionResult.SUCCESS;
    }

    private void tryGiveKnowledge(ItemStack stack, Player player) {
        if (player instanceof ServerPlayer && MiscUtils.isPlayerFakeMP((ServerPlayer) player)) {
            return;
        }

        if (isCreative(stack)) {
            ResearchManager.forceMaximizeAll(player);
            return;
        }
        if (canInscribeKnowledge(stack, player)) return; //Means it's either empty or the player that has incsribed the knowledge is trying to use it.
        PlayerProgress progress = getKnowledge(stack);
        if (progress == null) return;
        ProgressionTier prev = progress.getTierReached();
        if (ResearchHelper.mergeApplyPlayerprogress(progress, player) && progress.getTierReached().isThisLater(prev)) {
            PktProgressionUpdate pkt = new PktProgressionUpdate(progress.getTierReached());
            // 1.16.5 PacketChannel.CHANNEL.sendToPlayer(PlayerEntity, msg); the ported helper is typed to ServerPlayer.
            if (player instanceof ServerPlayer) {
                PacketChannel.sendToPlayer((ServerPlayer) player, pkt);
            }
        }
    }

    private void tryInscribeKnowledge(ItemStack stack, Player player) {
        if (canInscribeKnowledge(stack, player)) {
            setKnowledge(stack, player, ResearchHelper.getProgress(player, LogicalSide.SERVER));
        }
    }

    @Nullable
    public static Player getKnowledgeOwner(ItemStack stack, MinecraftServer server) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(compound, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(owner);
    }

    @Nullable
    public static MutableComponent getKnowledgeOwnerName(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        if (!compound.contains("knowledgeOwnerName")) {
            return null;
        }
        return Component.Serializer.fromJson(compound.getString("knowledgeOwnerName"));
    }

    @Nullable
    public static PlayerProgress getKnowledge(ItemStack stack) {
        if (isCreative(stack)) return null;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        if (!compound.contains("knowledgeTag")) {
            return null;
        }
        CompoundTag tag = compound.getCompound("knowledgeTag");
        try {
            PlayerProgress progress = new PlayerProgress();
            progress.loadKnowledge(tag);
            return progress;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean canInscribeKnowledge(ItemStack stack, Player player) {
        if (isCreative(stack)) return false;

        CompoundTag compound = NBTHelper.getPersistentData(stack);
        UUID owner = NBTHelper.getUUID(compound, "knowledgeOwnerUUID", null);
        if (owner == null) {
            return true;
        }
        return player.getUUID().equals(owner);
    }

    public static void setKnowledge(ItemStack stack, Player player, PlayerProgress progress) {
        if (isCreative(stack) || !progress.isValid()) return;

        CompoundTag knowledge = new CompoundTag();
        progress.storeKnowledge(knowledge);
        CompoundTag compound = NBTHelper.getPersistentData(stack);
        compound.putString("knowledgeOwnerName", Component.Serializer.toJson(player.getDisplayName()));
        compound.putUUID("knowledgeOwnerUUID", player.getUUID());
        compound.put("knowledgeTag", knowledge);
    }

    public static boolean isCreative(ItemStack stack) {
        CompoundTag cmp = NBTHelper.getPersistentData(stack);
        if (!cmp.contains("creativeKnowledge")) {
            return false;
        }
        return cmp.getBoolean("creativeKnowledge");
    }

    private void setCreative(ItemStack stack) {
        NBTHelper.getPersistentData(stack).putBoolean("creativeKnowledge", true);
    }
}
