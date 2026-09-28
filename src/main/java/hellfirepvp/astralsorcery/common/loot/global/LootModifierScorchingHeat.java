package hellfirepvp.astralsorcery.common.loot.global;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.util.RecipeHelper;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Collectors;

public class LootModifierScorchingHeat extends LootModifier {

    public static final Codec<LootModifierScorchingHeat> CODEC = RecordCodecBuilder.create(
            inst -> LootModifier.codecStart(inst).apply(inst, LootModifierScorchingHeat::new)
    );

    private LootModifierScorchingHeat(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return generatedLoot;
        }
        return generatedLoot.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    Optional<Tuple<ItemStack, Float>> furnaceResult = RecipeHelper.findSmeltingResult(context.getLevel(), stack);
                    if (context.hasParam(LootContextParams.THIS_ENTITY)) {
                        Entity e = context.getParamOrNull(LootContextParams.THIS_ENTITY);
                        if (e instanceof Player player) {
                            furnaceResult.ifPresent(result -> ForgeEventFactory.firePlayerSmeltedEvent(player, result.getA()));
                        }
                    }
                    furnaceResult.ifPresent(result -> {
                        ItemStack resultStack = result.getA();
                        float resultExp = result.getB();

                        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
                        if (tool != null && !tool.isEmpty() && !(resultStack.getItem() instanceof BlockItem)) {
                            int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);
                            if (silkTouch <= 0) {
                                int addedCount = 0;
                                int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
                                if (fortuneLevel > 0) {
                                    addedCount = Math.max(context.getRandom().nextInt(fortuneLevel + 2) - 1, 0);
                                    resultStack.setCount(resultStack.getCount() * (addedCount + 1));
                                }

                                resultExp *= (addedCount + 1);
                                if (resultExp > 0) {
                                    int iExp = (int) resultExp;
                                    float partialExp = resultExp - iExp;
                                    if (partialExp > 0 && partialExp > context.getRandom().nextFloat()) {
                                        iExp += 1;
                                    }
                                    if (iExp >= 1) {
                                        Vec3 blockPos = context.getParamOrNull(LootContextParams.ORIGIN);
                                        if (blockPos != null) {
                                            ServerLevel world = context.getLevel();
                                            world.addFreshEntity(new ExperienceOrb(world, blockPos.x(), blockPos.y(), blockPos.z(), iExp));
                                        }
                                    }
                                }
                            }
                        }
                    });
                    return furnaceResult.map(Tuple::getA).orElse(stack);
                })
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
