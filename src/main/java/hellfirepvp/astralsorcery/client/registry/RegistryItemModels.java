/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import hellfirepvp.astralsorcery.common.block.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.item.ItemKnowledgeShare;
import hellfirepvp.astralsorcery.common.item.ItemResonator;
import hellfirepvp.astralsorcery.common.item.base.client.ItemDynamicColor;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.registries.RegistryObject;

/** Item tint handlers and model properties, installed exclusively from ClientProxy. */
@OnlyIn(Dist.CLIENT)
public final class RegistryItemModels {

    private RegistryItemModels() {}

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
