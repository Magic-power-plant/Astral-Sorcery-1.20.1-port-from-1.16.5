/**
 * 公共内容、配方、世界生成及领域对象的注册装配和启动生命周期。
 *
 * <p>RegistryBootstrap 统一绑定 DeferredRegister 和自定义注册事件；RegistriesAS 保存唯一句柄。
 * RegistryView 定义查询能力，AbstractBootstrapRegistry 封装构造至发布的状态，
 * ForgeRegistryHandle 适配 Forge。配置重载与扩展事件窗口不共用此一次性生命周期。
 * 渲染绑定仍只能从客户端入口调用。</p>
 *
 * @see hellfirepvp.astralsorcery.common.registry.RegistryBootstrap
 * @see hellfirepvp.astralsorcery.common.registry.RegistryView
 * @see "仓库 docs/registries.md"
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.registry;
