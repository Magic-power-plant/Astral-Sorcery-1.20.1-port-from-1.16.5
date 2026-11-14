/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.block.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemKnowledgeShare;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.dispenser.FluidContainerDispenseBehavior;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryItems
 * Created by HellFirePvP
 * Date: 01.06.2019 / 13:57
 */
// Port notes for 1.16.5 common/registry/RegistryItems.java (207 lines):
//
// The object-creation half of the 1.16.5 class is gone from common/registry in the 1.20.1 target - it moved into
// DeferredRegister holders, so re-implementing it here would register every item twice:
//   registerItems()              (1.16.5 ref:67-127)   -> common/lib/ItemsAS (ItemsAS.ITEMS)
//   registerItemBlocks()         (ref:129-131, 168-172) -> common/lib/BlocksAS#registerBlockWithItem, which registers
//                                                          the BlockItem into ItemsAS.ITEMS under the block name
//   registerFluidContainerItems()(ref:133-135)          -> common/lib/FluidsAS (LIQUID_STARLIGHT_BUCKET, itself
//                                                          registered into ItemsAS.ITEMS)
//   registerItem(T)              (ref:174-182)          -> DeferredRegister#register; the name is no longer derived
//                                                          from the class via NameUtil.fromClass, it is passed in
//   buildItemBlockProperties()   (ref:184-205)          -> Item.Properties are built inline by BlocksAS
//
// What genuinely still belongs here - the non-registry item side work - is ported in full below.
//
//TODO_PORT[AS-REG-08]: registerItem's ItemDynamicColor bookkeeping (1.16.5
// common/registry/RegistryItems.java:63, 178-180) collected a `List<ItemDynamicColor> colorItems` while items were
// being constructed. Nothing collects that list in 1.20.1 because the items are constructed by ItemsAS, so
// registerColors below walks ItemsAS.ITEMS instead. That is equivalent for every item 1.16.5 collected, with one
// intentional widening: 1.16.5 never collected BlockItems (registerItemBlock did not touch colorItems) whereas
// ItemsAS.ITEMS also holds them. No BlockItem in the tree implements ItemDynamicColor, so no behaviour changes;
// if one ever does, it would now get a colour handler where 1.16.5 gave it none.
public class RegistryItems {

    private RegistryItems() {}

    @OnlyIn(Dist.CLIENT)
    public static void registerColors(RegisterColorHandlersEvent.Item event) {
        // 1.16.5: colorItems.forEach(item -> itemColorEvent.getItemColors().register(item::getColor, (Item) item));
        for (RegistryObject<Item> entry : ItemsAS.ITEMS.getEntries()) {
            Item item = entry.get();
            if (item instanceof ItemDynamicColor) {
                event.getItemColors().register(((ItemDynamicColor) item)::getColor, item);
            }
        }
    }

    public static void registerDispenseBehaviors() {
        // 1.16.5: DispenserBlock.registerDispenseBehavior(BUCKET_LIQUID_STARLIGHT, ...) - the bucket is called
        // FluidsAS.LIQUID_STARLIGHT_BUCKET in the 1.20.1 target, and the method is DispenserBlock#registerBehavior.
        DispenserBlock.registerBehavior(FluidsAS.LIQUID_STARLIGHT_BUCKET.get(), FluidContainerDispenseBehavior.getInstance());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerItemProperties() {
        // 1.16.5 ItemModelsProperties.registerProperty(Item, ResourceLocation, IItemPropertyGetter) became
        // ItemProperties.register(Item, ResourceLocation, ItemPropertyFunction) - the getter gained the entity seed
        // parameter and its entity argument is a LivingEntity rather than a bare Entity.
        ItemProperties.register(ItemsAS.INFUSED_GLASS.get(),
                ResourceLocation.parse("engraved"),
                (stack, world, entity, seed) ->
                        ItemInfusedGlass.getEngraving(stack) != null ? 1 : 0);
        ItemProperties.register(ItemsAS.KNOWLEDGE_SHARE.get(),
                ResourceLocation.parse("written"),
                (stack, world, entity, seed) ->
                        ItemKnowledgeShare.isCreative(stack) || ItemKnowledgeShare.getKnowledge(stack) != null ? 1 : 0);
        ItemProperties.register(ItemsAS.RESONATOR.get(),
                ResourceLocation.parse("upgrade"),
                (stack, world, entity, seed) -> {
                    if (!(entity instanceof Player)) {
                        return ItemResonator.ResonatorUpgrade.STARLIGHT.ordinal() / (float) ItemResonator.ResonatorUpgrade.values().length;
                    }
                    ItemResonator.ResonatorUpgrade current = ItemResonator.getCurrentUpgrade((Player) entity, stack);
                    return current.ordinal() / (float) ItemResonator.ResonatorUpgrade.values().length;
                });
        // 1.16.5: Item.getItemFromBlock(block) -> 1.20.1: Block#asItem(); ItemStack#getDamage -> #getDamageValue;
        // Property#getAllowedValues -> Property#getPossibleValues.
        ItemProperties.register(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get().asItem(),
                ResourceLocation.parse("stage"),
                (stack, world, entity, seed) ->
                        ((float) stack.getDamageValue()) / BlockCelestialCrystalCluster.STAGE.getPossibleValues().size());
        ItemProperties.register(BlocksAS.GEM_CRYSTAL_CLUSTER.get().asItem(),
                ResourceLocation.parse("stage"),
                (stack, world, entity, seed) ->
                        ((float) stack.getDamageValue()) / BlockGemCrystalCluster.STAGE.getPossibleValues().size());
    }
}
