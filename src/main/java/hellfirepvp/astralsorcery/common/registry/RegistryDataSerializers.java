/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.data.ASDataSerializers;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Locale;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryDataSerializers
 * Created by HellFirePvP
 * Date: 06.07.2019 / 19:13
 */
// Port notes for 1.16.5 common/registry/RegistryDataSerializers.java (44 lines):
//
//   IDataSerializer<V>       -> net.minecraft.network.syncher.EntityDataSerializer<V>
//   DataSerializerEntry      -> removed in 1.19. The wrapper existed only to give the serializer a registry name;
//                               the Forge registry now holds the EntityDataSerializer itself
//                               (ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS).
//   registry primer          -> gone with IForgeRegistryEntry; a custom serializer goes in from RegisterEvent.
//                               This class subscribes itself to the mod bus because common/CommonProxy (outside
//                               this slice) has no RegistryDataSerializers call, exactly as 1.16.5 had none
//                               either - it ran from PrimerEventHandler#registerDataSerializers
//                               (1.16.5 common/registry/internal/PrimerEventHandler.java:229-231).
//   DataSerializersAS.LONG/VECTOR/FLUID (1.16.5 ref:32-34)
//                            -> those three fields are already initialized to these very instances in
//                               common/lib/DataSerializersAS, so the assignment the reference performed is a
//                               no-op here and is left out rather than duplicated.
//
// Why the Forge registry and not EntityDataSerializers#registerSerializer: that vanilla method appends to the
// vanilla CrudeIncrementalIntIdentityHashBiMap and hard-fails past id 256, while Forge keeps modded serializers
// in ENTITY_DATA_SERIALIZERS with an id range that starts at 256 (GameData#getDataSerializersRegistryBuilder)
// and resolves them through ForgeHooks#getSerializerId / #getSerializer. That is the same split 1.16.5 relied
// on, and it keeps the ids off the vanilla range.
//
//EXTERNAL: common/util/data/ASDataSerializers#register() (the vanilla-map route) has no caller anywhere in the
// 1.20.1 tree, which means the three serializers were unregistered until this class was added - and
// SynchedEntityData#defineId throws "Unregistered serializer" for an unknown one, so
// common/entity/EntityShootingStar (ASDataSerializers.LONG) could not even be class-loaded. With the
// registration below in place that method should be dropped rather than wired up, so the serializers do not end
// up in both id spaces; common/util is outside this slice and it is reported as an external change.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryDataSerializers {

    private RegistryDataSerializers() {}

    @SubscribeEvent
    public static void registerSerializers(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, helper -> {
            register(helper, ASDataSerializers.LONG, "long");
            register(helper, ASDataSerializers.VECTOR, "vector");
            register(helper, ASDataSerializers.FLUID, "fluid");
        });
    }

    private static <V, T extends EntityDataSerializer<V>> T register(RegisterEvent.RegisterHelper<EntityDataSerializer<?>> helper,
                                                                     T dataSerializer, String name) {
        helper.register(AstralSorcery.key(name.toLowerCase(Locale.ROOT)), dataSerializer);
        return dataSerializer;
    }
}
