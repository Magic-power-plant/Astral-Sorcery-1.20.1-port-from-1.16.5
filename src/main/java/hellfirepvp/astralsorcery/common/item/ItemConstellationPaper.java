/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.client.util.sound.ClientSoundHelper;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBaseItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.item.base.CreativeTabItem;
import hellfirepvp.astralsorcery.common.lib.CreativeTabsAS;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemExplosionResistant;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemConstellationPaper
 * Created by HellFirePvP
 * Date: 21.07.2019 / 14:47
 */
// 1.16.5 built its own Properties (maxStackSize(1) + group(CommonProxy.ITEM_GROUP_AS_PAPERS)).
// 1.20.1 has no Properties#group, so the properties are supplied by ItemsAS and the tab comes from CreativeTabItem.
public class ItemConstellationPaper extends Item implements ItemDynamicColor, ConstellationBaseItem, CreativeTabItem {

    public ItemConstellationPaper(Properties properties) {
        super(properties);
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        return CreativeTabsAS.TAB_AS_PAPERS.get();
    }

    // 1.16.5 fillItemGroup: a blank paper plus one paper per registered constellation in the papers tab.
    @Override
    public void fillCreativeTab(CreativeModeTab.Output output) {
        output.accept(new ItemStack(this, 1));

        for (IConstellation c : ConstellationRegistry.getAllConstellations()) {
            ItemStack cPaper = new ItemStack(this, 1);
            setConstellation(cPaper, c);
            output.accept(cPaper);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> toolTip, TooltipFlag flag) {
        IConstellation c = getConstellation(stack);
        if (c != null && c.canDiscover(Minecraft.getInstance().player, ResearchHelper.getClientProgress())) {
            // 1.16.5 getConstellationName() returned IFormattableTextComponent; 1.20.1 returns an immutable Component, hence copy().
            toolTip.add(c.getConstellationName().copy().withStyle(ChatFormatting.BLUE));
        } else {
            toolTip.add(Component.translatable("astralsorcery.misc.noinformation").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return InteractionResultHolder.success(held);
        }
        if (world.isClientSide() && getConstellation(held) != null) {
            ClientSoundHelper.playSoundClient(SoundsAS.GUI_JOURNAL_PAGE, 1F, 1F);
            AstralSorcery.getProxy().openGui(player, GuiType.CONSTELLATION_PAPER, getConstellation(held));
        }
        return InteractionResultHolder.success(held);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        EntityItemExplosionResistant res = new EntityItemExplosionResistant(EntityTypesAS.ITEM_EXPLOSION_RESISTANT.get(), world, location.getX(), location.getY(), location.getZ(), itemstack);
        res.load(location.saveWithoutId(new CompoundTag()));
        if (itemstack.getItem() instanceof ItemConstellationPaper) {
            IConstellation cst = getConstellation(itemstack);
            if (cst != null) {
                res.applyColor(cst.getConstellationColor());
            }
        }
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (world.isClientSide() || !(entity instanceof Player)) {
            return;
        }

        IConstellation cst = getConstellation(stack);
        if (cst == null) {
            PlayerProgress progress = ResearchHelper.getProgress((Player) entity, LogicalSide.SERVER);

            List<IConstellation> constellations = new ArrayList<>();
            for (IConstellation c : ConstellationRegistry.getAllConstellations()) {
                if (c.canDiscover((Player) entity, progress)) {
                    constellations.add(c);
                }
            }

            for (ResourceLocation strConstellation : progress.getKnownConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null) {
                    constellations.remove(c);
                }
            }
            for (ResourceLocation strConstellation : progress.getSeenConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null) {
                    constellations.remove(c);
                }
            }

            IConstellation constellation = MiscUtils.getRandomEntry(constellations, world.random);
            if (constellation != null) {
                setConstellation(stack, constellation);
            }
        }


        cst = getConstellation(stack);
        if (cst != null) {
            PlayerProgress progress = ResearchHelper.getProgress((Player) entity, LogicalSide.SERVER);

            boolean has = false;
            for (ResourceLocation strConstellation : progress.getSeenConstellations()) {
                IConstellation c = ConstellationRegistry.getConstellation(strConstellation);
                if (c != null && c.equals(cst)) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                if (cst.canDiscover((Player) entity, progress) && ResearchManager.memorizeConstellation(cst, (Player) entity)) {
                    // 1.16.5 took an ICommandSource here; the ported helper is typed to Player.
                    ResearchHelper.sendConstellationMemorizationMessage((Player) entity, progress, cst);
                }
            }
        }

        super.inventoryTick(stack, world, entity, slot, isSelected);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        IConstellation c = getConstellation(stack);
        if (c != null) {
            if (ResearchHelper.getClientProgress().hasConstellationDiscovered(c)) {
                return 0xFF000000 | c.getConstellationColor().getRGB();
            }
        }
        return 0xFF595959;
    }

    @Nullable
    public IConstellation getConstellation(ItemStack stack) {
        return IConstellation.readFromNBT(NBTHelper.getPersistentData(stack));
    }

    public boolean setConstellation(ItemStack stack, @Nullable IConstellation constellation) {
        constellation.writeToNBT(NBTHelper.getPersistentData(stack));
        return true;
    }
}
