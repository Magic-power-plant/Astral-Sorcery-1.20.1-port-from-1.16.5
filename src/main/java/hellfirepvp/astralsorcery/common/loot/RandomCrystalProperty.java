package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class RandomCrystalProperty extends LootItemConditionalFunction {

    private RandomCrystalProperty(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.RANDOM_CRYSTAL_PROPERTIES;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (stack.getItem() instanceof CrystalAttributeGenItem) {
            CrystalAttributes attr = CrystalGenerator.generateNewAttributes(stack);
            ((CrystalAttributeGenItem) stack.getItem()).setAttributes(stack, attr);
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(RandomCrystalProperty::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<RandomCrystalProperty> {

        @Override
        public RandomCrystalProperty deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new RandomCrystalProperty(conditions);
        }
    }
}
