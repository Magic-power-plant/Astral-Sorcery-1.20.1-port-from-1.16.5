package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentNightVision;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentScorchingHeat;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EnchantmentsAS {

    private EnchantmentsAS() {}

    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, AstralSorcery.MODID);

    public static final RegistryObject<EnchantmentNightVision> NIGHT_VISION = ENCHANTMENTS.register("night_vision", EnchantmentNightVision::new);
    public static final RegistryObject<EnchantmentScorchingHeat> SCORCHING_HEAT = ENCHANTMENTS.register("scorching_heat", EnchantmentScorchingHeat::new);

}
