package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimplePrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionSourceNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.CrystalTransmissionNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the on-disk identifiers of the starlight transmission nodes.
 *
 * <p>{@code LightNetworkBuffer} writes {@code TransmissionProvider#getIdentifier} as the {@code trNodeId} string
 * of every saved node and resolves it back through {@code TransmissionClassRegistry#getProvider}. The identifier
 * is therefore part of the save format, and an unresolvable one does not throw - it returns null and the node is
 * silently dropped, taking the player's whole light network with it.
 *
 * <p>The values below are the ones 1.16.5 produces via {@code NameUtil.fromClass}, i.e.
 * {@code CaseFormat.UPPER_CAMEL -> LOWER_UNDERSCORE} over the node's simple class name. A previous version of
 * {@code TransmissionProvider} used {@code getSimpleName().toLowerCase(Locale.ROOT)} instead, which produced
 * {@code simpletransmissionnode} rather than {@code simple_transmission_node}. That was self-consistent - the
 * same wrong key was written and read - so a fresh world behaved normally and nothing ever threw.
 *
 * <p>Only the five nodes with no Minecraft dependencies in their constructor are covered here; the six receiver
 * nodes (altar, well, ritual pedestal, tree beacon, lens, prism) construct block-entity-typed nodes and cannot be
 * instantiated without a bootstrapped registry, which plain JUnit cannot do in this repo.
 */
class TransmissionProviderIdentifierTest {

    @Test
    void nodeIdentifiersAreSnakeCasedAsIn1165() {
        assertEquals("astralsorcery:simple_transmission_node",
                new SimpleTransmissionNode.Provider().getIdentifier().toString());
        assertEquals("astralsorcery:simple_prism_transmission_node",
                new SimplePrismTransmissionNode.Provider().getIdentifier().toString());
        assertEquals("astralsorcery:simple_transmission_source_node",
                new SimpleTransmissionSourceNode.Provider().getIdentifier().toString());
        assertEquals("astralsorcery:crystal_transmission_node",
                new CrystalTransmissionNode.Provider().getIdentifier().toString());
        assertEquals("astralsorcery:crystal_prism_transmission_node",
                new CrystalPrismTransmissionNode.Provider().getIdentifier().toString());
    }

    @Test
    void theIdentifierIsCachedAndStableAcrossCalls() {
        //getIdentifier() memoises into identifierCache; a per-call recomputation would still be correct but the
        //registry map lookups happen on every node read, so the cache must actually hold.
        TransmissionProvider provider = new SimpleTransmissionNode.Provider();
        assertEquals(provider.getIdentifier(), provider.getIdentifier());
        assertEquals("astralsorcery:simple_transmission_node", provider.getIdentifier().toString());
    }
}
