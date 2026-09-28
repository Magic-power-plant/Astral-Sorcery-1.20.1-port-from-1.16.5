package hellfirepvp.astralsorcery.common.data.world.cache;

import java.io.File;
import java.io.IOException;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IWorldRelatedData
 * Created by HellFirePvP
 * Date: 12.08.2016 / 11:33
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.common.data.IWorldRelatedData);
 * ObserverLib is a source-only reference for this port, not a runtime dependency.
 */
public interface IWorldRelatedData {

    public WorldCacheDomain.SaveKey<?> getSaveKey();

    public abstract void markSaved();

    public void writeData(File baseDirectory, File backupDirectory) throws IOException;

    public void readData(File baseDirectory) throws IOException;

}
