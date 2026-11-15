/**
 * 视觉特效类型、属性、数据描述解析、特效实例基类与实体状态效果图标绘制。
 *
 * <p>实例生命周期由效果处理器驱动；伤害、奖励等玩法副作用不能由视觉效果决定。
 * MobEffectIconRenderer 仅通过 Forge 客户端初始化钩子安装，状态效果逻辑归 common.effect.mob。</p>
 *
 * @see hellfirepvp.astralsorcery.client.effect.EntityVisualFX
 * @see hellfirepvp.astralsorcery.client.effect.ClientEffectSpecs
 * @see hellfirepvp.astralsorcery.client.effect.EffectSpecRegistry
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.client.effect;
