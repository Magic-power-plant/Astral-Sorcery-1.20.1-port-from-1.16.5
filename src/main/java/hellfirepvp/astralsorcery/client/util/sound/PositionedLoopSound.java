/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.sound;

import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PositionedLoopSound
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:59
 */
//1.20.1: 1.16.5 extended SimpleSound implements ITickableSound, ISound.
//SimpleSound (now SimpleSoundInstance) is final-ish/constructor incompatible here, so extend
//AbstractTickableSoundInstance which is SimpleSoundInstance's tickable sibling and covers both interfaces.
public class PositionedLoopSound extends AbstractTickableSoundInstance {

    private Predicate<PositionedLoopSound> func = null;
    private boolean hasStoppedPlaying = false;
    private float volumeMultiplier = 1F;

    public PositionedLoopSound(CategorizedSoundEvent sound, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        this(sound.getSoundEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public PositionedLoopSound(SoundEvent sound, SoundSource category, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        super(sound, category, RandomSource.create());
        //1.16.5 passed these through the SimpleSound constructor:
        //(name, category, volume, pitch, repeat=true, repeatDelay=0, AttenuationType.LINEAR, x, y, z, isGlobal)
        this.volume = volume;
        this.pitch = pitch;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.looping = true;
        this.delay = 0;
        this.attenuation = SoundInstance.Attenuation.LINEAR;
        this.relative = isGlobal;
    }

    public void setRefreshFunction(Predicate<PositionedLoopSound> func) {
        this.func = func;
    }

    @Override
    public boolean isStopped() {
        hasStoppedPlaying = func == null || func.test(this);
        return hasStoppedPlaying;
    }

    public boolean hasStoppedPlaying() {
        return hasStoppedPlaying || !Minecraft.getInstance().getSoundManager().isActive(this);
    }
    public void setVolumeMultiplier(float volumeMultiplier) {
        this.volumeMultiplier = Mth.clamp(volumeMultiplier, 0F, 1F);
    }

    @Override
    public float getVolume() {
        return super.getVolume() * volumeMultiplier;
    }

    @Override
    public void tick() {}
}
