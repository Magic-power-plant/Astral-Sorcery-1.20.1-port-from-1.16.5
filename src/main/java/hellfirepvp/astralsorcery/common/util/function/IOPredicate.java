package hellfirepvp.astralsorcery.common.util.function;

import java.io.IOException;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IOPredicate
 * Created by HellFirePvP
 * Date: 22.08.2019 / 19:35
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.common.util.IOPredicate); consumed by AlternatingSet.
 */
public interface IOPredicate<T> {

    boolean testUnsafe(T t) throws IOException;

}
