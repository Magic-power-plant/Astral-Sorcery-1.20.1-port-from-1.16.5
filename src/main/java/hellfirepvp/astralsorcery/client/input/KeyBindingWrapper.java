/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.input;

import net.minecraft.client.KeyMapping;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyBindingWrapper
 * Created by HellFirePvP
 * Date: 13.05.2020 / 19:21
 *
 * 1.16.5 -&gt; 1.20.1: {@code KeyBinding} -&gt; {@link KeyMapping}. Otherwise unchanged from
 * 1.16.5 client/input/KeyBindingWrapper.java.
 */
public abstract class KeyBindingWrapper {

    private final KeyMapping keyBinding;

    protected KeyBindingWrapper(KeyMapping keyBinding) {
        this.keyBinding = keyBinding;
    }

    public KeyMapping getKeyBinding() {
        return keyBinding;
    }

    public void onKeyDown() {}

    public void onKeyUp() {}
}
