package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.perk.source.ModifierSourceProvider;
import net.minecraftforge.eventbus.api.Event;

import java.util.function.Consumer;

public class ASRegistryEvents {

    public static class ModifierSourceRegister extends Event {

        private final Consumer<ModifierSourceProvider<?>> registrar;

        public ModifierSourceRegister(Consumer<ModifierSourceProvider<?>> registrar) {
            this.registrar = registrar;
        }

        public void registerSource(ModifierSourceProvider<?> sourceProvider) {
            this.registrar.accept(sourceProvider);
        }
    }

}
