package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.entity.*;
import hellfirepvp.astralsorcery.common.entity.item.*;
import hellfirepvp.astralsorcery.common.entity.technical.EntityGrapplingHook;
import hellfirepvp.astralsorcery.common.entity.technical.EntityObservatoryHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypesAS {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<EntityType<EntityNocturnalSpark>> NOCTURNAL_SPARK = ENTITIES.register("nocturnal_spark",
            () -> EntityType.Builder.<EntityNocturnalSpark>of(EntityNocturnalSpark::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("nocturnal_spark"));

    public static final RegistryObject<EntityType<EntityIlluminationSpark>> ILLUMINATION_SPARK = ENTITIES.register("illumination_spark",
            () -> EntityType.Builder.<EntityIlluminationSpark>of(EntityIlluminationSpark::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("illumination_spark"));

    public static final RegistryObject<EntityType<EntityFlare>> FLARE = ENTITIES.register("flare",
            () -> EntityType.Builder.<EntityFlare>of(EntityFlare::new, MobCategory.AMBIENT)
                    .sized(0.6F, 0.6F)
                    .build("flare"));

    public static final RegistryObject<EntityType<EntitySpectralTool>> SPECTRAL_TOOL = ENTITIES.register("spectral_tool",
            () -> EntityType.Builder.<EntitySpectralTool>of(EntitySpectralTool::new, MobCategory.AMBIENT)
                    .sized(1.0F, 1.0F)
                    .build("spectral_tool"));

    //MISC in both references (1.16.5 RegistryEntities.java registers every entity as EntityClassification.MISC,
    //and 1.21.1 EntitiesAS.java:149 uses MobCategory.MISC). EntityShootingStar is a ThrowableProjectile, not a
    //LivingEntity, so it can never have an AttributeSupplier: as AMBIENT it made Bootstrap's validation log
    //"Entity astralsorcery:shooting_star has no attributes" and counted against the ambient mob cap.
    public static final RegistryObject<EntityType<EntityShootingStar>> SHOOTING_STAR = ENTITIES.register("shooting_star",
            () -> EntityType.Builder.<EntityShootingStar>of(EntityShootingStar::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("shooting_star"));

    public static final RegistryObject<EntityType<EntityItemHighlighted>> ITEM_HIGHLIGHT = ENTITIES.register("item_highlight",
            () -> EntityType.Builder.<EntityItemHighlighted>of(EntityItemHighlighted::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("item_highlight"));

    public static final RegistryObject<EntityType<EntityItemExplosionResistant>> ITEM_EXPLOSION_RESISTANT = ENTITIES.register("item_explosion_resistant",
            () -> EntityType.Builder.<EntityItemExplosionResistant>of(EntityItemExplosionResistant::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("item_explosion_resistant"));

    public static final RegistryObject<EntityType<EntityCrystal>> ITEM_CRYSTAL = ENTITIES.register("item_crystal",
            () -> EntityType.Builder.<EntityCrystal>of(EntityCrystal::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("item_crystal"));

    public static final RegistryObject<EntityType<EntityDazzlingGem>> ITEM_DAZZLING_GEM = ENTITIES.register("item_dazzling_gem",
            () -> EntityType.Builder.<EntityDazzlingGem>of(EntityDazzlingGem::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("item_dazzling_gem"));

    public static final RegistryObject<EntityType<EntityStarmetal>> ITEM_STARMETAL_INGOT = ENTITIES.register("item_starmetal_ingot",
            () -> EntityType.Builder.<EntityStarmetal>of(EntityStarmetal::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("item_starmetal_ingot"));

    public static final RegistryObject<EntityType<EntityObservatoryHelper>> OBSERVATORY_HELPER = ENTITIES.register("observatory_helper",
            () -> EntityType.Builder.<EntityObservatoryHelper>of(EntityObservatoryHelper::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .build("observatory_helper"));

    public static final RegistryObject<EntityType<EntityGrapplingHook>> GRAPPLING_HOOK = ENTITIES.register("grappling_hook",
            () -> EntityType.Builder.<EntityGrapplingHook>of(EntityGrapplingHook::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("grappling_hook"));

    private EntityTypesAS() {}
}
