/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.effect;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Immutable decoded data. Render resources are resolved only by client providers. */
public final class PatreonEffectDefinition extends PatreonEffect {
    private final PatreonEffectType type;
    private final UUID ownerUUID;
    private final List<String> parameters;
    private final Color treeBeaconColor;

    public PatreonEffectDefinition(PatreonEffectType type, UUID ownerUUID, List<String> parameters) {
        super(UUID.fromString(parameters.get(0)), readFlareColor(type, parameters));
        this.type = Objects.requireNonNull(type);
        this.ownerUUID = Objects.requireNonNull(ownerUUID);
        this.parameters = List.copyOf(parameters);
        this.treeBeaconColor = type == PatreonEffectType.TREE_BEACON_COLOR
                ? new Color(Integer.parseInt(parameters.get(2))) : null;
    }

    @Nullable
    private static FlareColor readFlareColor(PatreonEffectType type, List<String> parameters) {
        if (type == PatreonEffectType.FLARE_DYNAMIC_COLOR) return null;
        int index = type == PatreonEffectType.HELMET ? 2 : 1;
        if (index >= parameters.size() && type == PatreonEffectType.HELMET) return null;
        String name = parameters.get(index);
        return "null".equals(name) ? null : FlareColor.valueOf(name);
    }

    public PatreonEffectType getType() { return type; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public List<String> getParameters() { return parameters; }
    @Nullable
    public Color getTreeBeaconColor() { return treeBeaconColor; }

    @Override
    public boolean hasPartialEntity() {
        return type == PatreonEffectType.FLARE_DYNAMIC_COLOR || super.hasPartialEntity();
    }
}
