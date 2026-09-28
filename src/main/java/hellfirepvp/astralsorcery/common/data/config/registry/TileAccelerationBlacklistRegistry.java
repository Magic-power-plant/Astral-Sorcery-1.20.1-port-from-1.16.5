/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigDataAdapter;
import hellfirepvp.astralsorcery.common.data.config.registry.sets.TileAccelerationBlacklistEntry;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileAccelerationBlacklistRegistry
 * Created by HellFirePvP
 * Date: 24.01.2020 / 20:13
 */
public class TileAccelerationBlacklistRegistry extends ConfigDataAdapter<TileAccelerationBlacklistEntry> {

    public static final TileAccelerationBlacklistRegistry INSTANCE = new TileAccelerationBlacklistRegistry();

    private final List<Class<?>> erroredTiles = Lists.newArrayList();

    private TileAccelerationBlacklistRegistry() {}

    public boolean canBeInfluenced(BlockEntity tile) {
        Class<?> tileClass = tile.getClass();
        if (erroredTiles.contains(tileClass)) {
            return false;
        }
        for (Class<?> excludedTile : erroredTiles) {
            if (excludedTile.isAssignableFrom(tileClass)) {
                return false;
            }
        }

        for (TileAccelerationBlacklistEntry entry : this.getConfiguredValues()) {
            if (entry.test(tile)) {
                return false;
            }
        }
        return true;
    }

    public void addErrored(BlockEntity tile) {
        if (tile != null && !this.erroredTiles.contains(tile.getClass())) {
            this.erroredTiles.add(tile.getClass());
        }
    }

    @Override
    public List<TileAccelerationBlacklistEntry> getDefaultValues() {
        return Lists.newArrayList(
                new TileAccelerationBlacklistEntry("net.minecraft.world.level.block.piston.PistonBlockEntity"),
                new TileAccelerationBlacklistEntry(RandomizableContainerBlockEntity.class.getName()),

                // Accelerating storage system components looks like a bad idea
                new TileAccelerationBlacklistEntry("appeng"),
                new TileAccelerationBlacklistEntry("raoulvdberge.refinedstorage"),

                // Filter all transmission & source tiles
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.base.network.TileTransmissionBase"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.base.network.TileSourceBase"),

                // Bad news for potentially various reasons
                new TileAccelerationBlacklistEntry(TileAltar.class.getName()),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileAttunementAltar"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileObservatory"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileRitualLink"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileRitualPedestal"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileSpectralRelay"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileTelescope"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileTranslucentBlock"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileTreeBeaconComponent"),
                new TileAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.tile.TileVanishing")
        );
    }

    @Override
    public String getSectionName() {
        return "tile_acceleration_blacklist";
    }

    @Override
    public String getCommentDescription() {
        return "Accepts & matches against strings: " +
                "1) what a tileentity-type's registry name starts with, " +
                "2) what a tileentity's fully qualified class name starts with, " +
                "3) (special case) a fully qualified class name (Instances & sub-class instance of that class will be blacklisted)";
    }

    @Override
    public String getTranslationKey() {
        return translationKey("data");
    }

    @Override
    public Predicate<Object> getValidator() {
        return (obj) -> obj instanceof String;
    }

    @Nullable
    @Override
    public TileAccelerationBlacklistEntry deserialize(String string) throws IllegalArgumentException {
        if (string.isEmpty()) {
            throw new IllegalArgumentException("TileAccelerationBlacklist entry filter must not be empty!");
        }
        return new TileAccelerationBlacklistEntry(string);
    }
}
