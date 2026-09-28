package hellfirepvp.astralsorcery.common.lib;

import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the Item.Properties rule that decides how ItemsAS may build its wand and tool entries.
 *
 * Item construction itself cannot be tested here: Item's constructor takes an intrusive holder from
 * BuiltInRegistries.ITEM, which needs Bootstrap#bootStrap, and Forge patches Bootstrap to call NetworkHooks#init -
 * that needs the transformed EventBus and so cannot run outside an FML launch. Item.Properties on its own needs
 * neither, so the ordering rule below is checkable, and it is the rule that actually crashed the dedicated server.
 */
public class ItemPropertiesOrderingTest {

    @Test
    void addingAStackLimitAfterADurabilityThrows() {
        //ItemArchitectWand / ItemBlinkWand / ItemExchangeWand / ItemGrappleWand / ItemWand / ItemIlluminationWand /
        //ItemLinkingTool all call properties.stacksTo(1) inside their own constructor. ItemsAS used to hand
        //architect/blink/exchange/grapple a Properties().durability(512) placeholder, so wiring the real classes in
        //with those properties would have thrown here during item registration.
        Item.Properties damageable = new Item.Properties().durability(512);
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> damageable.stacksTo(1));
        assertEquals("Unable to have damage AND stack.", thrown.getMessage());
    }

    @Test
    void aWandsOwnStacksToCallIsSafeOnPlainProperties() {
        //The shape ItemsAS now uses for every one of those items.
        assertDoesNotThrow(() -> new Item.Properties().stacksTo(1));
    }

    @Test
    void durabilityAfterAStackLimitIsAccepted() {
        //INFUSED_GLASS is registered as stacksTo(1).durability(5); only the reverse order throws.
        assertDoesNotThrow(() -> new Item.Properties().stacksTo(1).durability(5));
    }
}
