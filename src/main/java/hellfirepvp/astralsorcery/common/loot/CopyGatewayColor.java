package hellfirepvp.astralsorcery.common.loot;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.block.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import hellfirepvp.astralsorcery.common.blockentity.CelestialGatewayBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

public class CopyGatewayColor extends LootItemConditionalFunction {

    private CopyGatewayColor(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.newHashSet(LootContextParams.BLOCK_ENTITY);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.COPY_GATEWAY_COLOR;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CelestialGatewayBlockEntity) {
            ((CelestialGatewayBlockEntity) blockEntity).getColor().ifPresent(color -> {
                BlockCelestialGateway.setColor(stack, color);
            });
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyGatewayColor::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyGatewayColor> {

        @Override
        public CopyGatewayColor deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new CopyGatewayColor(conditions);
        }
    }
}
