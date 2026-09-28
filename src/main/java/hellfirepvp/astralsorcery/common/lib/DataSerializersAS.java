/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.util.data.ASDataSerializers;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.fluids.FluidStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataSerializersAS
 * Created by HellFirePvP
 * Date: 06.07.2019 / 19:12
 */
//1.16.5 net.minecraft.network.datasync.IDataSerializer -> 1.20.1 net.minecraft.network.syncher.EntityDataSerializer
//TODO_PORT[AS-LIB-DS-01]: 1.16.5 leaves these three fields null until
// common/registry/RegistryDataSerializers#registerSerializers wraps each ASDataSerializers instance in a
// DataSerializerEntry and hands it to the registry primer, assigning the created serializer back here
// (1.16.5 common/registry/RegistryDataSerializers.java:31-35). In 1.20.1 DataSerializerEntry is gone and
// serializers are registered directly through EntityDataSerializers#registerSerializer, which
// common/util/data/ASDataSerializers#register already does; common/registry/** is outside this slice, so
// nothing assigns these fields there yet. They are therefore initialized to the very same instances the
// 1.16.5 registry would have stored, and left non-final so a ported RegistryDataSerializers can still
// overwrite them.
public class DataSerializersAS {

    private DataSerializersAS() {}

    public static EntityDataSerializer<Long> LONG = ASDataSerializers.LONG;
    public static EntityDataSerializer<Vector3> VECTOR = ASDataSerializers.VECTOR;
    public static EntityDataSerializer<FluidStack> FLUID = ASDataSerializers.FLUID;

}
