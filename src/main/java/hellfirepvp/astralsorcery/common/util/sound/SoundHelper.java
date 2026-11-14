/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.sound;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;


/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SoundHelper
 * Created by HellFirePvP
 * Date: 30.06.2019 / 22:57
 */
//1.20.1: SoundEvent is final with a private constructor, so CategorizedSoundEvent can no longer extend it.
//1.16.5 relied on `sound instanceof CategorizedSoundEvent` inside these helpers to override the passed
//category; that runtime check is replaced by dedicated CategorizedSoundEvent overloads with the same effect.
//TODO_PORT[AS-SND-02]: 1.16.5 SoundHelper had a single SoundEvent-typed parameter per helper. The
//CategorizedSoundEvent overloads below are additive; once every call site has been re-ported, the pairs can
//be collapsed if CategorizedSoundEvent ever gains a SoundEvent-compatible representation again.
public class SoundHelper {

    public static void playSoundAround(SoundEvent sound, Level world, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level world, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, category, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, Level world, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level world, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, category, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(SoundEvent sound, SoundSource category, Level world, double posX, double posY, double posZ, float volume, float pitch) {
        world.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
    }

    //--- CategorizedSoundEvent variants; these mirror the 1.16.5 category-override branch. ---

    public static void playSoundAround(CategorizedSoundEvent sound, Level world, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, SoundSource category, Level world, Vec3i position, float volume, float pitch) {
        playSoundAround(sound, category, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, Level world, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, SoundSource.MASTER, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, SoundSource category, Level world, Vector3 position, float volume, float pitch) {
        playSoundAround(sound, category, world, position.getX(), position.getY(), position.getZ(), volume, pitch);
    }

    public static void playSoundAround(CategorizedSoundEvent sound, SoundSource category, Level world, double posX, double posY, double posZ, float volume, float pitch) {
        //1.16.5: `if (sound instanceof CategorizedSoundEvent) category = ((CategorizedSoundEvent) sound).getCategory();`
        category = sound.getCategory();
        world.playSound(null, posX, posY, posZ, sound.getSoundEvent(), category, volume, pitch);
    }

}
