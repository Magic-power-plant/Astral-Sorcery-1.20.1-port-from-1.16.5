package hellfirepvp.astralsorcery.common.perk.data.builder;

import com.google.gson.*;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class PerkDataProvider implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    protected final DataGenerator generator;

    public PerkDataProvider(DataGenerator generator) {
        this.generator = generator;
    }

    public abstract void registerPerks(Consumer<FinishedPerk> registrar);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path path = this.generator.getPackOutput().getOutputFolder();

        List<FinishedPerk> builtPerks = new ArrayList<>();
        this.registerPerks(finishedPerk -> {
            ResourceLocation perkName = finishedPerk.perk.getRegistryName();
            Point.Float offset = finishedPerk.perk.getOffset();
            if (builtPerks.stream().anyMatch(knownPerk -> knownPerk.perk.getOffset().equals(offset))) {
                throw new IllegalArgumentException("Duplicate perk registration at " + offset + " for " + perkName);
            }
            if (builtPerks.contains(finishedPerk)) {
                throw new IllegalArgumentException("Duplicate perk registry name: " + perkName);
            }
            builtPerks.add(finishedPerk);
        });

        builtPerks.sort(Comparator.naturalOrder());
        builtPerks.forEach(perk -> {
            Path filePath = path.resolve(String.format("data/%s/perks/%s.json", perk.perk.getRegistryName().getNamespace(), perk.perk.getRegistryName().getPath()));
            this.savePerkFile(cache, perk.serialize(), filePath);
        });

        JsonObject allPerks = new JsonObject();
        builtPerks.forEach(perk -> allPerks.add(perk.perk.getRegistryName().toString(), perk.serialize()));
        this.savePerkFile(cache, allPerks, path.resolve("data/astralsorcery/perks/_full_tree.json"));
        return CompletableFuture.completedFuture(null);
    }

    private void savePerkFile(CachedOutput cache, JsonElement perk, Path filePath) {
        try {
            String perkJson = GSON.toJson(perk);
            cache.writeIfNeeded(filePath, perkJson.getBytes(StandardCharsets.UTF_8), com.google.common.hash.Hashing.sha1().hashBytes(perkJson.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException exc) {
            AstralSorcery.LOG.error("Couldn't save perk {}", filePath, exc);
        }
    }

    @Override
    public String getName() {
        return "Perks";
    }

    public static class FinishedPerk implements Comparable<FinishedPerk> {

        private final AbstractPerk perk;
        private final List<ResourceLocation> connections;

        public FinishedPerk(AbstractPerk perk, List<ResourceLocation> connections) {
            this.perk = perk;
            this.connections = connections;
        }

        private JsonObject serialize() {
            JsonObject object = this.perk.serializePerk();
            JsonArray array = new JsonArray();
            for (ResourceLocation connection : this.connections) {
                array.add(connection.toString());
            }
            object.add("connection", array);
            return object;
        }

        @Override
        public int compareTo(FinishedPerk that) {
            return this.perk.getRegistryName().compareTo(that.perk.getRegistryName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FinishedPerk that = (FinishedPerk) o;
            return Objects.equals(perk.getRegistryName(), that.perk.getRegistryName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(perk.getRegistryName());
        }
    }
}
