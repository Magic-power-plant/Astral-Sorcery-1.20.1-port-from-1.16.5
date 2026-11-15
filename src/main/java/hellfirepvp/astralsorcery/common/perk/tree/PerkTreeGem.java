/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.node.socket.GemSocketPerk;
import java.awt.Point;

/** Layout data for a gem socket point. */
public class PerkTreeGem<T extends AbstractPerk & GemSocketPerk> extends PerkTreePoint<T> {
    public PerkTreeGem(T perk, Point.Float offset) {
        super(perk, offset);
        this.setRenderSize((int) (this.getRenderSize() * 1.4));
    }
}
