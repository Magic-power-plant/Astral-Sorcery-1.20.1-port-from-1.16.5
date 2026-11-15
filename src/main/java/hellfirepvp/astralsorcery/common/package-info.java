/**
 * 公共初始化代理、tick 契约与公共任务调度。
 *
 * <p>编排共享服务，根包禁止直接依赖客户端类型；物理客户端工作从 ClientProxy 入口接入，
 * 逻辑服务端负责权威状态。GuiType 仅保存稳定协议标识和参数序列化，客户端构造屏幕。</p>
 *
 * @see hellfirepvp.astralsorcery.common.CommonProxy
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.common;
