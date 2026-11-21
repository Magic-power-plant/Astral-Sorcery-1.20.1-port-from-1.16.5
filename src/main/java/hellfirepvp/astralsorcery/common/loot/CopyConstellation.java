package hellfirepvp.astralsorcery.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.api.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBlockEntity;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.lib.LootAS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyConstellation extends LootItemConditionalFunction {

    private CopyConstellation(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootAS.Functions.COPY_CONSTELLATION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_ENTITY)) {
            BlockEntity blockEntity = context.getParam(LootContextParams.BLOCK_ENTITY);
            if (blockEntity instanceof ConstellationBlockEntity && stack.getItem() instanceof ConstellationItem) {
                IWeakConstellation main = ((ConstellationBlockEntity) blockEntity).getAttunedConstellation();
                IMinorConstellation trait = ((ConstellationBlockEntity) blockEntity).getTraitConstellation();

                ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, main);
                ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, trait);
            }
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyConstellation::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyConstellation> {

        @Override
        public CopyConstellation deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
            return new CopyConstellation(conditions);
        }
    }
}
