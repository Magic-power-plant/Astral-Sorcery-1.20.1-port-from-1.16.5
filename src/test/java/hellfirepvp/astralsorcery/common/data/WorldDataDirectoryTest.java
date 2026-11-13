package hellfirepvp.astralsorcery.common.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorldDataDirectoryTest {

    @TempDir
    Path directory;

    @Test
    void singleplayerSavesKeepTheSamePlayersProgressSeparate() throws IOException {
        Path firstSave = directory.resolve("saves/World A");
        Path secondSave = directory.resolve("saves/World B");
        Path firstData = WorldDataDirectory.getOrCreate(firstSave).toPath();
        Path secondData = WorldDataDirectory.getOrCreate(secondSave).toPath();
        String playerFile = UUID.randomUUID() + ".astral";
        Path firstPlayer = Files.createDirectories(firstData.resolve("playerdata")).resolve(playerFile);
        Files.writeString(firstPlayer, "first world's progression");

        assertEquals(firstSave.resolve("astralsorcery"), firstData);
        assertEquals(secondSave.resolve("astralsorcery"), secondData);
        assertFalse(Files.exists(secondData.resolve("playerdata").resolve(playerFile)));
        assertFalse(Files.exists(directory.resolve("astralsorcery")));

        Path secondPlayer = Files.createDirectories(secondData.resolve("playerdata")).resolve(playerFile);
        Files.writeString(secondPlayer, "second world's progression");
        assertEquals("first world's progression", Files.readString(firstPlayer));
        assertEquals(firstData, WorldDataDirectory.getOrCreate(firstSave).toPath());
    }

    @Test
    void dedicatedServerUsesItsActualSaveNameAndKeepsExistingFiles() throws IOException {
        Path save = directory.resolve("custom-level-name");
        Path data = Files.createDirectories(save.resolve("astralsorcery"));
        Path players = Files.createDirectories(data.resolve("playerdata"));
        Path progress = Files.write(players.resolve("player.astral"), new byte[]{1, 2, 3});
        Path backup = Files.write(players.resolve("player.astralback"), new byte[]{4, 5});
        Path gateway = Files.write(data.resolve("gateway_filter.dat"), new byte[]{6});
        Path worldCache = Files.createDirectories(data.resolve("DIM_minecraft_overworld"));

        assertEquals(data, WorldDataDirectory.getOrCreate(save).toPath());
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(progress));
        assertArrayEquals(new byte[]{4, 5}, Files.readAllBytes(backup));
        assertArrayEquals(new byte[]{6}, Files.readAllBytes(gateway));
        assertTrue(Files.isDirectory(worldCache));
        assertFalse(Files.exists(directory.resolve("world")));
    }

    @Test
    void sharedLegacyDataIsNeitherImportedNorRemoved() throws IOException {
        Path legacy = Files.createDirectories(directory.resolve("astralsorcery/playerdata"));
        Path progress = Files.write(legacy.resolve("player.astral"), new byte[]{1, 2, 3});
        Path backup = Files.write(legacy.resolve("player.astralback"), new byte[]{4, 5});
        Path gateway = Files.write(legacy.getParent().resolve("gateway_filter.dat"), new byte[]{6});

        Path data = WorldDataDirectory.getOrCreate(directory.resolve("saves/New World")).toPath();

        assertFalse(Files.exists(data.resolve("playerdata")));
        assertFalse(Files.exists(data.resolve("gateway_filter.dat")));
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(progress));
        assertArrayEquals(new byte[]{4, 5}, Files.readAllBytes(backup));
        assertArrayEquals(new byte[]{6}, Files.readAllBytes(gateway));
    }

    @Test
    void pathsWithSpacesAndUnicodeAreSupported() {
        Path save = directory.resolve("saves/星辉 世界");
        Path data = WorldDataDirectory.getOrCreate(save).toPath();
        assertEquals(save.resolve("astralsorcery"), data);
        assertTrue(Files.isDirectory(data));
    }

    @Test
    void aFileBlockingTheDataDirectoryFailsWithoutChangingIt() throws IOException {
        Path save = Files.createDirectories(directory.resolve("blocked-world"));
        Path obstruction = Files.write(save.resolve("astralsorcery"), new byte[]{7, 8});

        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> WorldDataDirectory.getOrCreate(save));

        assertInstanceOf(IOException.class, failure.getCause());
        assertTrue(failure.getMessage().contains(obstruction.toString()));
        assertArrayEquals(new byte[]{7, 8}, Files.readAllBytes(obstruction));
    }

    @Test
    void missingWorldCannotFallBackToTheWorkingDirectory() {
        assertThrows(NullPointerException.class, () -> WorldDataDirectory.getOrCreate(null));
    }
}
