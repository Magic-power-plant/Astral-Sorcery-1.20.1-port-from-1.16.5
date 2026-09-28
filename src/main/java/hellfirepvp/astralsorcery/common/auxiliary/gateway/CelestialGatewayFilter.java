package hellfirepvp.astralsorcery.common.auxiliary.gateway;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CelestialGatewayFilter {

    private final File gatewayFilter;
    private Set<ResourceKey<Level>> cache = new HashSet<>();

    CelestialGatewayFilter() {
        this.gatewayFilter = this.loadFilter();
        this.loadCache();
    }

    private File loadFilter() {
        File dataDir = AstralSorcery.getProxy().getASServerDataDirectory();
        File gatewayFilter = new File(dataDir, "gateway_filter.dat");
        if (!gatewayFilter.exists()) {
            try {
                gatewayFilter.createNewFile();
            } catch (IOException exc) {
                throw new IllegalStateException("Couldn't create plain world filter file! Are we missing file permissions?", exc);
            }
        }
        return gatewayFilter;
    }

    public boolean hasGateways(ResourceLocation worldKey) {
        return this.cache.contains(worldKey);
    }

    void addDim(ResourceKey<Level> worldKey) {
        if (cache.add(worldKey)) {
            this.saveCache();
        }
    }

    void removeDim(ResourceKey<Level> worldKey) {
        if (cache.remove(worldKey)) {
            this.saveCache();
        }
    }

    private void loadCache() {
        try {
            CompoundTag tag = NbtIo.read(this.gatewayFilter);
            if (tag == null) {
                this.cache = new HashSet<>();
                return;
            }
            ListTag list = tag.getList("list", Tag.TAG_STRING);
            this.cache = new HashSet<>();
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation location = ResourceLocation.parse(list.getString(i));
                this.cache.add(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location));
            }
        } catch (IOException ignored) {
            this.cache = new HashSet<>();
        }
    }

    private void saveCache() {
        try {
            ListTag list = new ListTag();
            for (ResourceKey<Level> dimType : cache) {
                list.add(StringTag.valueOf(dimType.location().toString()));
            }
            CompoundTag cmp = new CompoundTag();
            cmp.put("list", list);
            NbtIo.write(cmp, this.gatewayFilter);
        } catch (IOException ignored) {}
    }
}
