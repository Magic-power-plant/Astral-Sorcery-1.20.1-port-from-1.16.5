package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderAddedPercentage;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderBreakSpeed;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderFlatAttribute;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderPercentageAttribute;
import hellfirepvp.astralsorcery.common.perk.reader.ReaderVanillaAttribute;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


/** Registers the built-in readers into the canonical perk-attribute reader registry. */
public final class RegistryPerkAttributeReaders {

    private static boolean initialized;

    private RegistryPerkAttributeReaders() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_MELEE_DAMAGE,
                ref("generic.attack_damage")).formatAsDecimal());
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_HEALTH,
                ref("generic.max_health")));
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_MOVESPEED,
                ref("generic.movement_speed")).formatAsDecimal());
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_SWIMSPEED,
                ForgeMod.SWIM_SPEED).formatAsDecimal());
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_ARMOR,
                ref("generic.armor")));
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_ARMOR_TOUGHNESS,
                ref("generic.armor_toughness")));
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_ATTACK_SPEED,
                ref("generic.attack_speed")).formatAsDecimal());
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_REACH,
                ForgeMod.BLOCK_REACH).formatAsDecimal());
        register(new ReaderVanillaAttribute(PerkAttributeTypesAS.ATTR_TYPE_LUCK,
                ref("generic.luck")).formatAsDecimal());

        register(new ReaderFlatAttribute(PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_MAXIMUM,
                1000F)).formatAsDecimal();
        register(new ReaderFlatAttribute(PerkAttributeTypesAS.ATTR_TYPE_MINING_SIZE, 0F));

        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EXP);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_ALIGNMENT_CHARGE_REGENERATION);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_CRIT_MULTIPLIER);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_ALL_ELEMENTAL_RESIST);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_PROJ_DAMAGE);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_PROJ_SPEED);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_LIFE_RECOVERY);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_POTION_DURATION);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_ENCH_EFFECT);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_DODGE);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_CRIT_CHANCE);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_ATTACK_LIFE_LEECH);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_INC_THORNS);
        registerDefaultReader(PerkAttributeTypesAS.ATTR_TYPE_COOLDOWN_REDUCTION);

        register(new ReaderBreakSpeed(PerkAttributeTypesAS.ATTR_TYPE_INC_HARVEST_SPEED));
    }

    private static RegistryObject<Attribute> ref(String key) {
        return RegistryObject.create(ResourceLocation.parse(key), ForgeRegistries.ATTRIBUTES);
    }

    private static PerkAttributeReader registerDefaultReader(PerkAttributeType type) {
        return type.isMultiplicative()
                ? register(new ReaderPercentageAttribute(type))
                : register(new ReaderAddedPercentage(type));
    }

    private static <T extends PerkAttributeReader> T register(T reader) {
        return RegistriesAS.PERK_ATTRIBUTE_READERS.register(reader);
    }
}
