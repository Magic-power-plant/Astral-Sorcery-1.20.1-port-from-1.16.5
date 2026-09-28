/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryArgumentTypes
 * Created by HellFirePvP
 * Date: 04.12.2020 / 16:21
 */
// Port notes for 1.16.5 common/registry/RegistryArgumentTypes.java (37 lines):
//
// The one registration of init() (1.16.5 ref:31) was missing from the 1.20.1 tree entirely:
// common/cmd/argument/ArgumentTypeConstellation exists and common/cmd/sub/CommandAttune (major()) and
// common/cmd/sub/CommandConstellation (any(), twice) still build command nodes out of it, but nothing ever told
// the game about the type. On 1.16.5 that only cost the client its suggestions; since 1.19 it is fatal -
// Commands#sendCommands serializes the whole tree through ArgumentUtils/ArgumentTypeInfos#byClass, which throws
// on a class that is not in the BY_CLASS map, so /astralsorcery attune and /astralsorcery constellation would
// break command sync for every joining player. Hence the registration is restored here.
//
//   net.minecraft.command.arguments.ArgumentTypes#register(String, Class, IArgumentSerializer)
//                    -> gone in 1.19. Argument types live in the vanilla registry
//                       Registries.COMMAND_ARGUMENT_TYPE ("minecraft:command_argument_type"), so the String id
//                       is a ResourceLocation and the registration happens from Forge's RegisterEvent - which
//                       fires for wrapped vanilla registries too. The 1.16.5 id was
//                       AstralSorcery.key("constellation").toString(), i.e. the same
//                       "astralsorcery:constellation" the ResourceLocation carries now.
//   IArgumentSerializer<T>
//                    -> ArgumentTypeInfo<A, T extends ArgumentTypeInfo.Template<A>>. The Template indirection is
//                       new: an info no longer serializes the ArgumentType itself but a per-instance template,
//                       which is what lets 1.19+ rebuild argument types against a CommandBuildContext.
//   new ArgumentSerializer<>(ArgumentTypeConstellation::any)
//                    -> SingletonArgumentInfo.contextFree(ArgumentTypeConstellation::any). ArgumentSerializer was
//                       the "no data to write, just call the supplier" serializer, and contextFree(Supplier) is
//                       exactly that; contextAware(Function<CommandBuildContext, A>) is the other 1.20.1 option
//                       and is not needed because ArgumentTypeConstellation reads ConstellationRegistry directly.
//   the Class<T> argument
//                    -> still needed, but it now goes into ArgumentTypeInfos#registerByClass, which fills the
//                       BY_CLASS map byClass()/unpack() look in. Note that the class - not the instance - is the
//                       key, so this single registration covers all four factories
//                       (any/weak/major/minor); 1.16.5 behaved the same way, registering only any().
//
// 1.16.5 called init() from CommonProxy#initialize (1.16.5 common/CommonProxy.java:175). The 1.20.1 CommonProxy
// has no such call, so - as in this slice's RegistryDataSerializers - the class subscribes itself to the mod bus
// instead of relying on a change outside it.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryArgumentTypes {

    private RegistryArgumentTypes() {}

    @SubscribeEvent
    public static void registerArgumentTypes(RegisterEvent event) {
        event.register(Registries.COMMAND_ARGUMENT_TYPE, helper ->
                register(helper, AstralSorcery.key("constellation"), ArgumentTypeConstellation.class,
                        SingletonArgumentInfo.contextFree(ArgumentTypeConstellation::any)));
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> void register(
            RegisterEvent.RegisterHelper<ArgumentTypeInfo<?, ?>> helper, ResourceLocation key, Class<A> argumentClazz, I info) {
        helper.register(key, ArgumentTypeInfos.registerByClass(argumentClazz, info));
    }
}
