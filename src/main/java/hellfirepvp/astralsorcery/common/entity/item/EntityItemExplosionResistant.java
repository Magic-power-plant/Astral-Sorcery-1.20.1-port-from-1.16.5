/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.item;

import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityItemExplosionResistant
 * Created by HellFirePvP
 * Date: 18.08.2019 / 11:22
 */
public class EntityItemExplosionResistant extends EntityItemHighlighted {

    //1.16.5 declares the three constructors as EntityType<? extends ItemEntity>; the 1.20.1 tree narrows them
    //to EntityType<? extends EntityItemExplosionResistant> so EntityItemExplosionResistant::new satisfies
    //EntityType.EntityFactory in common/lib/EntityTypesAS.
    public EntityItemExplosionResistant(EntityType<? extends EntityItemExplosionResistant> type, Level world) {
        super(type, world);
    }

    public EntityItemExplosionResistant(EntityType<? extends EntityItemExplosionResistant> type, Level world, double x, double y, double z) {
        super(type, world, x, y, z);
    }

    public EntityItemExplosionResistant(EntityType<? extends EntityItemExplosionResistant> type, Level world, double x, double y, double z, ItemStack stack) {
        super(type, world, x, y, z, stack);
    }

    //1.16.5 EntityType.IFactory -> 1.20.1 EntityType.EntityFactory; EntityTypesAS holds a RegistryObject in
    //1.20.1, hence the additional get().
    public static EntityType.EntityFactory<EntityItemExplosionResistant> factoryExplosionResistant() {
        return (spawnEntity, world) -> new EntityItemExplosionResistant(EntityTypesAS.ITEM_EXPLOSION_RESISTANT.get(), world);
    }

    @Override
    //1.16.5 Entity#attackEntityFrom -> 1.20.1 Entity#hurt; DamageSource#isExplosion() was replaced by the
    //minecraft:is_explosion damage type tag.
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.hurt(source, amount);
    }

    @Override
    //1.16.5 Entity#createSpawnPacket -> 1.20.1 Entity#getAddEntityPacket
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
