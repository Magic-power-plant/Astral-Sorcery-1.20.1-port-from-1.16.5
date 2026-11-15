/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.screen;

import hellfirepvp.astralsorcery.common.GuiType;
import hellfirepvp.astralsorcery.client.screen.journal.ScreenJournalProgression;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.blockentity.refraction.RefractionTableBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.observation.TelescopeBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Client screen factories for the stable GuiType protocol identifiers. */
@OnlyIn(Dist.CLIENT)
public final class GuiScreens {
    private GuiScreens() {}

    @Nullable
    public static Screen deserialize(GuiType type, CompoundTag data) {
        //1.16.5: Minecraft#world/#player -> 1.20.1: Minecraft#level/#player
        Level clWorld = Minecraft.getInstance().level;
        Player clPlayer = Minecraft.getInstance().player;
        if (clWorld == null || clPlayer == null) {
            return null;
        }

        BlockPos at;
        try {
            switch (type) {
                case CONSTELLATION_PAPER:
                    //Resolved exactly as 1.16.5 does, so the lookup failure mode stays identical.
                    IConstellation cst = RegistriesAS.CONSTELLATIONS.getValue(ResourceLocation.parse(data.getString("cst")));
                    //AS-GUI-01 (resolved): the BLOCKED note here claimed ScreenConstellationPaper was "an empty
                    //stub without that constructor". Stale - it is 166 lines and declares
                    //ScreenConstellationPaper(IConstellation) at :51.
                    return new ScreenConstellationPaper(cst);
                case TOME:
                    return ScreenJournalProgression.getOpenJournalInstance();
                case REFRACTION_TABLE:
                    at = NBTHelper.readBlockPosFromNBT(data);
                    RefractionTableBlockEntity refractionTable = MiscUtils.getBlockEntityAt(clWorld, at, RefractionTableBlockEntity.class, true);
                    if (refractionTable != null) {
                        //AS-GUI-02 (resolved): ScreenRefractionTable is 452 lines and declares
                        //ScreenRefractionTable(TileRefractionTable) at :69. Its GUI is the only construction site
                        //of PktEngraveGlass (:384), so glass engraving was entirely unreachable while this
                        //returned null.
                        return new ScreenRefractionTable(refractionTable);
                    }
                    return null;
                case TELESCOPE:
                    at = NBTHelper.readBlockPosFromNBT(data);
                    TelescopeBlockEntity telescope = MiscUtils.getBlockEntityAt(clWorld, at, TelescopeBlockEntity.class, true);
                    if (telescope != null) {
                        //AS-GUI-03 (resolved): ScreenTelescope is 280 lines and declares
                        //ScreenTelescope(TileTelescope) at :55.
                        return new ScreenTelescope(telescope);
                    }
                    return null;
                case HAND_TELESCOPE:
                    return new ScreenHandTelescope();
                default:
                    throw new IllegalArgumentException("Unknown GuiType: " + type.name());
            }
        } catch (Exception exc) {
            throw new IllegalArgumentException("Invalid Arguments for GuiType: " + type.name(), exc);
        }
    }
}
