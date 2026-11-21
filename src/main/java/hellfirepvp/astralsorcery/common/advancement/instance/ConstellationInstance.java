package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConstellationInstance extends AbstractCriterionTriggerInstance {

    private boolean constellationMajor = false;
    private boolean constellationWeak = false;
    private boolean constellationMinor = false;
    private final Set<IConstellation> constellations = new HashSet<>();

    private ConstellationInstance(ResourceLocation id) {
        super(id, ContextAwarePredicate.ANY);
    }

    public ConstellationInstance(ResourceLocation id, ContextAwarePredicate predicate) {
        super(id, predicate);
    }

    public static ConstellationInstance any(ResourceLocation type) {
        return new ConstellationInstance(type);
    }

    public static ConstellationInstance anyMajor(ResourceLocation type) {
        ConstellationInstance instance = new ConstellationInstance(type);
        instance.constellationMajor = true;
        return instance;
    }

    public static ConstellationInstance anyWeak(ResourceLocation type) {
        ConstellationInstance instance = new ConstellationInstance(type);
        instance.constellationWeak = true;
        return instance;
    }

    public static ConstellationInstance anyMinor(ResourceLocation type) {
        ConstellationInstance instance = new ConstellationInstance(type);
        instance.constellationMinor = true;
        return instance;
    }

    public static ConstellationInstance anyOf(ResourceLocation type, IConstellation... cst) {
        ConstellationInstance instance = new ConstellationInstance(type);
        instance.constellations.addAll(Arrays.asList(cst));
        return instance;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext conditions) {
        JsonObject out = super.serializeToJson(conditions);
        if (this.constellationMajor) {
            out.addProperty("major", true);
        }
        if (this.constellationWeak) {
            out.addProperty("weak", true);
        }
        if (this.constellationMinor) {
            out.addProperty("minor", true);
        }
        if (!this.constellations.isEmpty()) {
            JsonArray names = new JsonArray();
            for (IConstellation cst : this.constellations) {
                names.add(cst.getRegistryName().toString());
            }
            out.add("constellations", names);
        }
        return out;
    }

    public static ConstellationInstance deserialize(ResourceLocation id, JsonObject json) {
        ConstellationInstance instance = new ConstellationInstance(id);
        instance.constellationMajor = GsonHelper.getAsBoolean(json, "major", false);
        instance.constellationWeak  = GsonHelper.getAsBoolean(json, "weak", false);
        instance.constellationMinor = GsonHelper.getAsBoolean(json, "minor", false);
        JsonArray constellationNames = GsonHelper.getAsJsonArray(json, "constellations", new JsonArray());
        for (int idx = 0; idx < constellationNames.size(); idx++) {
            JsonElement element = constellationNames.get(idx);
            String key = element.getAsString();
            IConstellation cst = ConstellationRegistry.getConstellation(ResourceLocation.parse(key));
            if (cst == null) {
                throw new IllegalArgumentException(String.format("Unknown constellation: %s - at constellations[%s]", key, idx));
            }
            instance.constellations.add(cst);
        }
        return instance;
    }

    public boolean test(IConstellation discovered) {
        if (constellationMajor && !(discovered instanceof IMajorConstellation)) {
            return false;
        }
        if (constellationWeak && (!(discovered instanceof IWeakConstellation) || discovered instanceof IMajorConstellation)) {
            return false;
        }
        if (constellationMinor && !(discovered instanceof IMinorConstellation)) {
            return false;
        }
        return constellations.isEmpty() || constellations.contains(discovered);
    }
}
