package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockEntityClassNamesTest {
    private static final String OLD = "hellfirepvp.astralsorcery.common.blockentity.TreeBeaconBlockEntity";
    private static final String CURRENT = "hellfirepvp.astralsorcery.common.blockentity.treebeacon.TreeBeaconBlockEntity";
    @Test void exactNamesAndPrefixesContinueMatchingAfterPackageMove() {
        assertEquals(CURRENT, BlockEntityClassNames.resolve(OLD));
        assertTrue(BlockEntityClassNames.matches(CURRENT, OLD));
        assertTrue(BlockEntityClassNames.matches(CURRENT, "hellfirepvp.astralsorcery.common.blockentity.Tree"));
        assertTrue(BlockEntityClassNames.matches(CURRENT, "HELLFIREPVP.ASTRALSORCERY.COMMON.BLOCKENTITY.TREE"));
        assertTrue(BlockEntityClassNames.matches(CURRENT, "hellfirepvp.astralsorcery.common.blockentity.treebeacon"));
        assertFalse(BlockEntityClassNames.matches(CURRENT, "hellfirepvp.astralsorcery.common.blockentity.Lens"));
        assertFalse(BlockEntityClassNames.matches(CURRENT, ""));
    }
    @Test void thirdPartyFiltersAndSerializedConfigurationStayUnchanged() {
        assertEquals("example.SomeBlockEntity", BlockEntityClassNames.resolve("example.SomeBlockEntity"));
        assertTrue(BlockEntityClassNames.matches("example.SomeBlockEntity", "example"));
        assertEquals(OLD, new BlockEntityAccelerationBlacklistEntry(OLD).serialize());
    }
}
