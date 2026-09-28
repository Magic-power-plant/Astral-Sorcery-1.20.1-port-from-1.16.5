/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.entity.InteractableEntity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinServerPlayNetHandler
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.16.5: ServerPlayNetHandler#processUseEntity(CUseEntityPacket) contained the whole interaction
 *         switch inline, including the "invalid entity attacked" disconnect.
 * 1.20.1: ServerGamePacketListenerImpl#handleInteract only dispatches into the anonymous
 *         ServerboundInteractPacket.Handler implementation ServerGamePacketListenerImpl$1; the
 *         disconnect now lives in that inner class' onAttack() (verified with javap: the
 *         invokevirtual of ServerGamePacketListenerImpl#disconnect sits at offset 98 of
 *         ServerGamePacketListenerImpl$1#onAttack). The mixin therefore targets the inner class.
 *
 * Field naming across the two runtime name sets (verified against
 * forge-1.20.1-47.4.20_mapped_official_1.20.1.jar for dev and forge-1.20.1-47.4.20-srg.jar for
 * production):
 *  - val$entity      stays "val$entity" in both (javac synthetics that MCP never renamed),
 *  - val$serverlevel stays "val$serverlevel" in both (unused here),
 *  - this$0          is "this$0" in dev but "f_143671_" in production, hence the alias.
 * Both shadows use remap = false so nothing depends on the annotation processor being able to
 * resolve synthetic members of an anonymous class.
 */
@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class MixinServerPlayNetHandler {

    @Shadow(remap = false)
    @Final
    private Entity val$entity;

    @Shadow(remap = false, aliases = {"f_143671_"})
    @Final
    private ServerGamePacketListenerImpl this$0;

    //Due to compatibility, this will not be used. see reach_set_server_entity_interact.js
    /*
     * 1.16.5 carried the following, disabled, constant override on the outer class:
     *
     * @ModifyConstant(method = "processUseEntity", constant = @Constant(doubleValue = 36.0, ordinal = 1), require = 1)
     * public double overrideEntityInteractDistanceLimit(double distance) {
     *     ServerPlayNetHandler playNetHandler = (ServerPlayNetHandler)(Object) this;
     *     PlayerEntity player = playNetHandler.player;
     *
     *     PlayerProgress prog = ResearchHelper.getProgress(player, player.getEntityWorld().isRemote() ? LogicalSide.CLIENT : LogicalSide.SERVER);
     *     if (prog.isValid() && prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyEntityReach)) {
     *         return Double.MAX_VALUE;
     *     }
     *     return distance;
     * }
     *
     * In 1.20.1 the squared reach check moved to ServerGamePacketListenerImpl#handleInteract
     * (MAX_INTERACTION_DISTANCE = 36.0D) on the outer class. It stays disabled here for the same
     * compatibility reason it was disabled in 1.16.5.
     */

    @Inject(
            method = "onAttack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"),
            cancellable = true
    )
    public void allowInteractableEntity(CallbackInfo ci) {
        Entity interacted = this.val$entity;
        if (interacted instanceof InteractableEntity) {
            this.this$0.player.attack(interacted);
            ci.cancel();
        }
    }

}
