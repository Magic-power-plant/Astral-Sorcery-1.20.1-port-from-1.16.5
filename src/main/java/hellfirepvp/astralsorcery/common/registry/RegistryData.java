/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.common.lib.DataAS;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryData
 * Created by HellFirePvP
 * Date: 03.07.2019 / 16:47
 */
// Port notes for 1.16.5 common/registry/RegistryData.java (40 lines):
//
// The body of 1.16.5 init() (ref:31-38) is reproduced verbatim by common/lib/DataAS#init(): the same
// WorldCacheManager.createDomain(AstralSorcery.MODID) and the same four createSaveKey calls, in the same order,
// with the same four on-disk identifiers ("gateway-cache", "lightnetwork", "storagenetwork", "rock-crystals")
// and the same four suppliers. DataAS additionally runs it from a static initializer and makes it idempotent,
// so this class delegates instead of repeating the calls - creating the domain twice would replace the live
// one and orphan every SaveKey handed out so far.
//
//   hellfirepvp.observerlib.common.data.WorldCacheManager (1.16.5 ref:16)
//                    -> common/data/world/cache/WorldCacheManager, the embedded copy of the ObserverLib class
//                       (ObserverLib is a source-only reference in this port and must never be imported).
//                       WorldCacheDomain / WorldCacheDomain.SaveKey come from the same embedded package.
public class RegistryData {

    private RegistryData() {}

    public static void init() {
        DataAS.init();
    }
}
