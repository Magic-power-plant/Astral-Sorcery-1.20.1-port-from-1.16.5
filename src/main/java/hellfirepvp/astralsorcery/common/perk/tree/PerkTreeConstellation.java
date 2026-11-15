/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import java.awt.Point;

/** Constellation association and layout data for root and minor constellation points. */
public class PerkTreeConstellation<T extends AbstractPerk> extends PerkTreePoint<T> {
    public static final int ROOT_SPRITE_SIZE = 50;
    public static final int MINOR_SPRITE_SIZE = 40;

    private final IConstellation associatedConstellation;
    private final int perkSpriteSize;

    public PerkTreeConstellation(T perk, Point.Float offset, IConstellation associatedConstellation, int perkSpriteSize) {
        super(perk, offset);
        this.associatedConstellation = associatedConstellation;
        this.perkSpriteSize = perkSpriteSize;
        this.setRenderSize(perkSpriteSize / 2);
    }

    public IConstellation getAssociatedConstellation() { return associatedConstellation; }
    public int getPerkSpriteSize() { return perkSpriteSize; }
}
