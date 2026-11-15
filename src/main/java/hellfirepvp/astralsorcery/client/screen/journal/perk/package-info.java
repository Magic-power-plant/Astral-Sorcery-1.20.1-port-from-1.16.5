/**
 * 星能力树节点绘制、批次与视图尺寸管理。
 *
 * <p>读取星能力树快照，PerkRenderRegistry 按模型类型及最近父类查表选择渲染器。
 * PerkRender / DynamicPerkRender 定义批次绘制和动态覆盖层能力，新增节点可在客户端注册绑定。
 * 分组在批次建立时去重；节点分配和返还必须由服务端处理。</p>
 *
 * @see hellfirepvp.astralsorcery.client.screen.journal.perk.PerkRenderRegistry
 * @see hellfirepvp.astralsorcery.client.screen.journal.perk.PerkRender
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.client.screen.journal.perk;
