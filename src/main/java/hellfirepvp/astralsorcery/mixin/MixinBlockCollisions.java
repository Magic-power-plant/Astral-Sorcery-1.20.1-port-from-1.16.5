/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.util.collision.CollisionHelper;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinBlockCollisions
 * Created by HellFirePvP
 * Date: 01.01.2022 / 09:52
 *
 * 1.20.1 port of the 1.16.5 class MixinVoxelShapeSpliterator.
 *
 * 1.16.5: net.minecraft.util.math.shapes.VoxelShapeSpliterator implemented
 *         Spliterator&lt;VoxelShape&gt; and pushed one shape per tryAdvance(Consumer) call. The mixin
 *         injected at the HEAD of tryAdvance and, for as long as
 *         CollisionHelper#onCollision kept returning true, fed the custom collision boxes to the
 *         downstream consumer and short-circuited the vanilla scan by returning true.
 * 1.20.1: the class was renamed to net.minecraft.world.level.BlockCollisions and turned into a
 *         Guava AbstractIterator&lt;T&gt;; the per-element hook is computeNext(), which returns the
 *         element instead of handing it to a Consumer (verified with javap: the method either
 *         returns resultProvider.apply(pos, shape) or endOfData()). The Consumer of the 1.16.5
 *         contract is therefore emulated with a single-slot capture field, and "return true" from
 *         tryAdvance becomes "set the callback return value to the produced element".
 *
 * Two 1.20.1-only guards are required because BlockCollisions is now shared by three call sites
 * (verified with javap on net.minecraft.world.level.CollisionGetter):
 *  - getBlockCollisions            -&gt; onlySuffocatingBlocks = false, resultProvider = (pos, shape) -&gt; shape
 *  - collidesWithSuffocatingBlock  -&gt; onlySuffocatingBlocks = true,  resultProvider = (pos, shape) -&gt; shape
 *  - findSupportingBlock           -&gt; onlySuffocatingBlocks = false, resultProvider = (pos, shape) -&gt; pos.immutable()
 * Only the first one is the 1.16.5 VoxelShapeSpliterator use case, so the injector bails out for
 * the suffocation scan and for any resultProvider that does not yield a VoxelShape.
 *
 * The BlockCollisions fields are read directly; META-INF/accesstransformer.cfg carries
 * "public-f net.minecraft.world.level.BlockCollisions *", the 1:1 port of the 1.16.5 entry
 * "public-f net.minecraft.util.math.shapes.VoxelShapeSpliterator *".
 *
 * CollisionHelper#onCollision takes a BlockCollisions&lt;?&gt; (1.16.5: the non-generic
 * VoxelShapeSpliterator): it only reads BlockCollisions#box and #context, neither of which depends
 * on T, so the T of this mixin stays here - the helper must not be handed a VoxelShape.
 */
@Mixin(BlockCollisions.class)
public abstract class MixinBlockCollisions<T> {

    @Unique
    private boolean astralSorceryDidCustomCollision;

    @Unique
    private VoxelShape astralSorceryCustomShape;

    @SuppressWarnings("unchecked")
    @Inject(method = "computeNext", at = @At("HEAD"), cancellable = true)
    public void addCustomCollision(CallbackInfoReturnable<T> cir) {
        if (!this.astralSorceryDidCustomCollision) {
            BlockCollisions<T> iterator = (BlockCollisions<T>) (Object) this;

            //1.20.1 only: the suffocation scan re-uses this class but never iterated custom
            //collision in 1.16.5, so leave it to vanilla entirely.
            if (iterator.onlySuffocatingBlocks) {
                this.astralSorceryDidCustomCollision = true;
                return;
            }

            this.astralSorceryCustomShape = null;
            if (CollisionHelper.onCollision(iterator, (VoxelShape shape) -> this.astralSorceryCustomShape = shape) &&
                    this.astralSorceryCustomShape != null) {
                VoxelShape produced = this.astralSorceryCustomShape;
                this.astralSorceryCustomShape = null;

                T result = iterator.resultProvider.apply(iterator.pos, produced);
                if (result instanceof VoxelShape) {
                    cir.setReturnValue(result);
                    return;
                }
                //1.20.1 only: resultProvider yields something else than a VoxelShape
                //(CollisionGetter#findSupportingBlock maps to a BlockPos) - that call site has no
                //1.16.5 counterpart, so hand it back to vanilla untouched.
            }
            this.astralSorceryDidCustomCollision = true;
        }
    }
}
