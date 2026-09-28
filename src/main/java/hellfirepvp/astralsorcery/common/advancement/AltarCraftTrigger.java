package hellfirepvp.astralsorcery.common.advancement;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.advancement.instance.AltarRecipeInstance;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class AltarCraftTrigger extends ListenerCriterionTrigger<AltarRecipeInstance> {

    public static final ResourceLocation ID = AstralSorcery.key("altar_craft");

    public AltarCraftTrigger() {
        super(ID);
    }

    @Override
    public AltarRecipeInstance createInstance(JsonObject object, DeserializationContext conditions) {
        return AltarRecipeInstance.deserialize(getId(), object);
    }

    public void trigger(ServerPlayer player, AltarRecipe recipe, ItemStack output) {
        Listeners<AltarRecipeInstance> listeners = this.listeners.get(player.getAdvancements());
        if (listeners != null) {
            listeners.trigger((i) -> i.test(recipe, output));
        }
    }

}
