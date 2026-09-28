package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

public interface IConstellation extends Comparable<IConstellation> {
    int STAR_GRID_INDEX = 31;
    int STAR_GRID_WIDTH_HEIGHT = STAR_GRID_INDEX + 1;

    StarLocation addStar(int x, int y);

    StarConnection addConnection(StarLocation star1, StarLocation star2);

    int getSortingId();

    List<StarLocation> getStars();

    List<StarConnection> getStarConnections();

    String getSimpleName();

    String getTranslationKey();

    default Component getConstellationName() {
        return Component.translatable(this.getTranslationKey());
    }

    default Component getConstellationTypeDescription() {
        String type = "unknown";
        if (this instanceof IMajorConstellation) {
            type = "major";
        } else if (this instanceof IWeakConstellation) {
            type = "weak";
        } else if (this instanceof IMinorConstellation) {
            type = "minor";
        }
        return Component.translatable(String.format("astralsorcery.journal.constellation.type.%s", type));
    }

    default Component getConstellationTag() {
        return Component.translatable(this.getTranslationKey() + ".tag");
    }

    default Component getConstellationDescription() {
        return Component.translatable(this.getTranslationKey() + ".description");
    }

    default Component getConstellationEnchantmentDescription() {
        return Component.translatable(this.getTranslationKey() + ".enchantments");
    }

    static String getDefaultSaveKey() {
        return "constellationName";
    }

    List<Ingredient> getConstellationSignatureItems();

    default IConstellation addSignatureItem(ItemStack item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    default IConstellation addSignatureItem(Item item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    default IConstellation addSignatureItem(Supplier<Ingredient> ingredient) {
        return this;
    }

    Color getConstellationColor();

    default Color getTierRenderColor() {
        if (this instanceof IMinorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MINOR;
        }
        if (this instanceof IMajorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MAJOR;
        }
        return ColorsAS.CONSTELLATION_TYPE_WEAK;
    }

    boolean canDiscover(Player player, PlayerProgress progress);

    default void writeToNBT(CompoundTag compound) {
        writeToNBT(compound, getDefaultSaveKey());
    }

    default void writeToNBT(CompoundTag compound, String key) {
        compound.putString(key, getRegistryName().toString());
    }

    @Nullable
    static IConstellation readFromNBT(CompoundTag compound) {
        return readFromNBT(compound, getDefaultSaveKey());
    }

    @Nullable
    static IConstellation readFromNBT(CompoundTag compound, String key) {
        String id = compound.getString(key);
        if (id.isEmpty()) {
            return null;
        }
        return ConstellationRegistry.getConstellation(ResourceLocation.tryParse(id));
    }

    ResourceLocation getRegistryName();

    default ResourceLocation getId() {
        return getRegistryName();
    }

    default String getName() {
        return getSimpleName();
    }

    default Component getNameComponent() {
        return getConstellationName();
    }

    default float getBrightness() {
        return 1.0F;
    }

    default float getMaxSize() {
        return 1.0F;
    }

    //1.16.5 resolved this through `RegistriesAS.REGISTRY_ENGRAVING_EFFECT.getValue(this.getRegistryName())`
    //(1.16.5 IConstellation.java:103-105). Returning null unconditionally made every engraving inert:
    //RegistryEngravingEffects#init built all twelve EngravingEffects, but EngravedStarMap - the only reader -
    //got null for every constellation, so an engraved star map granted no potion, enchantment or modifier.
    //EngravingEffect now self-registers into its own map under the same constellation registry name, matching
    //what ConstellationEffectProvider and MantleEffectHelper already do for the two sibling effect types.
    @Nullable
    default EngravingEffect getEngravingEffect() {
        return EngravingEffect.getEffect(this.getRegistryName());
    }
}
