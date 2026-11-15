/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.tree;

import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import java.awt.Point;
import java.util.Objects;

/** Perk identity, position and layout size, shared by tree preparation and the journal. */
public class PerkTreePoint<T extends AbstractPerk> {
    private final Point.Float offset;
    private final T perk;
    private int renderSize = 11;

    public PerkTreePoint(T perk, Point.Float offset) {
        this.offset = offset;
        this.perk = perk;
    }

    public void setRenderSize(int renderSize) { this.renderSize = renderSize; }
    public int getRenderSize() { return renderSize; }
    public T getPerk() { return perk; }
    public Point.Float getOffset() { return offset; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerkTreePoint<?> that = (PerkTreePoint<?>) o;
        return Objects.equals(offset, that.offset);
    }

    @Override
    public int hashCode() { return Objects.hash(offset); }
}
