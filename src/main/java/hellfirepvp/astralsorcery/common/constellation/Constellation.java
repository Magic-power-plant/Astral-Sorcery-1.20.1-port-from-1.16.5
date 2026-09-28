/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: Constellation
 * Created by HellFirePvP
 * Date: 01.06.2019 / 15:59
 */
public abstract class Constellation extends BaseConstellation implements IConstellation {

    private static int counter = 0;

    private final List<Supplier<Ingredient>> signatureItems = new LinkedList<>();

    private final String name, simpleName;
    private final Color color;
    private final int id;

    //1.16.5 inherits setRegistryName/getRegistryName from Forge's IForgeRegistryEntry via BaseConstellation.
    //That interface was removed in 1.20.1, so the registry name is held on the constellation itself.
    private final ResourceLocation registryName;

    public Constellation(String name) {
        this(name, ColorsAS.CONSTELLATION_TYPE_MAJOR);
    }

    public Constellation(String name, Color color) {
        this.id = counter++;
        this.simpleName = name;
        //TODO_PORT[AS-CST-02]: 1.16.5 resolves the owning mod here
        //(1.16.5 Constellation.java:53-60):
        //    ModContainer mod = MiscUtils.getCurrentlyActiveMod();
        //    if (mod != null) { setRegistryName(new ResourceLocation(mod.getModId(), name));
        //                       this.name = mod.getModId() + ".constellation." + name; }
        //    else             { setRegistryName(AstralSorcery.key(name));
        //                       this.name = "unknown.constellation." + name; }
        //so that addon mods registering constellations get their own namespace and lang key. This tree's
        //common/util/MiscUtils.java#getCurrentlyActiveMod() is stubbed to always return null (1.20.1 removed
        //ModLoadingContext#getActiveContainer), and taking the reference's null branch verbatim would rename
        //every Astral Sorcery constellation's lang key to "unknown.constellation.<name>". The branch below is
        //therefore what the reference's *mod != null* path evaluates to for Astral Sorcery's own constellations,
        //which is the only path exercised today. Restoring the addon path needs MiscUtils#getCurrentlyActiveMod
        //to actually resolve the active container - reported as an external change.
        this.registryName = AstralSorcery.key(name);
        this.name = AstralSorcery.MODID + ".constellation." + name;
        this.color = color;
    }

    @Override
    public IConstellation addSignatureItem(Supplier<Ingredient> ingredient) {
        this.signatureItems.add(ingredient);
        return this;
    }

    @Override
    public List<Ingredient> getConstellationSignatureItems() {
        return this.signatureItems.stream()
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    public boolean canDiscover(Player player, PlayerProgress progress) {
        return true;
        //return !Mods.GAMESTAGES.isPresent() ||
        //        (player != null && canDiscoverGameStages(player, progress));
    }

    //Guess we can only config one at a time...
    /*
    @Optional.Method(modid = "gamestages")
    final boolean canDiscoverGameStages(PlayerEntity player, PlayerProgress progress) {
        return !Mods.CRAFTTWEAKER.isPresent() || canDiscoverGameStagesCraftTweaker(player, progress);
    }

    @Optional.Method(modid = "crafttweaker")
    private boolean canDiscoverGameStagesCraftTweaker(PlayerEntity player, PlayerProgress progress) {
        if (player == null) {
            return false;
        }
        IStageData data = GameStageHelper.getPlayerData(player);
        if (data == null) {
            return false;
        }
        return GameStageTweaks.canDiscover(data.getStages(), name);
    }
    */

    @Override
    public int getSortingId() {
        return this.id;
    }

    @Override
    public Color getConstellationColor() {
        return color;
    }

    @Override
    public String getTranslationKey() {
        return this.name;
    }

    @Override
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    @Override
    public String toString() {
        return "Constellation={name:" + this.name + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constellation that = (Constellation) o;
        return Objects.equals(getRegistryName(), that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegistryName());
    }

    @Override
    public int compareTo(IConstellation o) {
        return Integer.compare(this.getSortingId(), o.getSortingId());
    }

    public static class Major extends Weak implements IMajorConstellation {

        public Major(String name) {
            super(name);
        }

        public Major(String name, Color color) {
            super(name, color);
        }

        @Override
        public boolean canDiscover(Player player, PlayerProgress progress) {
            return true;
        }

        //@Override
        //public boolean canDiscover(PlayerEntity player, PlayerProgress progress) {
        //    return !Mods.GAMESTAGES.isPresent() ||
        //            (player != null && canDiscoverGameStages(player, progress));
        //}
    }

    public static class Weak extends Constellation implements IWeakConstellation {

        public Weak(String name) {
            super(name);
        }

        public Weak(String name, Color color) {
            super(name, color);
        }

        @Override
        public boolean canDiscover(Player player, PlayerProgress progress) {
            return super.canDiscover(player, progress) &&
                    progress.getTierReached().isThisLaterOrEqual(ProgressionTier.ATTUNEMENT) &&
                    progress.wasOnceAttuned();
        }
    }

    public static abstract class WeakSpecial extends Weak implements IConstellationSpecialShowup {

        public WeakSpecial(String name) {
            super(name);
        }

        public WeakSpecial(String name, Color color) {
            super(name, color);
        }
    }

    public static class Minor extends Constellation implements IMinorConstellation {

        private final List<MoonPhase> phases;

        //Note: 1.16.5 delegates to super(name) here, i.e. an un-coloured Minor gets CONSTELLATION_TYPE_MAJOR,
        //not CONSTELLATION_TYPE_MINOR (1.16.5 Constellation.java:202). Every registered minor constellation
        //passes an explicit colour, so the default is unobservable - kept verbatim rather than "corrected".
        public Minor(String name, MoonPhase... applicablePhases) {
            super(name);
            phases = new ArrayList<>(applicablePhases.length);
            for (MoonPhase ph : applicablePhases) {
                if (ph == null) {
                    throw new IllegalArgumentException("null MoonPhase passed to Minor constellation registration for " + name);
                }
                phases.add(ph);
            }
        }

        public Minor(String name, Color color, MoonPhase... applicablePhases) {
            super(name, color);
            phases = new ArrayList<>(applicablePhases.length);
            for (MoonPhase ph : applicablePhases) {
                if (ph == null) {
                    throw new IllegalArgumentException("null MoonPhase passed to Minor constellation registration for " + name);
                }
                phases.add(ph);
            }
        }

        @Override
        public List<MoonPhase> getShowupMoonPhases(long rSeed) {
            List<MoonPhase> shifted = new ArrayList<>(phases.size());
            for (MoonPhase mp : this.phases) {
                int index = mp.ordinal() + (((int) (rSeed % MoonPhase.values().length)) + MoonPhase.values().length);
                while (index >= MoonPhase.values().length) {
                    index -= MoonPhase.values().length;
                }
                //1.16.5: MathHelper.clamp -> 1.20.1: Mth.clamp
                index = Mth.clamp(index, 0, MoonPhase.values().length - 1);
                MoonPhase offset = MoonPhase.values()[index];
                if (!shifted.contains(offset)) {
                    shifted.add(offset);
                }
            }
            return shifted;
        }

        @Override
        public boolean canDiscover(Player player, PlayerProgress progress) {
            return super.canDiscover(player, progress) &&
                    progress.wasOnceAttuned() &&
                    progress.getTierReached().isThisLaterOrEqual(ProgressionTier.TRAIT_CRAFT);
        }
    }
}
