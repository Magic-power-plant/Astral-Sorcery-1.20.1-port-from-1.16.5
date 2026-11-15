/**
 * 视觉特效的不可变数据描述、无状态门面与创建、刷新、生命周期观察能力契约。
 *
 * <p>接口定义能力，不保存生命周期状态；实际状态由客户端 EntityComplexFX 等抽象基类拥有。
 * 本包无客户端类型依赖，但使用客户端模板或特效源的调用方仍须遵守客户端执行边界。
 * EffectSpec 以模板 ID 与可序列化参数描述外观，通过显式后端解析；缺省参数继承模板配置。
 * 门面不发送网络包、不注册 MobEffect，也不复制渲染队列。</p>
 *
 * @see hellfirepvp.astralsorcery.common.effect.visual.Effects
 * @see hellfirepvp.astralsorcery.common.effect.visual.EffectHandle
 * @see hellfirepvp.astralsorcery.common.effect.visual.EffectSpec
 * @see hellfirepvp.astralsorcery.common.effect.visual.EffectSpecBackend
 * @see "仓库 docs/effects-facade.md"
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.effect.visual;
