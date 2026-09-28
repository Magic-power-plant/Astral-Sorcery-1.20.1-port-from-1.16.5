package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.loot.CopyConstellation;
import hellfirepvp.astralsorcery.common.loot.CopyCrystalProperties;
import hellfirepvp.astralsorcery.common.loot.CopyGatewayColor;
import hellfirepvp.astralsorcery.common.loot.LinearLuckBonus;
import hellfirepvp.astralsorcery.common.loot.RandomCrystalProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.RegisterEvent;

public class LootAS {

    private LootAS() {}

    public static final ResourceLocation SHRINE_CHEST = AstralSorcery.key("shrine_chest");

    public static final ResourceLocation STARFALL_SHOOTING_STAR_REWARD = AstralSorcery.key("gameplay/starfall/shooting_star");

    public static class Functions {

        public static LootItemFunctionType LINEAR_LUCK_BONUS;
        public static LootItemFunctionType RANDOM_CRYSTAL_PROPERTIES;
        public static LootItemFunctionType COPY_CRYSTAL_PROPERTIES;
        public static LootItemFunctionType COPY_CONSTELLATION;
        public static LootItemFunctionType COPY_GATEWAY_COLOR;

        // Called from common/registry/RegistryLoot#registerLootFunctions with the RegisterEvent helper for
        // Registries.LOOT_FUNCTION_TYPE: vanilla registries are frozen during mod construction in Forge 1.20.1,
        // so registering here directly from CommonProxy#initialize failed CONSTRUCT with "Registry is already frozen".
        public static void register(RegisterEvent.RegisterHelper<LootItemFunctionType> helper) {
            LINEAR_LUCK_BONUS = register(helper, "linear_luck_bonus", new LinearLuckBonus.Serializer());
            RANDOM_CRYSTAL_PROPERTIES = register(helper, "random_crystal_property", new RandomCrystalProperty.Serializer());
            COPY_CRYSTAL_PROPERTIES = register(helper, "copy_crystal_properties", new CopyCrystalProperties.Serializer());
            COPY_CONSTELLATION = register(helper, "copy_constellation", new CopyConstellation.Serializer());
            COPY_GATEWAY_COLOR = register(helper, "copy_gateway_color", new CopyGatewayColor.Serializer());
        }

        private static LootItemFunctionType register(RegisterEvent.RegisterHelper<LootItemFunctionType> helper,
                                                     String name, Serializer<? extends LootItemFunction> serializer) {
            LootItemFunctionType type = new LootItemFunctionType(serializer);
            helper.register(AstralSorcery.key(name), type);
            return type;
        }
    }
}
