/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import hellfirepvp.astralsorcery.common.structure.match.structure.MatchableStructure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureSnapshot
 * Created by HellFirePvP
 * Date: 12.02.2020 / 18:46
 *
 * Embedded VERBATIM from ObserverLib (hellfirepvp.observerlib.client.preview.StructureSnapshot); ObserverLib is
 * a source-only reference for this port, not a runtime dependency. Only the package declaration, the license
 * header, the {@link OnlyIn} annotation and the MatchableStructure import differ.
 *
 * Pins the tick a preview was created at, so the structure's descriptive states do not animate while the preview
 * is shown.
 */
@OnlyIn(Dist.CLIENT)
public class StructureSnapshot {

    private final MatchableStructure structure;
    private final long snapshot;

    StructureSnapshot(MatchableStructure structure, long snapshot) {
        this.structure = structure;
        this.snapshot = snapshot;
    }

    MatchableStructure getStructure() {
        return structure;
    }

    long getSnapshotTick() {
        return snapshot;
    }
}
