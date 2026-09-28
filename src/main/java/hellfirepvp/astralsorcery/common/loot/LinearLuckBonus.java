package hellfirepvp.astralsorcery.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.minecraft.util.RandomSource;
import java.util.Set;

public class LinearLuckBonus extends LootItemConditionalFunction {

    private LinearLuckBonus(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.LINEAR_LUCK_BONUS;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        ItemStack tool = context.getParam(LootContextParams.TOOL);
        if (tool != null) {
            int luck = 0;
            Entity e = context.getParam(LootContextParams.THIS_ENTITY);
            if (e instanceof Player && ((Player) e).hasEffect(MobEffects.LUCK)) {
                luck += ((Player) e).getEffect(MobEffects.LUCK).getAmplifier() + 1;
            }
            luck += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            luck += EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, tool);

            RandomSource rand = context.getRandom();
            int size = 0;
            for (int i = 0; i < luck; i++) {
                size += rand.nextInt(3) + 1;
            }
            stack.setCount(stack.getCount() + size);
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(LinearLuckBonus::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LinearLuckBonus> {

        @Override
        public LinearLuckBonus deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new LinearLuckBonus(conditions);
        }
    }
}
