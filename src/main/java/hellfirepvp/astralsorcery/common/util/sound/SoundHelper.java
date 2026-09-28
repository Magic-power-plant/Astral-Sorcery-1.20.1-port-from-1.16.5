/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.sound;

import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.FadeSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Predicate;

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

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(SoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopClient(sound, SoundSource.MASTER, pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static PositionedLoopSound playSoundLoopClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        PositionedLoopSound posSound = new PositionedLoopSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopFadeInClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(SoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        return playSoundLoopFadeInClient(sound, SoundSource.MASTER, pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeLoopSound playSoundLoopFadeInClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<PositionedLoopSound> func) {
        FadeLoopSound posSound = new FadeLoopSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(CategorizedSoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<FadeSound> func) {
        return playSoundFadeInClient(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(SoundEvent sound, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<FadeSound> func) {
        return playSoundFadeInClient(sound, SoundSource.MASTER, pos, volume, pitch, isGlobal, func);
    }

    @OnlyIn(Dist.CLIENT)
    public static FadeSound playSoundFadeInClient(SoundEvent sound, SoundSource cat, Vector3 pos, float volume, float pitch, boolean isGlobal, Predicate<FadeSound> func) {
        FadeSound posSound = new FadeSound(sound, cat, volume, pitch, pos, isGlobal);
        posSound.setRefreshFunction(func);
        Minecraft.getInstance().getSoundManager().play(posSound);
        return posSound;
    }

    @OnlyIn(Dist.CLIENT)
    public static float getSoundVolume(SoundSource cat) {
        return Minecraft.getInstance().options.getSoundSourceVolume(cat);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClient(SoundEvent sound, float volume, float pitch) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.playSound(sound, volume, pitch);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClient(CategorizedSoundEvent sound, float volume, float pitch) {
        playSoundClient(sound.getSoundEvent(), volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(CategorizedSoundEvent sound, BlockPos pos, float volume, float pitch) {
        playSoundClientWorld(sound.getSoundEvent(), sound.getCategory(), pos, volume, pitch);
    }

    @OnlyIn(Dist.CLIENT)
    public static void playSoundClientWorld(SoundEvent sound, SoundSource cat, BlockPos pos, float volume, float pitch) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, pos.getX(), pos.getY(), pos.getZ(), sound, cat, volume, pitch);
        }
    }

}
