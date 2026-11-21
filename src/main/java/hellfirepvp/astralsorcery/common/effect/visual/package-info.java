/**
 * 视觉特效创建、刷新与生命周期观察的运行时能力契约。
 *
 * <p>接口定义能力，不保存生命周期状态；实际状态由客户端 EntityComplexFX 等抽象基类拥有。
 * 本包无客户端类型依赖，但使用客户端模板或特效源的调用方仍须遵守客户端执行边界。
 * 特效门面与不可变数据描述由 api.effect 管理；本包能力不发送网络包、不注册 MobEffect，
 * 也不复制渲染队列。</p>
 *
 * @see hellfirepvp.astralsorcery.common.effect.visual.EffectHandle
 * @see "仓库 docs/effects-facade.md"
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.effect.visual;
