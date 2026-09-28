package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.ConstellationInstance;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class DiscoverConstellationTrigger extends ListenerCriterionTrigger<ConstellationInstance> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AstralSorcery.MODID, "find_constellation");

    public DiscoverConstellationTrigger() {
        super(ID);
    }

    @Override
    public ConstellationInstance createInstance(JsonObject object, DeserializationContext conditions) {
        return ConstellationInstance.deserialize(getId(), object);
    }

    public void trigger(ServerPlayer player, IConstellation cst) {
        Listeners<ConstellationInstance> listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger((i) -> i.test(cst));
        }
    }

}
