package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.lib.PerkConvertersAS;
import hellfirepvp.astralsorcery.common.perk.PerkConverter;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.node.focus.KeyGelu;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RegistryPerkConverters {

    private static final Map<ResourceLocation, PerkConverter> CONVERTERS = new LinkedHashMap<>();
    private static boolean initialized;

    private RegistryPerkConverters() {}

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        PerkConvertersAS.IDENTITY = register(new PerkConverter(AstralSorcery.key("identity")) {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifier(Player player, PlayerProgress progress,
                                                          PerkAttributeModifier modifier,
                                                          @Nullable ModifierSource owningSource) {
                return modifier;
            }
        });
        PerkConvertersAS.FOCUS_ALCARA = register(new PerkConverter(AstralSorcery.key("focus_alcara")) {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifier(Player player, PlayerProgress progress,
                                                          PerkAttributeModifier modifier,
                                                          @Nullable ModifierSource owningSource) {
                if (modifier.getAttributeType().equals(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EXP)) {
                    return modifier.convertModifier(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT,
                            modifier.getMode(), modifier.getValue(player, progress));
                }
                return modifier;
            }
        });
        PerkConvertersAS.FOCUS_GELU = register(new PerkConverter(AstralSorcery.key("focus_gelu")) {
            @Nonnull
            @Override
            public PerkAttributeModifier convertModifier(Player player, PlayerProgress progress,
                                                          PerkAttributeModifier modifier,
                                                          @Nullable ModifierSource owningSource) {
                if (modifier.getAttributeType().equals(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT)
                        && owningSource != null && !(owningSource instanceof KeyGelu)) {
                    return modifier.convertModifier(PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT,
                            ModifierType.STACKING_MULTIPLY, 1F);
                }
                return modifier;
            }
        });
    }

    public static synchronized List<PerkConverter> getConverters() {
        return Collections.unmodifiableList(new ArrayList<>(CONVERTERS.values()));
    }

    @Nullable
    public static synchronized PerkConverter get(ResourceLocation key) {
        return CONVERTERS.get(key);
    }

    private static <T extends PerkConverter> T register(T converter) {
        CONVERTERS.put(converter.getRegistryName(), converter);
        return converter;
    }
}
