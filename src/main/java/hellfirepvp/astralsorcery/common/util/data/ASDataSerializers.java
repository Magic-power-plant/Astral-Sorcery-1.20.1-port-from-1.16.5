package hellfirepvp.astralsorcery.common.util.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraftforge.fluids.FluidStack;

public class ASDataSerializers {

    public static final EntityDataSerializer<Long> LONG = new EntityDataSerializer<Long>() {
        @Override
        public void write(FriendlyByteBuf buf, Long value) {
            buf.writeLongLE(value);
        }

        @Override
        public Long read(FriendlyByteBuf buf) {
            return buf.readLongLE();
        }

        @Override
        public Long copy(Long value) {
            return value;
        }
    };

    public static final EntityDataSerializer<Vector3> VECTOR = new EntityDataSerializer<Vector3>() {
        @Override
        public void write(FriendlyByteBuf buf, Vector3 value) {
            buf.writeDouble(value.getX());
            buf.writeDouble(value.getY());
            buf.writeDouble(value.getZ());
        }

        @Override
        public Vector3 read(FriendlyByteBuf buf) {
            return new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public Vector3 copy(Vector3 value) {
            return value.clone();
        }
    };

    public static final EntityDataSerializer<FluidStack> FLUID = new EntityDataSerializer<FluidStack>() {
        @Override
        public void write(FriendlyByteBuf buf, FluidStack value) {
            ByteBufUtils.writeFluidStack(buf, value);
        }

        @Override
        public FluidStack read(FriendlyByteBuf buf) {
            return ByteBufUtils.readFluidStack(buf);
        }

        @Override
        public FluidStack copy(FluidStack value) {
            return value.copy();
        }
    };

    public static void register() {
        EntityDataSerializers.registerSerializer(LONG);
        EntityDataSerializers.registerSerializer(VECTOR);
        EntityDataSerializers.registerSerializer(FLUID);
    }

}

