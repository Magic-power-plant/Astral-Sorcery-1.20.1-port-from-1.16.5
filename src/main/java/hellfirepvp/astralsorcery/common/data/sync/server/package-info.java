/**
 * 光路、赞助者光点和时间冻结等通道的服务端状态。
 *
 * <p>通过 SyncDataHolder 的锁内访问维护状态和脏标记；全量快照不能消费待发增量。</p>
 *
 * @see hellfirepvp.astralsorcery.common.data.sync.server.DataLightConnections
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.data.sync.server;
