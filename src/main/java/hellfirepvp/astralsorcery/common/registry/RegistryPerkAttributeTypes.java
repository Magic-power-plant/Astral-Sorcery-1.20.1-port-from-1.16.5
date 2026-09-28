package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.perk.PerkAttributeLimiter;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import hellfirepvp.astralsorcery.common.perk.type.*;

import static hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS.*;

public class RegistryPerkAttributeTypes {

    private static final Map<ResourceLocation, PerkAttributeType> TYPES = new LinkedHashMap<>();

    private RegistryPerkAttributeTypes() {}

    public static void init() {
        ATTR_TYPE_INC_ALL_ELEMENTAL_RESIST = new AttributeTypeAllElementalResist();
        ATTR_TYPE_ARMOR = new AttributeTypeArmor();
        ATTR_TYPE_ARMOR_TOUGHNESS = new AttributeTypeArmorToughness();
        ATTR_TYPE_PROJ_SPEED = new AttributeTypeArrowSpeed();
        ATTR_TYPE_ATTACK_SPEED = new AttributeTypeAttackSpeed();
        ATTR_TYPE_INC_HARVEST_SPEED = new AttributeTypeBreakSpeed();
        ATTR_TYPE_INC_CRIT_CHANCE = new AttributeTypeCritChance();
        ATTR_TYPE_INC_CRIT_MULTIPLIER = new AttributeTypeCritMultiplier();
        ATTR_TYPE_INC_DODGE = new AttributeTypeDodge();
        ATTR_TYPE_INC_ENCH_EFFECT = new AttributeTypeEnchantmentEffectiveness();
        ATTR_TYPE_ATTACK_LIFE_LEECH = new AttributeTypeLifeLeech();
        ATTR_TYPE_LIFE_RECOVERY = new AttributeTypeLifeRecovery();
        ATTR_TYPE_HEALTH = new AttributeTypeMaxHealth();
        ATTR_TYPE_REACH = new AttributeTypeMaxReach();
        ATTR_TYPE_MELEE_DAMAGE = new AttributeTypeMeleeAttackDamage();
        ATTR_TYPE_MOVESPEED = new AttributeTypeMovementSpeed();
        ATTR_TYPE_INC_PERK_EFFECT = new AttributeTypePerkEffect();
        ATTR_TYPE_POTION_DURATION = new AttributeTypePotionDuration();
        ATTR_TYPE_PROJ_DAMAGE = new AttributeTypeProjectileAttackDamage();
        ATTR_TYPE_SWIMSPEED = new AttributeTypeSwimSpeed();
        ATTR_TYPE_INC_THORNS = new AttributeTypeThorns();
        ATTR_TYPE_ALIGNMENT_CHARGE_MAXIMUM = new AttributeTypeChargeMaximum();
        ATTR_TYPE_MINING_SIZE = new AttributeTypeMiningSize();
        ATTR_TYPE_COOLDOWN_REDUCTION = new AttributeTypeCooldown();
        ATTR_TYPE_LUCK = new AttributeTypeLuck();

        ATTR_TYPE_BLEED_DURATION = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_BLEED_DURATION, false);
        ATTR_TYPE_BLEED_CHANCE = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_BLEED_CHANCE, false);
        ATTR_TYPE_BLEED_STACKS = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_BLEED_STACKS, false);
        ATTR_TYPE_RAMPAGE_DURATION = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_RAMPAGE_DURATION, false);
        ATTR_TYPE_INC_THORNS_RANGED = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_INC_THORNS_RANGED, false);
        ATTR_TYPE_ARC_CHAINS = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_ARC_CHAINS, false);
        ATTR_TYPE_ALIGNMENT_CHARGE_REGENERATION = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_ALIGNMENT_CHARGE_REGENERATION, true);

        ATTR_TYPE_INC_PERK_EXP = PerkAttributeType.makeDefault(KEY_ATTR_TYPE_INC_PERK_EXP, true);

        PerkAttributeLimiter.limit(ATTR_TYPE_INC_DODGE, () -> 0.0, () -> 0.75);
        PerkAttributeLimiter.limit(ATTR_TYPE_INC_ALL_ELEMENTAL_RESIST, () -> 0.0, () -> 0.6);
        PerkAttributeLimiter.limit(ATTR_TYPE_ATTACK_LIFE_LEECH, () -> 0.0, () -> 0.2);
        PerkAttributeLimiter.limit(ATTR_TYPE_COOLDOWN_REDUCTION, () -> 0.0, () -> 0.8);

        rebuildLocalIndex();
        RegistryPerkAttributeReaders.init();
        RegistryPerkConverters.init();
        RegistryPerkCustomModifiers.init();
    }

    public static synchronized List<PerkAttributeType> getTypes() {
        return Collections.unmodifiableList(new ArrayList<>(TYPES.values()));
    }

    @Nullable
    public static synchronized PerkAttributeType get(ResourceLocation key) {
        return TYPES.get(key);
    }

    private static void rebuildLocalIndex() {
        TYPES.clear();
        for (Field field : PerkAttributeTypesAS.class.getFields()) {
            try {
                Object value = field.get(null);
                if (value instanceof PerkAttributeType type) {
                    TYPES.put(type.getRegistryName(), type);
                }
            } catch (IllegalAccessException ignored) {
                // Public fields are expected; an inaccessible extension field is skipped.
            }
        }
    }
}
