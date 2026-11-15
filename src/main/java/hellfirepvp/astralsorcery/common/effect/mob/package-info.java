/**
 * 流血、免死与掉落修正等实体状态效果及其事件行为。
 *
 * <p>MobEffect 管理效果定义，MobEffectInstance 管理实体上的持续时间与强度；
 * EffectCustomTexture 提供共同事件挂接与图标名称。图标绘制由 client.effect 实现，
 * 仅通过 Forge 的 initializeClient 钩子安装，不在公共静态初始化中创建客户端资源。</p>
 *
 * @see hellfirepvp.astralsorcery.common.effect.mob.EffectCustomTexture
 * @see hellfirepvp.astralsorcery.common.effect.mob.EffectBleed
 * @see "仓库 docs/effects-facade.md"
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.effect.mob;
