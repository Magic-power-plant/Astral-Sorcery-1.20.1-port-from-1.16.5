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

//1.20.1: 1.16.5 extended SimpleSound implements ITickableSound, ISound.
//AbstractTickableSoundInstance is the 1.20.1 equivalent base covering both interfaces.
public class FadeSound extends AbstractTickableSoundInstance {

    private Predicate<FadeSound> func = null;
    private boolean hasStoppedPlaying = false;
    private float volumeMultiplier = 1F;

    private float fadeInTicks = 40;
    private float fadeOutTicks = 1;

    private int tick = 0;
    private int stopTick = 0;

    public FadeSound(CategorizedSoundEvent sound, float volume, float pitch, Vector3 pos, boolean isGlobal) {
        this(sound.getSoundEvent(), sound.getCategory(), volume, pitch, pos, isGlobal);
    }

    public FadeSound(SoundEvent sound, SoundSource category, float volume, float pitch, Vector3 pos, boolean isGlobal) {
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

    public void setRefreshFunction(Predicate<FadeSound> func) {
        this.func = func;
    }

    public <T extends FadeSound> T setFadeInTicks(float fadeInTicks) {
        this.fadeInTicks = fadeInTicks;
        return (T) this;
    }

    public <T extends FadeSound> T setFadeOutTicks(float fadeOutTicks) {
        this.fadeOutTicks = fadeOutTicks;
        return (T) this;
    }

    //1.20.1: ITickableSound#isDonePlaying was renamed to TickableSoundInstance#isStopped
    @Override
    public boolean isStopped() {
        return (this.hasStoppedPlaying = (func == null || func.test(this))) && this.stopTick > this.fadeOutTicks;
    }

    public boolean hasStoppedPlaying() {
        return hasStoppedPlaying || !Minecraft.getInstance().getSoundManager().isActive(this);
    }

    public void setVolumeMultiplier(float volumeMultiplier) {
        this.volumeMultiplier = Mth.clamp(volumeMultiplier, 0F, 1F);
    }

    @Override
    public float getVolume() {
        float mulFadeIn = Mth.clamp(this.tick / this.fadeInTicks, 0F, 1F);
        float mulFadeOut = Mth.clamp(1F - this.stopTick / this.fadeOutTicks, 0F, 1F);
        return mulFadeIn * mulFadeOut * super.getVolume() * volumeMultiplier;
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasStoppedPlaying) {
            this.stopTick++;
        }
    }

    //1.20.1: ISound#canBeSilent was renamed to SoundInstance#canStartSilent
    @Override
    public boolean canStartSilent() {
        return true;
    }
}
