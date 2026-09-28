package hellfirepvp.astralsorcery.common.advancement.instance;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.advancement.PerkLevelTrigger;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.fml.LogicalSide;

public class PerkLevelInstance extends AbstractCriterionTriggerInstance {

    private int levelNeeded = 0;

    private PerkLevelInstance(ResourceLocation criterionIn) {
        super(criterionIn, ContextAwarePredicate.ANY);
    }

    public PerkLevelInstance(ResourceLocation criterionIn, ContextAwarePredicate predicate) {
        super(criterionIn, predicate);
    }

    public static PerkLevelInstance reachLevel(int level) {
        PerkLevelInstance instance = new PerkLevelInstance(PerkLevelTrigger.ID);
        instance.levelNeeded = level;
        return instance;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext conditions) {
        JsonObject out = super.serializeToJson(conditions);
        out.addProperty("levelNeeded", this.levelNeeded);
        return out;
    }

    public static PerkLevelInstance deserialize(ResourceLocation id, JsonObject json) {
        PerkLevelInstance instance = new PerkLevelInstance(id);
        instance.levelNeeded = GsonHelper.getAsInt(json, "levelNeeded");
        return instance;
    }

    public boolean test(ServerPlayer player) {
        return ResearchHelper.getProgress(player, LogicalSide.SERVER).getPerkData().getPerkLevel(player, LogicalSide.SERVER) >= this.levelNeeded;
    }
}
