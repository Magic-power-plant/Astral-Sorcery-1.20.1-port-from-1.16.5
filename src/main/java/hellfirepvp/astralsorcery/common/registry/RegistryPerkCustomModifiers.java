package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.lib.PerkCustomModifiersAS;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;


public final class RegistryPerkCustomModifiers {

    private static boolean initialized;

    private RegistryPerkCustomModifiers() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        PerkCustomModifiersAS.FOCUS_GELU = register(new PerkAttributeModifier(
                AstralSorcery.key("focus_gelu"), PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT,
                ModifierType.ADDED_MULTIPLY, 0.02F) {
            @Override
            protected void initModifier() {
                super.initModifier();
                this.setAbsolute();
            }

            @Override
            public float getValue(Player player, PlayerProgress progress) {
                return getRawValue() * progress.getPerkData().getEffectGrantingPerks().size();
            }

            @Override
            public boolean hasDisplayString() {
                return false;
            }
        });
        PerkCustomModifiersAS.FOCUS_ULTERIA = register(new PerkAttributeModifier(
                AstralSorcery.key("focus_ulteria"), PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EXP,
                ModifierType.STACKING_MULTIPLY, 1F + 0.05F) {
            @Override
            protected void initModifier() {
                super.initModifier();
                this.setAbsolute();
            }

            @Override
            public float getValue(Player player, PlayerProgress progress) {
                LogicalSide side = player.level().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
                return 1F + (0.05F * progress.getPerkData().getAvailablePerkPoints(player, side));
            }

            @Override
            public boolean hasDisplayString() {
                return false;
            }
        });
        PerkCustomModifiersAS.FOCUS_VORUX = register(new PerkAttributeModifier(
                AstralSorcery.key("focus_vorux"), PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT,
                ModifierType.ADDED_MULTIPLY, 0.01F) {
            @Override
            protected void initModifier() {
                super.initModifier();
                this.setAbsolute();
            }

            @Override
            public float getValue(Player player, PlayerProgress progress) {
                return getRawValue() * progress.getPerkData().getEffectGrantingPerks().size();
            }

            @Override
            public boolean hasDisplayString() {
                return false;
            }
        });
    }

    private static <T extends PerkAttributeModifier> T register(T modifier) {
        RegistriesAS.PERK_CUSTOM_MODIFIERS.register(modifier);
        return modifier;
    }
}
