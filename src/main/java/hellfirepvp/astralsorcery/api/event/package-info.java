/**
 * 统一扩展注册事件及 transmission、source、sync 注册入口。
 *
 * <p>监听 Forge EVENT_BUS，在事件回调内同步注册；注册窗口关闭后不得保留事件继续写入。</p>
 *
 * @see hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.api.event;
