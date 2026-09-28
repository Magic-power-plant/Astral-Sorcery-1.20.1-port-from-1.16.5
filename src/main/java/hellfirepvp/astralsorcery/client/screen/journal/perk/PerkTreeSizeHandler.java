/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen.journal.perk;

import hellfirepvp.astralsorcery.client.screen.helper.SizeHandler;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkTreeSizeHandler
 * Created by HellFirePvP
 * Date: 08.08.2019 / 18:37
 */
public class PerkTreeSizeHandler extends SizeHandler {

    public PerkTreeSizeHandler() {
        setWidthHeightNodes(10F);
        setSpaceBetweenNodes(10F);
    }

    @Nullable
    @Override
    public float[] buildRequiredRectangle() {
        float leftMost = 0;
        float rightMost = 0;
        float upperMost = 0;
        float lowerMost = 0;

        //1.16.5 iterated PerkTree#getPerkPoints, which handed out the Collection<PerkTreePoint<?>> directly.
        //This tree's PerkTree maps the very same collection to its perks (PerkTreePoint#getPerk) before returning,
        //so the tree point is recovered through AbstractPerk#getPoint() - that is the identical cached instance
        //PreparedPerkTreeData collected, hence the same offsets in the same iteration order.
        for (AbstractPerk perk : PerkTree.PERK_TREE.getPerkPoints(LogicalSide.CLIENT)) {
            PerkTreePoint<?> point = perk.getPoint();
            Point.Float offset = point.getOffset();
            float x = offset.x;
            float y = offset.y;
            if (x < leftMost) leftMost = x;
            if (x > rightMost) rightMost = x;
            if (y > lowerMost) lowerMost = y;
            if (y < upperMost) upperMost = y;
        }
        return new float[] { leftMost, rightMost, upperMost, lowerMost };
    }
}
