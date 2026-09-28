package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.PerkLevelInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class PerkLevelTrigger extends ListenerCriterionTrigger<PerkLevelInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("perk_level");

    public PerkLevelTrigger() {
        super(ID);
    }

    @Override
    public PerkLevelInstance createInstance(JsonObject object, DeserializationContext conditions) {
        return PerkLevelInstance.deserialize(getId(), object);
    }

    public void trigger(ServerPlayer player) {
        Listeners<PerkLevelInstance> listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger((i) -> i.test(player));
        }
    }
}
