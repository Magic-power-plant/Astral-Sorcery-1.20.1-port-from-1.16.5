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
import hellfirepvp.astralsorcery.common.data.config.registry.sets.BlockEntityAccelerationBlacklistEntry;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockEntityAccelerationBlacklistRegistry
 * Created by HellFirePvP
 * Date: 24.01.2020 / 20:13
 */
public class BlockEntityAccelerationBlacklistRegistry extends ConfigDataAdapter<BlockEntityAccelerationBlacklistEntry> {

    public static final BlockEntityAccelerationBlacklistRegistry INSTANCE = new BlockEntityAccelerationBlacklistRegistry();

    private final List<Class<?>> erroredBlockEntities = Lists.newArrayList();

    private BlockEntityAccelerationBlacklistRegistry() {}

    public boolean canBeInfluenced(BlockEntity blockEntity) {
        Class<?> blockEntityClass = blockEntity.getClass();
        if (erroredBlockEntities.contains(blockEntityClass)) {
            return false;
        }
        for (Class<?> excludedBlockEntity : erroredBlockEntities) {
            if (excludedBlockEntity.isAssignableFrom(blockEntityClass)) {
                return false;
            }
        }

        for (BlockEntityAccelerationBlacklistEntry entry : this.getConfiguredValues()) {
            if (entry.test(blockEntity)) {
                return false;
            }
        }
        return true;
    }

    public void addErrored(BlockEntity blockEntity) {
        if (blockEntity != null && !this.erroredBlockEntities.contains(blockEntity.getClass())) {
            this.erroredBlockEntities.add(blockEntity.getClass());
        }
    }

    @Override
    public List<BlockEntityAccelerationBlacklistEntry> getDefaultValues() {
        return Lists.newArrayList(
                new BlockEntityAccelerationBlacklistEntry("net.minecraft.world.level.block.piston.PistonBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry(RandomizableContainerBlockEntity.class.getName()),

                // Accelerating storage system components looks like a bad idea
                new BlockEntityAccelerationBlacklistEntry("appeng"),
                new BlockEntityAccelerationBlacklistEntry("raoulvdberge.refinedstorage"),

                // Filter all transmission & source tiles
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.base.network.TransmissionBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.base.network.SourceBlockEntity"),

                // Bad news for potentially various reasons
                new BlockEntityAccelerationBlacklistEntry(AltarBlockEntity.class.getName()),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.altar.AttunementAltarBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.observation.ObservatoryBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.ritual.RitualLinkBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.starlight.SpectralRelayBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.observation.TelescopeBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.utility.TranslucentBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconComponentBlockEntity"),
                new BlockEntityAccelerationBlacklistEntry("hellfirepvp.astralsorcery.common.blockentity.utility.VanishingBlockEntity")
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
    public BlockEntityAccelerationBlacklistEntry deserialize(String string) throws IllegalArgumentException {
        if (string.isEmpty()) {
            throw new IllegalArgumentException("BlockEntityAccelerationBlacklist entry filter must not be empty!");
        }
        return new BlockEntityAccelerationBlacklistEntry(string);
    }
}
