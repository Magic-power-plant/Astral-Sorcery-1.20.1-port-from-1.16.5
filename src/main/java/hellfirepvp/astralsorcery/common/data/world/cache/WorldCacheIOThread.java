package hellfirepvp.astralsorcery.common.data.world.cache;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: WorldCacheIOThread
 * Created by HellFirePvP
 * Date: 29.06.2019 / 19:50
 *
 * Embedded from ObserverLib (hellfirepvp.observerlib.common.data.WorldCacheIOThread);
 * ObserverLib is a source-only reference for this port, not a runtime dependency.
 *
 * Directory layout below {@link WorldCacheDomain#getSaveDirectory()} is unchanged from ObserverLib:
 * "DIM_[sanitized dimension id]/[save key identifier]" plus "-Backup" and "-Error" siblings.
 */
public class WorldCacheIOThread extends TimerTask {

    private static WorldCacheIOThread saveTask;
    private static Timer ioThread;

    private final Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData>>> worldSaveQueue = Maps.newHashMap();
    private final Map<WorldCacheDomain, Map<ResourceLocation, List<IWorldRelatedData>>> awaitingSaveQueue = Maps.newHashMap();
    private boolean inSave = false, skipTick = false;

    private WorldCacheIOThread() {}

    public static void onServerStart() {
        if (ioThread != null) {
            return;
        }
        saveTask = new WorldCacheIOThread();
        ioThread = new Timer("WorldCacheIOThread", true);
        ioThread.scheduleAtFixedRate(saveTask, 30_000, 30_000);
    }

    public static void onServerStop() {
        if (saveTask == null) { //Server startup didn't finish; ObserverLib would NPE here.
            if (ioThread != null) {
                ioThread.cancel();
                ioThread = null;
            }
            return;
        }
        saveTask.flushAndSaveAll();

        saveTask.cancel();
        saveTask = null;
        ioThread.cancel();
        ioThread = null;
    }

    @Override
    public void run() {
        if (skipTick) {
            return;
        }

        inSave = true;
        saveAllNow();
        worldSaveQueue.clear();

        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        awaitingSaveQueue.clear();
        inSave = false;
    }

    private void flushAndSaveAll() {
        skipTick = true;
        for (WorldCacheDomain domain : this.awaitingSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.awaitingSaveQueue.get(domain).entrySet()) {
                this.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        saveAllNow();

        worldSaveQueue.clear();
        awaitingSaveQueue.clear();

        skipTick = false;
        inSave = false;
    }

    static void scheduleSave(WorldCacheDomain domain, ResourceLocation dimTypeName, IWorldRelatedData worldRelatedData) {
        WorldCacheIOThread tr = saveTask;
        if (saveTask == null) { //Server startup didn't finish
            return;
        }
        if (tr.inSave) {
            tr.awaitingSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimTypeName, id -> new ArrayList<>())
                    .add(worldRelatedData);
        } else {
            tr.worldSaveQueue.computeIfAbsent(domain, d -> new HashMap<>())
                    .computeIfAbsent(dimTypeName, id -> new ArrayList<>())
                    .add(worldRelatedData);
        }
    }

    @Nonnull
    static <T extends CachedWorldData> T loadNow(WorldCacheDomain domain, Level world, WorldCacheDomain.SaveKey<T> key) {
        T loaded = loadDataFromFile(domain, world.dimension().location(), key);
        loaded.onLoad(world);
        return loaded;
    }

    private void saveAllNow() {
        for (WorldCacheDomain domain : this.worldSaveQueue.keySet()) {
            for (Map.Entry<ResourceLocation, List<IWorldRelatedData>> entry : this.worldSaveQueue.get(domain).entrySet()) {
                entry.getValue().forEach(data -> saveNow(domain, entry.getKey(), data));
            }
        }
    }

    private void saveNow(WorldCacheDomain domain, ResourceLocation dimTypeName, IWorldRelatedData data) {
        try {
            saveDataToFile(domain.getSaveDirectory(), dimTypeName, data);
        } catch (IOException e) {
            AstralSorcery.LOG.warn("Unable to save WorldData!");
            AstralSorcery.LOG.warn("Affected data: Dim=" + dimTypeName + " key=" + data.getSaveKey().getIdentifier());
            AstralSorcery.LOG.warn("Printing StackTrace details...");
            e.printStackTrace();
        }
        data.markSaved();
    }

    private static void saveDataToFile(File baseDirectory, ResourceLocation dimTypeName, IWorldRelatedData data) throws IOException {
        DirectorySet f = getDirectorySet(baseDirectory, dimTypeName, data.getSaveKey());
        if (!f.getParentDirectory().exists()) {
            f.getParentDirectory().mkdirs();
        }
        data.writeData(f.getActualDirectory(), f.getBackupDirectory());
    }

    @Nonnull
    private static <T extends CachedWorldData> T loadDataFromFile(WorldCacheDomain domain, ResourceLocation dimTypeName, WorldCacheDomain.SaveKey<T> key) {
        DirectorySet f = getDirectorySet(domain.getSaveDirectory(), dimTypeName, key);
        if (!f.getActualDirectory().exists() && !f.getBackupDirectory().exists()) {
            return key.getNewInstance(key);
        }
        AstralSorcery.LOG.info("Load CachedWorldData '" + key.getIdentifier() + "' for world " + dimTypeName);
        boolean errored = false;
        T data = null;
        try {
            if (f.getActualDirectory().exists()) {
                data = attemptLoad(key, f.getActualDirectory());
            }
        } catch (Exception exc) {
            AstralSorcery.LOG.info("Loading worlddata '" + key.getIdentifier() + "' failed for its actual save. Attempting load from backup.");
            errored = true;
        }
        if(data == null) {
            try {
                if (f.getBackupDirectory().exists()) {
                    data = attemptLoad(key, f.getBackupDirectory());
                }
            } catch (Exception exc) {
                AstralSorcery.LOG.info("Loading worlddata '" + key.getIdentifier() + "' failed for its backup save. Creating empty one for current runtime and copying erroneous files to error directory.");
                errored = true;
            }
        }
        if(data == null && errored) {
            DirectorySet errorSet = f.getErrorDirectories();
            try {
                if(f.getActualDirectory().exists()) {
                    Files.copy(f.getActualDirectory(), errorSet.getActualDirectory());
                    FileUtils.deleteDirectory(f.getActualDirectory());
                }
                if(f.getBackupDirectory().exists()) {
                    Files.copy(f.getBackupDirectory(), errorSet.getBackupDirectory());
                    FileUtils.deleteDirectory(f.getBackupDirectory());
                }
            } catch (Exception e) {
                AstralSorcery.LOG.info("Attempting to copy erroneous worlddata '" + key.getIdentifier() + "' to its error files directory failed.");
                e.printStackTrace();
            }
        }
        if (data == null) {
            data = key.getNewInstance(key);
        }
        AstralSorcery.LOG.info("Loading of '" + key.getIdentifier() + "' for world " + dimTypeName + " finished.");
        return data;
    }

    private static <T extends CachedWorldData> T attemptLoad(WorldCacheDomain.SaveKey<T> key, File baseDirectory) throws IOException {
        T data = key.getNewInstance(key);
        data.readData(baseDirectory);
        return data;
    }

    private synchronized static DirectorySet getDirectorySet(File baseDirectory, ResourceLocation dimTypeName, WorldCacheDomain.SaveKey<?> key) {
        File worldDir = new File(baseDirectory, "DIM_" + sanitizeFileName(dimTypeName.toString()));
        if (!worldDir.exists()) {
            worldDir.mkdirs();
        } else {
            ensureFolder(worldDir);
        }
        return new DirectorySet(new File(worldDir, key.getIdentifier()));
    }

    private static File getServerWorldDirectory(File baseDirectory) {
        File pDir = new File(baseDirectory, "worlddata");
        if (!pDir.exists()) {
            pDir.mkdirs();
        }
        ensureFolder(pDir);
        return pDir;
    }

    private static void ensureFolder(File f) {
        if (!f.isDirectory()) {
            AstralSorcery.LOG.warn("dataFile exists, but is a file instead of a folder! Please ensure that this is a folder/delete the file!");
            AstralSorcery.LOG.warn("Encountered illegal state. Crashing to prevent further, harder to resolve errors!");
            throw new IllegalStateException("Affected file: " + f.getAbsolutePath());
        }
    }

    private static String sanitizeFileName(String name) {
        name = name.trim().replace(' ', '_').toLowerCase();
        for (char c0 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            name = name.replace(c0, '_');
        }
        name = name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"); //Anything else that falls through
        return name;
    }

    private static class DirectorySet {

        private final File actualDirectory;
        private final File backupDirectory;

        private DirectorySet(File worldDirectory) {
            this.actualDirectory = worldDirectory;
            this.backupDirectory = new File(worldDirectory.getParent(), worldDirectory.getName() + "-Backup");
        }

        File getParentDirectory() {
            return getActualDirectory().getParentFile();
        }

        File getActualDirectory() {
            return actualDirectory;
        }

        File getBackupDirectory() {
            return backupDirectory;
        }

        DirectorySet getErrorDirectories() {
            File errorDirectory = new File(actualDirectory.getParent(), actualDirectory.getName() + "-Error");
            if (!errorDirectory.exists()) {
                errorDirectory.mkdirs();
            }
            return new DirectorySet(errorDirectory);
        }

    }
}
