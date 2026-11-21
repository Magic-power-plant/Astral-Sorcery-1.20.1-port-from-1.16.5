package hellfirepvp.astralsorcery.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublishedExtensionContractsTest {

    @Test void legacyItemCapabilitiesRemainApiCapabilities() {
        assertTrue(hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem.class
                .isAssignableFrom(hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem.class));
        assertTrue(hellfirepvp.astralsorcery.api.crystal.CrystalAttributeGenItem.class
                .isAssignableFrom(hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem.class));
        assertTrue(hellfirepvp.astralsorcery.api.constellation.ConstellationItem.class
                .isAssignableFrom(hellfirepvp.astralsorcery.common.constellation.ConstellationItem.class));
    }

    @Test void extensionPointExposesLookupKeysAndItsKey() {
        ResourceLocation id = new ResourceLocation("example", "entry");
        ExtensionPoint<String> point = new ExtensionPoint<>() {
            @Override public String get(ResourceLocation lookup) { return id.equals(lookup) ? "value" : null; }
            @Override public Set<ResourceLocation> keys() { return Set.of(id); }
            @Override public ResourceLocation getKey() { return id; }
        };

        assertEquals("value", point.get(id));
        assertEquals(Set.of(id), point.keys());
        assertEquals(id, point.getKey());
    }

    @Test void optionalClientHooksDefaultToNoOp() {
        assertDoesNotThrow(() -> new IConstellationEffect() {
            @Override public boolean playEffect(net.minecraft.world.level.Level world,
                                                net.minecraft.core.BlockPos pos,
                                                hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties properties,
                                                hellfirepvp.astralsorcery.common.constellation.IMinorConstellation trait) {
                return false;
            }
        }.playClientEffect(null, null, null, 1.0F, false));
        assertDoesNotThrow(() -> new IMantleEffect() {}.tickClient((Player) null));
        assertDoesNotThrow(() -> new IStarlightNetworkMember() {
            @Override public void onNetworkJoin(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {}
            @Override public void onNetworkLeave(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {}
            @Override public void onNetworkDirty(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {}
        }.onNetworkJoin(null, null));
    }
}
