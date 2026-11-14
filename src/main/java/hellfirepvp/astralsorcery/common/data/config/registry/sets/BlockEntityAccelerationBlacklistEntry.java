/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigDataSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockEntityAccelerationBlacklistEntry
 * Created by HellFirePvP
 * Date: 24.01.2020 / 20:13
 */
public class BlockEntityAccelerationBlacklistEntry implements ConfigDataSet, Predicate<BlockEntity> {

    private final String filterString;
    private Class<?> filteredSuperClass;

    public BlockEntityAccelerationBlacklistEntry(String filterString) {
        this.filterString = filterString;
        try {
            this.filteredSuperClass = Class.forName(BlockEntityClassNames.resolve(filterString), false,
                    BlockEntityAccelerationBlacklistEntry.class.getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            this.filteredSuperClass = null; //Then we match by string..
        }
    }

    @Override
    public boolean test(BlockEntity blockEntity) {
        String testStr = this.filterString.toLowerCase(Locale.ROOT);
        if (testStr.isEmpty()) {
            return false;
        }

        if (this.filteredSuperClass != null) {
            return this.filteredSuperClass.isAssignableFrom(blockEntity.getClass());
        }

        ResourceLocation key = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
        if (key != null && key.toString().toLowerCase(Locale.ROOT).startsWith(testStr)) {
            return true;
        }

        return BlockEntityClassNames.matches(blockEntity.getClass().getName(), this.filterString);
    }

    @Nonnull
    @Override
    public String serialize() {
        return this.filterString;
    }
}
