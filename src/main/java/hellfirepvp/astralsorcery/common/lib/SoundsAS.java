package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundsAS {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AstralSorcery.MODID);

    public static final CategorizedSoundEvent BLOCK_COLOREDLENS_ATTACH = reg("block_coloredlens_attach", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent CRAFT_ATTUNEMENT = reg("craft_attunement", SoundSource.MASTER);
    public static final CategorizedSoundEvent CRAFT_FINISH = reg("craft_finish", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent ALTAR_CRAFT_START = reg("altar_craft_start", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_FINISH = reg("altar_craft_finish", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_LOOP_T1 = reg("altar_craft_loop_t1", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_LOOP_T2 = reg("altar_craft_loop_t2", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_LOOP_T3 = reg("altar_craft_loop_t3", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_LOOP_T4 = reg("altar_craft_loop_t4", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ALTAR_CRAFT_LOOP_T4_WAITING = reg("altar_craft_loop_t4_waiting", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent ATTUNEMENT_ATLAR_IDLE = reg("attunement_altar_idle_loop", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ATTUNEMENT_ATLAR_PLAYER_ATTUNE = reg("attunement_altar_player_attune", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ATTUNEMENT_ATLAR_ITEM_START = reg("attunement_altar_item_start", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ATTUNEMENT_ATLAR_ITEM_FINISH = reg("attunement_altar_item_finish", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ATTUNEMENT_ATLAR_ITEM_LOOP = reg("attunement_altar_item_loop", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent INFUSER_CRAFT_START = reg("infuser_craft_start", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent INFUSER_CRAFT_LOOP = reg("infuser_craft_loop", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent INFUSER_CRAFT_FINISH = reg("infuser_craft_finish", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent PERK_SEAL = reg("perk_seal", SoundSource.PLAYERS);
    public static final CategorizedSoundEvent PERK_UNSEAL = reg("perk_unseal", SoundSource.PLAYERS);
    public static final CategorizedSoundEvent PERK_UNLOCK = reg("perk_unlock", SoundSource.PLAYERS);
    public static final CategorizedSoundEvent ILLUMINATION_WAND_HIGHLIGHT = reg("illumination_wand_highlight", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ILLUMINATION_WAND_UNHIGHLIGHT = reg("illumination_wand_unhighlight", SoundSource.BLOCKS);
    public static final CategorizedSoundEvent ILLUMINATION_WAND_LIGHT = reg("illumination_wand_light", SoundSource.BLOCKS);

    public static final CategorizedSoundEvent GUI_JOURNAL_CLOSE = reg("gui_journal_close", SoundSource.MASTER);
    public static final CategorizedSoundEvent GUI_JOURNAL_PAGE = reg("gui_journal_page", SoundSource.MASTER);

    private static CategorizedSoundEvent reg(String name, SoundSource category) {
        RegistryObject<SoundEvent> ro = SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(AstralSorcery.key(name)));
        return new CategorizedSoundEvent(ro, category);
    }
}
