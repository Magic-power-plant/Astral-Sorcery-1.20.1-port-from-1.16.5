/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResultSpawnEntity
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:05
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe/interaction; this tree has it at the top
//level of common/crafting. Not moved because the package layout is a tree-wide decision.
//1.16.5 synced the type with ByteBufUtils.readRegistryEntry/writeRegistryEntry, which resolved the registry
//through IForgeRegistryEntry; that indirection is gone in 1.20.1, so the explicit
//FriendlyByteBuf#readRegistryIdUnsafe/writeRegistryIdUnsafe pair against ForgeRegistries.ENTITY_TYPES is
//used instead - same numeric-id wire format ByteBufUtils produced.
public class ResultSpawnEntity extends InteractionResult {

    private EntityType<?> entityType;

    ResultSpawnEntity() {
        super(InteractionResultRegistry.ID_SPAWN_ENTITY);
    }

    public static ResultSpawnEntity spawnEntity(EntityType<?> type) {
        //1.16.5: "if (!type.isSummonable()) throw new IllegalArgumentException("EntityType " +
        //type.getRegistryName() + " is not summonable!");" (1.16.5
        //common/crafting/recipe/interaction/ResultSpawnEntity.java:44-46). isSummonable is canSummon in
        //1.20.1 and IForgeRegistryEntry#getRegistryName is gone, so the name comes from the registry.
        if (!type.canSummon()) {
            throw new IllegalArgumentException("EntityType " + ForgeRegistries.ENTITY_TYPES.getKey(type) + " is not summonable!");
        }
        ResultSpawnEntity drop = new ResultSpawnEntity();
        drop.entityType = type;
        return drop;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public void doResult(Level level, Vec3 at) {
        //The null guard is new: 1.16.5 could rely on the type always being set because every construction
        //path (spawnEntity / read) assigns it, but read(JsonObject) here can throw before assigning.
        if (this.entityType == null) {
            return;
        }
        Entity entity = this.entityType.create(level);
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        //1.16.5: Entity#setLocationAndAngles -> Entity#moveTo, World#rand -> Level#random,
        //World#addEntity -> Level#addFreshEntity.
        entity.moveTo(at.x, at.y, at.z, level.random.nextFloat() * 360F, 0F);
        level.addFreshEntity(entity);
    }

    @Override
    public void read(JsonObject json) throws JsonParseException {
        ResourceLocation key = ResourceLocation.tryParse(GsonHelper.getAsString(json, "entityType"));
        EntityType<?> type = key == null ? null : ForgeRegistries.ENTITY_TYPES.getValue(key);
        if (type == null) {
            throw new JsonParseException("Unknown entity type: " + key);
        }
        this.entityType = type;
    }

    @Override
    public void write(JsonObject json) {
        json.addProperty("entityType", ForgeRegistries.ENTITY_TYPES.getKey(this.entityType).toString());
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        this.entityType = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES, this.entityType);
    }
}
