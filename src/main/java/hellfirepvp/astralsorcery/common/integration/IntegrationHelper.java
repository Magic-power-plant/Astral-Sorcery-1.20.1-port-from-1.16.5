package hellfirepvp.astralsorcery.common.integration;

import net.minecraftforge.fml.loading.FMLLoader;

public class IntegrationHelper {

    // TODO_PORT[AS-INT-01]: 实现JEI兼容 - 添加配方查看器支持
    public static boolean isJEILoaded() {
        return isModLoaded("jei");
    }

    // TODO_PORT[AS-INT-01]: 实现REI兼容 - 添加REI配方显示
    public static boolean isREILoaded() {
        return isModLoaded("roughlyenoughitems");
    }

    // TODO_PORT[AS-INT-01]: 实现EMI兼容 - 添加EMI替代REI支持
    public static boolean isEMILoaded() {
        return isModLoaded("emi");
    }

    // TODO_PORT[AS-INT-01]: 实现Curios兼容 - 添加饰品栏位支持
    public static boolean isCuriosLoaded() {
        return isModLoaded("curios");
    }

    // TODO_PORT[AS-INT-01]: 实现Botania兼容 - 添加花粉/花朵交互
    public static boolean isBotaniaLoaded() {
        return isModLoaded("botania");
    }

    // TODO_PORT[AS-INT-01]: 实现Patchouli兼容 - 添加 guidebook 集成
    public static boolean isPatchouliLoaded() {
        return isModLoaded("patchouli");
    }

    // TODO_PORT[AS-INT-01]: 实现Top兼容 - 添加THE ONE Probe支持
    public static boolean isTOPLoaded() {
        return isModLoaded("theoneprobe");
    }

    // TODO_PORT[AS-INT-01]: 实现XaerosWorldMap兼容
    public static boolean isXaerosLoaded() {
        return isModLoaded("xaeroworldmap");
    }

    public static boolean isClient() {
        return FMLLoader.getDist().isClient();
    }

    public static boolean isServer() {
        return !isClient();
    }

    // TODO_PORT[AS-INT-01]: 注册JEI/REI/EMI分类和配方渲染器
    public static void registerRecipeViewers() {
    }

    // TODO_PORT[AS-INT-01]: 注册Curios饰品栏位
    public static void registerCuriosSlots() {
    }

    // TODO_PORT[AS-INT-01]: 注册Botania花朵交互
    public static void registerBotaniaHandlers() {
    }

    // TODO_PORT[AS-INT-01]: 注册Patchouli书本页面
    public static void registerPatchouli() {
    }

    private static boolean isModLoaded(String modId) {
        return net.minecraftforge.fml.ModList.get().isLoaded(modId);
    }
}