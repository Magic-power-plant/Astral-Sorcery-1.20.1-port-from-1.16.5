package hellfirepvp.astralsorcery.common.data.world;

import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheDomain;
import hellfirepvp.astralsorcery.common.data.world.cache.base.SectionWorldData;
import hellfirepvp.astralsorcery.common.data.world.cache.base.WorldSection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RockCrystalBuffer extends SectionWorldData<RockCrystalBuffer.BufferSection> {

    public RockCrystalBuffer(WorldCacheDomain.SaveKey<?> key) {
        super(key, 10);
    }

    @Override
    protected BufferSection createNewSection(int sectionX, int sectionZ) {
        return new BufferSection(sectionX, sectionZ);
    }

    public List<BlockPos> collectPositions(ChunkPos center, int chunkRadius) {
        List<BlockPos> out = new LinkedList<>();
        for (int xx = -chunkRadius; xx <= chunkRadius; xx++) {
            for (int zz = -chunkRadius; zz <= chunkRadius; zz++) {
                ChunkPos other = new ChunkPos(center.x + xx, center.z + zz);
                BufferSection section = this.getSection(other.getWorldPosition());
                if (section != null) {
                    this.read(() -> out.addAll(section.crystalPositions));
                }
            }
        }
        return out;
    }

    public void addOre(BlockPos pos) {
        BufferSection section = this.getOrCreateSection(pos);
        this.write(() -> section.crystalPositions.add(pos));
        markDirty(section);
    }

    public void removeOre(BlockPos pos) {
        BufferSection section = this.getSection(pos);
        if (section != null) {
            this.write(() -> section.crystalPositions.remove(pos));
            markDirty(section);
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {}

    @Override
    public void readFromNBT(CompoundTag nbt) {}

    @Override
    public void updateTick(Level world) {}

    public static class BufferSection extends WorldSection {

        private final Set<BlockPos> crystalPositions = new HashSet<>();

        private BufferSection(int sX, int sZ) {
            super(sX, sZ);
        }

        @Override
        public void writeToNBT(CompoundTag tag) {
            ListTag posList = new ListTag();
            for (BlockPos exactPos : crystalPositions) {
                posList.add(NBTHelper.writeBlockPosToNBT(exactPos, new CompoundTag()));
            }
            tag.put("posList", posList);
        }

        @Override
        public void readFromNBT(CompoundTag tag) {
            crystalPositions.clear();

            ListTag entries = tag.getList("posList", Tag.TAG_COMPOUND);
            for (int j = 0; j < entries.size(); j++) {
                crystalPositions.add(NBTHelper.readBlockPosFromNBT(entries.getCompound(j)));
            }
        }
    }

}
