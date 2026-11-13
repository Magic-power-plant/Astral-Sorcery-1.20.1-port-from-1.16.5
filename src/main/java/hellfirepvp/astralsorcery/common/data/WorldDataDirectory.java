package hellfirepvp.astralsorcery.common.data;

import hellfirepvp.astralsorcery.AstralSorcery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Resolves mod-owned files under a world save, independently of the process working directory. */
public final class WorldDataDirectory {

    private WorldDataDirectory() {}

    /**
     * The caller must supply MinecraftServer#getWorldPath(LevelResource.ROOT), never getServerDirectory().
     * Existing files, including the per-dimension world caches, remain in place. Legacy shared data is
     * deliberately not imported: it carries no world identity, even when player UUIDs match.
     */
    public static File getOrCreate(Path worldDirectory) {
        Path directory = Objects.requireNonNull(worldDirectory, "An active world save is required")
                .toAbsolutePath().normalize().resolve(AstralSorcery.MODID);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create Astral Sorcery world data directory: " + directory, e);
        }
        return directory.toFile();
    }
}
