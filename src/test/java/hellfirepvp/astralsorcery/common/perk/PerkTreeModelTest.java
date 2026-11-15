package hellfirepvp.astralsorcery.common.perk;

import hellfirepvp.astralsorcery.common.perk.tree.*;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerkTreeModelTest {
    private static final String ROOT = "hellfirepvp.astralsorcery.common.perk.tree.";
    private static final List<String> TYPES = List.of("PerkTreePoint", "PerkTreeGem", "PerkTreeMajor", "PerkTreeConstellation");
    private static final List<String> CLIENT = List.of("hellfirepvp.astralsorcery.client.",
            "net.minecraft.client.", "net.minecraftforge.client.", "com.mojang.blaze3d.", "org.lwjgl.");

    @Test
    void modelClassesLinkAndExposeOnlyDataWithoutAnyClientClassAvailable() throws Exception {
        var denied = new ArrayList<String>();
        var location = PerkTreePoint.class.getProtectionDomain().getCodeSource().getLocation();
        try (var loader = new URLClassLoader(new java.net.URL[]{location}, getClass().getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (CLIENT.stream().anyMatch(name::startsWith)) {
                    denied.add(name);
                    throw new ClassNotFoundException("Client dependency: " + name);
                }
                if (name.startsWith(ROOT)) {
                    Class<?> type = findLoadedClass(name);
                    if (type == null) type = findClass(name);
                    if (resolve) resolveClass(type);
                    return type;
                }
                return super.loadClass(name, resolve);
            }
        }) {
            for (String name : TYPES) {
                Class<?> type = Class.forName(ROOT + name, true, loader);
                assertSame(loader, type.getClassLoader());
                assertEquals(0, type.getInterfaces().length);
                type.getDeclaredFields();
                type.getDeclaredConstructors();
                for (var method : type.getDeclaredMethods()) {
                    method.getGenericReturnType();
                    method.getGenericParameterTypes();
                    assertFalse(List.of("addGroups", "renderAt", "renderPerkAtBatch").contains(method.getName()));
                }
                try (var stream = getClass().getResourceAsStream("/" + (ROOT + name).replace('.', '/') + ".class")) {
                    assertNotNull(stream);
                    String bytes = new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1);
                    for (String client : CLIENT) assertFalse(bytes.contains(client.replace('.', '/')), name + ": " + client);
                }
            }
        }
        assertTrue(denied.isEmpty(), denied.toString());
    }

    @Test
    void sizesOffsetsAndEqualityRemainCompatibleWithPreparedTreeData() {
        Point.Float offset = new Point.Float(2.5F, -3F);
        PerkTreePoint<?> point = new PerkTreePoint<>(null, offset);
        PerkTreePoint<?> major = new PerkTreeMajor<>(null, offset);
        PerkTreePoint<?> gem = new PerkTreeGem<>(null, offset);
        assertSame(offset, point.getOffset());
        assertNull(point.getPerk());
        assertEquals(11, point.getRenderSize());
        assertEquals(15, major.getRenderSize());
        assertEquals(15, gem.getRenderSize());
        assertEquals(point, new PerkTreePoint<>(null, new Point.Float(2.5F, -3F)));
        assertEquals(point.hashCode(), new PerkTreePoint<>(null, new Point.Float(2.5F, -3F)).hashCode());
        assertNotEquals(point, major);
        assertNotEquals(major, gem);
        point.setRenderSize(23);
        assertEquals(23, point.getRenderSize());
        for (int size : new int[]{PerkTreeConstellation.ROOT_SPRITE_SIZE, PerkTreeConstellation.MINOR_SPRITE_SIZE}) {
            var constellation = new PerkTreeConstellation<>(null, offset, null, size);
            assertEquals(size, constellation.getPerkSpriteSize());
            assertEquals(size / 2, constellation.getRenderSize());
            assertNull(constellation.getAssociatedConstellation());
        }
    }
}
