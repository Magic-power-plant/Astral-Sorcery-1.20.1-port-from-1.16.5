/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.perk.node.MajorPerk;
import java.awt.Point;

/** Layout data for a major perk point. */
public class PerkTreeMajor<T extends MajorPerk> extends PerkTreePoint<T> {
    public PerkTreeMajor(T perk, Point.Float offset) {
        super(perk, offset);
        this.setRenderSize((int) (this.getRenderSize() * 1.4));
    }
}
