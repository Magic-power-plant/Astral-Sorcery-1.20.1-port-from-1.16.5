/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.sound;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.function.Predicate;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;

public final class ClientSoundHelper {
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
