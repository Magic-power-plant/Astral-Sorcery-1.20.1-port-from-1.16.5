/**
 * 命名同步通道注册、数据持有及全量和增量同步协调。
 *
 * <p>注册窗口关闭后保持注册表稳定；服务端状态通过 holder 访问并在同一锁内修改和标脏。</p>
 *
 * @see hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.data.sync;
