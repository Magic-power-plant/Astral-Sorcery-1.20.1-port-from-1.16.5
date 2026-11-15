/**
 * 公共内容、配方、世界生成及领域对象的注册装配和启动生命周期。
 *
 * <p>RegistryBootstrap 统一绑定 DeferredRegister 和自定义注册事件；RegistriesAS 保存唯一句柄。
 * RegistryView 定义查询能力，AbstractBootstrapRegistry 封装构造至发布的状态，
 * ForgeRegistryHandle 适配 Forge。AbstractRegistryInitializer 管理内建星座和研究装配的一次性状态。
 * constellation / research 子包承载具体声明，静态 Registry 入口只调度阶段。配置重载与扩展事件窗口不共用此一次性生命周期。
 * 方块实体类型由 common.lib.BlockEntityTypesAS 声明，渲染器由 client.registry.RegistryBlockEntityTypes 绑定。</p>
 *
 * @see hellfirepvp.astralsorcery.common.registry.RegistryBootstrap
 * @see hellfirepvp.astralsorcery.common.registry.RegistryView
 * @see "仓库 docs/registries.md"
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.registry;
