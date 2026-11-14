/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.effect;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TypeTreeBeaconColor
 * Created by HellFirePvP
 * Date: 05.09.2020 / 08:24
 */
public class TypeTreeBeaconColor extends ClientPatreonEffect {

    private final Color treeBeaconColor;

    public TypeTreeBeaconColor(UUID effectUUID, @Nullable FlareColor flareColor, Color treeBeaconColor) {
        super(effectUUID, flareColor);
        this.treeBeaconColor = treeBeaconColor;
    }

    public Color getTreeBeaconColor() {
        return treeBeaconColor;
    }
}
