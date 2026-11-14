/**
 * 同步服务端状态、客户端镜像及旧 provider 和 reader 适配基类。
 *
 * <p>基类拥有通道身份与清理生命周期，编码解码能力使用 api.sync；客户端镜像也须能在专用服务器创建。</p>
 *
 * @see hellfirepvp.astralsorcery.common.data.sync.base.AbstractData
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common.data.sync.base;
