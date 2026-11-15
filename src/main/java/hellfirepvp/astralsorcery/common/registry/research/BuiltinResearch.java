package hellfirepvp.astralsorcery.common.registry.research;

import hellfirepvp.astralsorcery.common.registry.AbstractRegistryInitializer;

/** Initializes the journal once, in progression order, after item and structure registration. */
public final class BuiltinResearch extends AbstractRegistryInitializer {
    @Override
    protected void registerEntries() {
        DiscoveryResearch.registerEntries();
        CraftingResearch.registerEntries();
        AttunementResearch.registerEntries();
        ConstellationResearch.registerEntries();
        RadianceResearch.registerEntries();
    }
}
