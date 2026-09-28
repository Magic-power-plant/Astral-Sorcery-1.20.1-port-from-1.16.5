/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import com.mojang.blaze3d.platform.InputConstants;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.input.KeyBindingWrapper;
import hellfirepvp.astralsorcery.client.input.KeyDisablePerkAbilities;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static hellfirepvp.astralsorcery.client.lib.KeyBindingsAS.DISABLE_PERK_ABILITIES;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryKeyBindings
 * Created by HellFirePvP
 * Date: 13.05.2020 / 18:43
 *
 * 1.16.5 -&gt; 1.20.1: KeyBinding -&gt; {@link KeyMapping}, InputMappings -&gt; {@link InputConstants}
 * ({@code getInputByCode(int, int)} -&gt; {@link InputConstants#getKey(int, int)}),
 * {@code InputEvent.KeyInputEvent} -&gt; {@link InputEvent.Key} and {@code KeyBinding#isKeyDown()} -&gt;
 * {@code KeyMapping#isDown()}. Forge deleted {@code ClientRegistry.registerKeyBinding(..)}: key mappings are now
 * registered through the mod-bus {@link RegisterKeyMappingsEvent}, so {@code init()} takes that event instead of
 * being called from {@code FMLClientSetupEvent} (see {@code client/ClientProxy}, TODO_PORT[AS-CLI-15]).
 */
public class RegistryKeyBindings {

    private static final Set<KeyBindingWrapper> watchedKeyBindings = new HashSet<>();
    private static final Set<KeyBindingWrapper> bindingsPressed = new HashSet<>();

    public static void init(RegisterKeyMappingsEvent event) {
        DISABLE_PERK_ABILITIES = register(event, "disable_perk_abilities", GLFW.GLFW_KEY_V, KeyDisablePerkAbilities::new);

        MinecraftForge.EVENT_BUS.addListener(RegistryKeyBindings::onKeyInput);
    }

    //1.16.5 client/registry/RegistryKeyBindings.java:47-49. Kept for parity with the reference even though
    //DISABLE_PERK_ABILITIES is currently the only binding and uses the three-argument overload.
    private static KeyBindingWrapper register(RegisterKeyMappingsEvent event, String name, int glfwKey) {
        return register(event, name, glfwKey, keyBinding -> new KeyBindingWrapper(keyBinding) {});
    }

    private static KeyBindingWrapper register(RegisterKeyMappingsEvent event, String name, int glfwKey, Function<KeyMapping, KeyBindingWrapper> wrapperCreator) {
        KeyMapping keyBinding = new KeyMapping(String.format("key.%s.%s", AstralSorcery.MODID, name),
                KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, glfwKey, AstralSorcery.NAME);
        //1.16.5: ClientRegistry.registerKeyBinding(keyBinding) - removed; RegisterKeyMappingsEvent#register replaces it,
        //which is why the event has to be threaded through this helper.
        event.register(keyBinding);
        KeyBindingWrapper wrapper = wrapperCreator.apply(keyBinding);
        watchedKeyBindings.add(wrapper);
        return wrapper;
    }

    private static void onKeyInput(InputEvent.Key event) {
        InputConstants.Key input = InputConstants.getKey(event.getKey(), event.getScanCode());

        //1.16.5 client/registry/RegistryKeyBindings.java:62-75, verbatim apart from
        //KeyBinding#isKeyDown() -> KeyMapping#isDown().
        KeyBindingWrapper eventKey = MiscUtils.iterativeSearch(watchedKeyBindings,
                keyBinding -> keyBinding.getKeyBinding().getKey().equals(input));
        if (eventKey != null) {
            boolean isPressed = eventKey.getKeyBinding().isDown();
            boolean wasPressed = bindingsPressed.contains(eventKey);
            if (isPressed != wasPressed) {
                if (isPressed) {
                    bindingsPressed.add(eventKey);
                    eventKey.onKeyDown();
                } else {
                    bindingsPressed.remove(eventKey);
                    eventKey.onKeyUp();
                }
            }
        }
    }
}
