/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategorizedSoundEvent
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:58
 */
//1.20.1: SoundEvent is a final-ish record-like class with a private constructor and must be registry-created
//via SoundEvent.createVariableRangeEvent/createFixedRangeEvent. It can therefore no longer be subclassed, so
//this class wraps the registered SoundEvent instead of extending it. Call sites that used to pass a
//CategorizedSoundEvent straight into a SoundEvent parameter must now use getSoundEvent().
//VERIFY_PORT[AS-SND-01]: 1.16.5 CategorizedSoundEvent extended SoundEvent, so every SoundEvent-typed API
//silently picked up the category via `instanceof`. Confirm in-game that all AS sounds still honour their
//SoundSource (i.e. respect the right volume slider) now that the category must be passed explicitly.
public class CategorizedSoundEvent {

    private final RegistryObject<SoundEvent> soundEvent;
    private final SoundSource category;

    public CategorizedSoundEvent(RegistryObject<SoundEvent> soundEvent, SoundSource category) {
        this.soundEvent = soundEvent;
        this.category = category;
    }

    public SoundEvent getSoundEvent() {
        return soundEvent.get();
    }

    /**
     * 1.16.5 equivalent of {@code SoundEvent#getName()}.
     */
    public ResourceLocation getName() {
        return this.getSoundEvent().getLocation();
    }

    public SoundSource getCategory() {
        return category;
    }

}
