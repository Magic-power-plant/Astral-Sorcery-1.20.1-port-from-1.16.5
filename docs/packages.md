# 包职责与组织约定

适用范围：本仓库的 Minecraft 1.20.1 / Forge 移植版，Java 17。
本文约定包的职责、依赖边界与维护方式；每个包的就近说明以对应的 `package-info.java` 为入口。

## 覆盖范围与统计口径

截至 2026-11-14，`src/main/java` 下有 **269 个实际声明包**、1,382 个业务 Java 源文件，
每个包配套一份 `package-info.java`。按根命名空间划分：入口 1、API 3、客户端 55、公共实现 208、Mixin 2。

- 根命名空间为 `hellfirepvp.astralsorcery`；以源文件的 package 声明为准，并核对目录路径。
- 只给直接包含 Java 类型的生产源码包编写包文档。仅组织子目录而没有直接类型的目录不创建空包。
- `src/test/java` 的测试不重复声明生产包文档；仅测试使用的包不计入上述生产包数量。
- `reference/`、`external/`、`build/` 和生成资源不属于本次生产包文档范围。
- 该数量是源码快照，不是长期硬编码配额。新增、拆分、合并或删除包时同步更新包文档与下方索引。

包说明表达当前类型的职责和后续维护约束，**不表示所有移植功能均已完成**。
现有 `TODO_PORT`、`BLOCKED_PORT` 与类内实现说明继续有效；注册层结构调整见 [注册表约定](registries.md)，注册 ID 保持不变。

## 层级与依赖边界

| 层级 | 放置内容 | 边界 |
| --- | --- | --- |
| 根包 | 模组启动、公共标识、Mixin 连接 | 仅装配入口，具体领域逻辑下沉 |
| api、api.event、api.sync | 明确公布的扩展版本、注册事件和能力契约 | 稳定范围以 [扩展 API 约定](extension-api.md) 为准 |
| common | 玩法、状态、配方、数据、注册、网络及世界生成 | 区分物理加载侧与逻辑执行侧，不因包名就假定所有成员都适合服务端调用 |
| client | 界面、渲染、相机、声音、资源与客户端输入 | 从客户端初始化进入，不能成为公共静态初始化的强依赖 |
| mixin、mixin.client | 原版或 Forge 钩子的窄桥接 | 复杂处理委托领域服务；客户端注入只放配置的 client 清单 |

这些边界是维护规则，不是声称现有导入图已经完全单向：例如 `SyncDataProvider`
明确使用 `AbstractData` 和 `ClientData`，这是现有扩展契约的一部分。
不得为了消除表面依赖而复制类型、重命名包或破坏已有扩展签名。

### 放置新代码

1. 先按玩法领域归属选择包，再按实际职责选择子包，例如配方序列化放 `common.crafting.serializer`，
   星光节点 provider 放 `common.starlight.transmission.registry`，而不是一律塞进 util。
2. `base` 用于共享状态、生命周期或确有复用的实现；`provider` / `factory` 表示创建或供给行为，
   `registry` 表示注册和查询，不能仅凭后缀推断它必须是接口或基类。
3. `common.lib` 保留注册句柄、既有 DeferredRegister 声明和常量，`common.registry` 装配注册流程。
   不复制第二套注册表，不把某个领域的运行时状态塞入全局持有类。
4. `util` 只接收明确可复用的辅助；有玩家、设备、世界生命周期的服务优先留在对应领域。
5. 包名采用现有的小写层级。保留历史命名与可用引用；单纯补文档不触发结构迁移。
6. 新增第三方 API 引用放入对应 `common.integration` 边界，确保依赖不存在时不会由公共启动路径加载。
   现有 CraftTweaker 等占位不等于已实现的集成，状态以源码说明为准。

## 状态生命周期与能力抽象

**抽象基类封装领域实体的生命周期状态，接口定义领域能力。**

- 生命周期包括创建、加载、保存、同步、tick、卸载、清理及相应状态转换。
  例如 `SynchronizedBlockEntity`、`TickableBlockEntity`、`NetworkBlockEntity`
  以及 `AbstractData` / `ClientData` 承担现有状态和生命周期职责。
- 能力接口表达调用方真正需要的行为，例如 `CrystalAttributeItem`、`ConstellationItem`、
  `IStarlightSource` / `IStarlightReceiver`、`SyncTarget` 和同步读写接口。
  能力接口不要求继承某个具体物品或设备类，也不等同于 Forge capability 注册。
- 无需为每个实现机械地添加接口或空基类。只有共享状态或生命周期时才引入基类；
  需要跨实现组合行为时提取能力。不要把状态管理复制到接口 default 方法。
- 工厂/provider 可以是接口，也可以是有状态抽象基类；例如 `ModifierSourceProvider`
  管理玩家来源缓存，不能为了统一外观而丢掉其应用、替换和撤销生命周期。
- 每个物品栈的属性放在 `ItemStack` 数据中，每玩家状态由玩家数据服务管理，
  世界/维度状态交给对应管理器；单例 Item 或注册对象不得缓存某位玩家的可变数据。
- 扩展入口优先使用现有事件、能力接口和 provider，保留领域层与适配层之间的扩展空间，
  但不预建没有调用方和实现的空包或多余层次。

## 加载侧、线程和生命周期

### 物理侧与逻辑侧

- 物理客户端专属类由客户端初始化入口接入；服务端操作检查逻辑侧。
  单人集成服务器同时存在两个逻辑侧，不能以“正在客户端进程中运行”代替服务端权限校验。
- `client` 是客户端表现层；`common` 并不保证所有成员都可在服务端调用。
  历史例外包括 `common.item.base.client` 与 `common.crafting.recipe.altar.effect`。
  赞助者外观 provider 和 `RegistryBlockRenderTypes` 已迁至 client。新代码须沿用明确的客户端调用路径，不扩散这些依赖。
- `common.data.sync.client` 的 client 表示同步镜像用途，**不是**物理客户端专属类。
  同步注册会在专用服务器创建这些数据和 reader；它们不能依赖 Minecraft 客户端渲染类。
- 本次包文档不引入 `@OnlyIn`、非空默认值或其他包级注解，也不以注释代替实际的加载隔离。

### 网络包后缀表示发送方

| 包后缀 | 方向 | 约束 |
| --- | --- | --- |
| common.network.login.client | 客户端 → 服务端 | 登录确认，保留握手索引和顺序 |
| common.network.login.server | 服务端 → 客户端 | 登录期全量同步，通过登录消息注册 |
| common.network.play.client | 客户端 → 服务端 | 请求必须验证发送者、权限、目标和参数 |
| common.network.play.server | 服务端 → 客户端 | 在客户端游戏线程应用同步和表现 |
| common.network.packet | 当前为服务端 → 客户端的知识同步 | 以 PacketChannel 注册及消息实现为准 |

具体消息的处理器负责调度与处理完成标记，不再外包一层重复排队。
包名不替代消息方向校验；变更消息顺序、字段编码或 ID 时另行评估协议兼容性。

### 可变数据和后台任务

- 世界与实体修改在所属游戏线程执行。网络线程和星光后台拓扑计算不能直接操作活跃世界或客户端对象。
- 服务端同步状态通过 `SyncDataHolder` 的访问入口修改，同一锁内更新并标脏；
  full 快照不消费待发 diff，空 full 也须替换旧状态，diff 按通道约定处理更新和删除。
- IO 和重载区分准备、快照、应用与清理；不可把可变 tag、集合或世界对象泄漏到生命周期不匹配的线程。
- 玩家退出、维度卸载、服务器停止或客户端断开时，按拥有者契约清理缓存、监听、循环效果和待处理状态。
- 玩家与世界数据路径统一经 `WorldDataDirectory` 解析；具体目录见 [玩家进度与存档目录](playerdata-storage.md)。
  不以工作目录作为世界存档根目录。

## 扩展 API、注册和兼容性

- `public` 或存在包文档不意味着稳定 API。公开兼容承诺只覆盖 [扩展 API 约定](extension-api.md) 明列的契约，
  其他 common/client 类型默认属于实现细节。
- 附属扩展监听 `RegisterAstralSorceryExtensionsEvent`，在 Forge EVENT_BUS 的同步回调内完成注册。
  不异步延迟注册，不保留已关闭事件继续写入，不绕过冻结注册表。
- 新 provider 使用稳定、显式、带自身 namespace 的标识；创建的节点/状态遵守各自工厂契约，
  不因 Java 类改名而改变已有存档 ID。NBT 或同步 schema 的改变另附迁移说明。
- 注册声明与查询统一由 `RegistriesAS` 的 typed handle 管理，`RegistryBootstrap` 装配生命周期；
  旧 primer 与空注册类已经退役，命名和兼容性规则见 [注册表约定](registries.md)。
- 配方 JSON、代码驱动配方、配置注册表与扩展注册表各有生命周期，不能因同称 registry 而混用重载/冻结语义。
- KubeJS 配方字段和示例同时维护 [KubeJS 文档](kubejs/README.md)，避免脚本适配和 Java 配方语义分叉。

## package-info.java 写作约定

每个有生产 Java 类型的包必须有一份 `package-info.java`，与类型位于同一目录，包声明与目录完全一致。

- 使用 UTF-8 无 BOM，与本次新增文件一致采用 LF 换行；不重新格式化已有业务文件。
- 使用中文解释职责，Java 标识符保留原名。首句写出该包实际负责什么，不能只写“本包包含相关类”。
- 第二段写清边界或约束：谁拥有状态、从何处调用、执行侧、线程、注册或存储规则中至少一个相关点。
- 用 `@see` 指向包内实际存在的代表类型，再引用仓库约定文档。不要把类名清单当成职责说明。
- Javadoc 放在 package 声明之前；无需 import、空类型、伪实现或 package 注解。
- 包说明中的约束必须与源码对照。历史例外、兼容空壳和迁移限制需明确指出，不能把期望设计写成已实现保证。

以下是实际包的格式示例：

```java
/**
 * 同步请求、编码、解码及同步通道工厂的能力接口。
 *
 * <p>接口只定义能力；状态与清理由 common.data.sync.base 管理，工厂产物须能在专用服务器创建。</p>
 *
 * @see hellfirepvp.astralsorcery.api.sync.SyncDataProvider
 * @see "仓库 docs/packages.md"
 */
package hellfirepvp.astralsorcery.api.sync;
```

## 修改后的检查

1. 新增/删除包时检查生产包与包文档一一对应，并更新下方索引和统计数。
2. 核对包声明、代表类型、客户端加载路径与线程约束；链接或类型重命名时同步修改包文档。
3. 在仓库根目录执行最窄有用的 JVM 验证：

```powershell
.\gradlew.bat compileJava
```

本次包迁移同时修改赞助者同步与客户端生命周期；使用 `test` 检查工厂隔离、快照、差量及清理，不能用编译代替行为验证。
Gradle 编译检查 Java 包声明，但不验证全部 Javadoc 语义；编辑包含类型链接或 HTML 的注释时还应检查 Javadoc 诊断。

可在 PowerShell 中快速检查覆盖（缺失和孤立文档均视为失败）：

```powershell
$root = (Resolve-Path -LiteralPath 'src/main/java').Path
$types = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter '*.java' |
    Where-Object Name -ne 'package-info.java')
$packages = @($types.DirectoryName | Sort-Object -Unique)
$infos = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter 'package-info.java')
$missing = @($packages | Where-Object {
    -not (Test-Path -LiteralPath (Join-Path $_ 'package-info.java'))
})
$orphan = @($infos | Where-Object { $_.DirectoryName -notin $packages })
if ($missing.Count -or $orphan.Count) {
    $missing
    $orphan.FullName
    throw '生产包与 package-info.java 未一一对应'
}
foreach ($info in $infos) {
    $expected = $info.DirectoryName.Substring($root.Length + 1).Replace('\', '.')
    $text = Get-Content -LiteralPath $info.FullName -Raw -Encoding UTF8
    $declaration = [regex]::Match($text, '(?m)^package\s+([\w.]+)\s*;')
    if (-not $declaration.Success -or $declaration.Groups[1].Value -ne $expected) {
        throw ('包声明与目录不一致: ' + $info.FullName)
    }
}
Write-Output ('生产包: {0}; 包文档: {1}' -f $packages.Count, $infos.Count)
```

完整包索引如下；包名省略共同前缀 `hellfirepvp.astralsorcery`，链接直达该包的就近说明。

## 本次包重排与客户端拆分

- 方块实体按 altar、crystal、fountain、gateway、illumination、infusion、observation、refraction、ritual、starlight、treebeacon、utility 分组。能力契约进入 capability，库存和流体实现进入 inventory / fluid，原有 base 与 network 层继续管理公共生命周期。
- 原 util 的方块、实体和物品辅助分别进入 common.block.util、common.entity.util、common.item.util；集合、函数、并发、数学和颜色辅助下沉到具名子包。配方辅助、事件钩子和天击伤害回归所属领域。重复的 util.data.NBTHelper 已收敛到 util.nbt.NBTHelper。
- 原 common.base.patreon 拆为 common.patreon.data / effect / entity / manager 与 client.patreon 的 provider / effect / entity / manager。共享枚举不构造客户端 provider，网络 reader 不等待赞助者目录下载，也不受视觉配置过滤。
- 客户端颜色查询、循环音效、向量顶点提交、客户端 reach 钩子、天击与时间冻结粒子迁入 client；公共侧只保留数值/状态逻辑以及现有侧代理入口。RegistryBlockRenderTypes 仅由客户端装配。
- 伴随实体的 full 快照不消费 diff；按拥有者发送的差量替换过时实体，退出、删除和关闭效果会清理渲染监听。注册 ID、effectClass 枚举名称、NBT 键与同步通道 ID 均保持不变。旧加速黑名单的完整类名与类名前缀通过显式映射继续识别，序列化保留用户原始配置。内部 Java 包路径发生变化，附属源码应更新 import。
- 此次未扩展到整个 common 的渲染迁移；物品渲染能力、祭坛配方视觉效果及部分设备的历史客户端方法仍沿已有调用边界运行。

## 包索引

### 入口与扩展 API（4）

| 包 | 职责 |
| --- | --- |
| [`（根包）`](../src/main/java/hellfirepvp/astralsorcery/package-info.java) | Astral Sorcery 模组入口与 Mixin 配置连接。 |
| [`api`](../src/main/java/hellfirepvp/astralsorcery/api/package-info.java) | 面向附属模组的扩展 API 版本入口。 |
| [`api.event`](../src/main/java/hellfirepvp/astralsorcery/api/event/package-info.java) | 统一扩展注册事件及 transmission、source、sync 注册入口。 |
| [`api.sync`](../src/main/java/hellfirepvp/astralsorcery/api/sync/package-info.java) | 同步请求、编码、解码及同步通道工厂的能力接口。 |

### 客户端（55）

| 包 | 职责 |
| --- | --- |
| [`client`](../src/main/java/hellfirepvp/astralsorcery/client/package-info.java) | 客户端初始化代理与客户端任务调度。 |
| [`client.constellation`](../src/main/java/hellfirepvp/astralsorcery/client/constellation/package-info.java) | 星座背景与星图绘制信息。 |
| [`client.data.config`](../src/main/java/hellfirepvp/astralsorcery/client/data/config/package-info.java) | 客户端配置规格与配置装配。 |
| [`client.data.config.entry`](../src/main/java/hellfirepvp/astralsorcery/client/data/config/entry/package-info.java) | 渲染选项与客户端配置条目。 |
| [`client.effect`](../src/main/java/hellfirepvp/astralsorcery/client/effect/package-info.java) | 视觉特效类型、属性和特效实例基类。 |
| [`client.effect.context`](../src/main/java/hellfirepvp/astralsorcery/client/effect/context/package-info.java) | 各类特效的批量绘制上下文。 |
| [`client.effect.context.base`](../src/main/java/hellfirepvp/astralsorcery/client/effect/context/base/package-info.java) | 批量特效渲染上下文的公共基类。 |
| [`client.effect.function`](../src/main/java/hellfirepvp/astralsorcery/client/effect/function/package-info.java) | 特效透明度、颜色、位移与缩放等变化函数契约。 |
| [`client.effect.function.impl`](../src/main/java/hellfirepvp/astralsorcery/client/effect/function/impl/package-info.java) | 内置特效渲染偏移函数实现。 |
| [`client.effect.handler`](../src/main/java/hellfirepvp/astralsorcery/client/effect/handler/package-info.java) | 特效注册、更新、添加与渲染调度。 |
| [`client.effect.source`](../src/main/java/hellfirepvp/astralsorcery/client/effect/source/package-info.java) | 持续特效源，以及天击和时间冻结的客户端表现。 |
| [`client.effect.source.orbital`](../src/main/java/hellfirepvp/astralsorcery/client/effect/source/orbital/package-info.java) | 聚能、仪式和注入等场景的轨道特效行为。 |
| [`client.effect.vfx`](../src/main/java/hellfirepvp/astralsorcery/client/effect/vfx/package-info.java) | 方块、水晶、光束、闪电与粒子等具体视觉效果。 |
| [`client.event`](../src/main/java/hellfirepvp/astralsorcery/client/event/package-info.java) | 天空、覆盖层、手持物品与星能力等客户端事件适配。 |
| [`client.event.effect`](../src/main/java/hellfirepvp/astralsorcery/client/event/effect/package-info.java) | 特效渲染事件与星门界面绘制适配。 |
| [`client.input`](../src/main/java/hellfirepvp/astralsorcery/client/input/package-info.java) | 客户端按键绑定包装与星能力开关输入。 |
| [`client.lib`](../src/main/java/hellfirepvp/astralsorcery/client/lib/package-info.java) | 客户端纹理、精灵、按键与特效类型的共享引用。 |
| [`client.model.armor`](../src/main/java/hellfirepvp/astralsorcery/client/model/armor/package-info.java) | 披风等装备的客户端模型。 |
| [`client.model.builtin`](../src/main/java/hellfirepvp/astralsorcery/client/model/builtin/package-info.java) | 祭坛、透镜、望远镜和观星台等内置模型。 |
| [`client.patreon`](../src/main/java/hellfirepvp/astralsorcery/client/patreon/package-info.java) | 赞助者效果的客户端构造和生命周期。 |
| [`client.patreon.effect`](../src/main/java/hellfirepvp/astralsorcery/client/patreon/effect/package-info.java) | 赞助者粒子、模型和玩家事件的客户端效果。 |
| [`client.patreon.entity`](../src/main/java/hellfirepvp/astralsorcery/client/patreon/entity/package-info.java) | 由同步伴随实体驱动的客户端粒子和模型。 |
| [`client.patreon.manager`](../src/main/java/hellfirepvp/astralsorcery/client/patreon/manager/package-info.java) | 客户端赞助效果、tick 回调和渲染资源清理。 |
| [`client.patreon.provider`](../src/main/java/hellfirepvp/astralsorcery/client/patreon/provider/package-info.java) | 仅客户端解析和建立赞助者视觉效果。 |
| [`client.registry`](../src/main/java/hellfirepvp/astralsorcery/client/registry/package-info.java) | 纹理、精灵、按键和特效等客户端注册流程。 |
| [`client.render`](../src/main/java/hellfirepvp/astralsorcery/client/render/package-info.java) | 渲染状态构建、OBJ 绘制与缓冲能力。 |
| [`client.render.blockentity`](../src/main/java/hellfirepvp/astralsorcery/client/render/blockentity/package-info.java) | 模组方块实体的客户端渲染器。 |
| [`client.render.entity`](../src/main/java/hellfirepvp/astralsorcery/client/render/entity/package-info.java) | 模组实体与掉落物的客户端渲染器。 |
| [`client.render.entity.layer`](../src/main/java/hellfirepvp/astralsorcery/client/render/entity/layer/package-info.java) | 玩家模型叠加层和赞助者外观绘制。 |
| [`client.resource`](../src/main/java/hellfirepvp/astralsorcery/client/resource/package-info.java) | 纹理、图集、动态资源与重载生命周期管理。 |
| [`client.resource.query`](../src/main/java/hellfirepvp/astralsorcery/client/resource/query/package-info.java) | 纹理、子纹理和精灵的资源查询描述。 |
| [`client.screen`](../src/main/java/hellfirepvp/astralsorcery/client/screen/package-info.java) | 星图、望远镜、观星台和映射台等交互界面。 |
| [`client.screen.base`](../src/main/java/hellfirepvp/astralsorcery/client/screen/base/package-info.java) | 容器、导航、天空与星座发现界面的可复用基类。 |
| [`client.screen.container`](../src/main/java/hellfirepvp/astralsorcery/client/screen/container/package-info.java) | 祭坛及宝典容器对应的客户端界面。 |
| [`client.screen.helper`](../src/main/java/hellfirepvp/astralsorcery/client/screen/helper/package-info.java) | 界面缩放、尺寸和渲染边界辅助类型。 |
| [`client.screen.journal`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/package-info.java) | 宝典主界面及星座、研究和星能力页面导航。 |
| [`client.screen.journal.bookmark`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/bookmark/package-info.java) | 宝典书签定位与导航提供者。 |
| [`client.screen.journal.overlay`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/overlay/package-info.java) | 宝典附加覆盖层和星能力统计面板。 |
| [`client.screen.journal.page`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/page/package-info.java) | 宝典文字、配方与多方块结构的页面渲染。 |
| [`client.screen.journal.perk`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/perk/package-info.java) | 星能力树节点绘制、批次与视图尺寸管理。 |
| [`client.screen.journal.perk.group`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/perk/group/package-info.java) | 星能力点及光晕的分组渲染实现。 |
| [`client.screen.journal.progression`](../src/main/java/hellfirepvp/astralsorcery/client/screen/journal/progression/package-info.java) | 宝典研究星系布局、簇映射与进度视图。 |
| [`client.screen.telescope`](../src/main/java/hellfirepvp/astralsorcery/client/screen/telescope/package-info.java) | 望远镜绘制区域与玩家视角星座投影。 |
| [`client.sky`](../src/main/java/hellfirepvp/astralsorcery/client/sky/package-info.java) | 天空渲染器的组合与串接。 |
| [`client.sky.astral`](../src/main/java/hellfirepvp/astralsorcery/client/sky/astral/package-info.java) | 模组星空渲染实现与渲染安装入口。 |
| [`client.structure`](../src/main/java/hellfirepvp/astralsorcery/client/structure/package-info.java) | 多方块结构预览、快照与预览世界渲染。 |
| [`client.util`](../src/main/java/hellfirepvp/astralsorcery/client/util/package-info.java) | 渲染、客户端资源和界面共用辅助。 |
| [`client.util.camera`](../src/main/java/hellfirepvp/astralsorcery/client/util/camera/package-info.java) | 客户端相机变换、视角替换及停止和 tick 回调。 |
| [`client.util.camera.path`](../src/main/java/hellfirepvp/astralsorcery/client/util/camera/path/package-info.java) | 相机路径描述与路径构建器。 |
| [`client.util.color`](../src/main/java/hellfirepvp/astralsorcery/client/util/color/package-info.java) | 客户端物品颜色和纹理颜色查询。 |
| [`client.util.draw`](../src/main/java/hellfirepvp/astralsorcery/client/util/draw/package-info.java) | 顶点缓冲批次、绘制上下文与 RenderType 装饰。 |
| [`client.util.image`](../src/main/java/hellfirepvp/astralsorcery/client/util/image/package-info.java) | 图像模板和星空图像生成。 |
| [`client.util.obj`](../src/main/java/hellfirepvp/astralsorcery/client/util/obj/package-info.java) | Wavefront OBJ 模型解析与顶点、面及组数据。 |
| [`client.util.sound`](../src/main/java/hellfirepvp/astralsorcery/client/util/sound/package-info.java) | 本地播放、淡入淡出及定位循环音效。 |
| [`client.util.word`](../src/main/java/hellfirepvp/astralsorcery/client/util/word/package-info.java) | 中英文随机词语和名称生成。 |

### 公共实现（208）

| 包 | 职责 |
| --- | --- |
| [`common`](../src/main/java/hellfirepvp/astralsorcery/common/package-info.java) | 公共初始化代理、tick 契约与公共任务调度。 |
| [`common.advancement`](../src/main/java/hellfirepvp/astralsorcery/common/advancement/package-info.java) | 祭坛合成、共鸣、星座发现和星能力等级的进度触发器。 |
| [`common.advancement.instance`](../src/main/java/hellfirepvp/astralsorcery/common/advancement/instance/package-info.java) | 配方、星座和星能力等级的进度条件实例。 |
| [`common.auxiliary`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/package-info.java) | 作物、动物、方块破坏、圣杯与光路等玩法辅助协调。 |
| [`common.auxiliary.book`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/book/package-info.java) | 宝典内容查询与物品关联的索引。 |
| [`common.auxiliary.charge`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/charge/package-info.java) | 玩家星能充能的更新与同步协调。 |
| [`common.auxiliary.combat`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/combat/package-info.java) | 星辰打击的服务端伤害和攻击逻辑。 |
| [`common.auxiliary.gateway`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/gateway/package-info.java) | 天辉星门目录、访问过滤与传送协调。 |
| [`common.auxiliary.link`](../src/main/java/hellfirepvp/astralsorcery/common/auxiliary/link/package-info.java) | 连接工具、可连接方块实体能力与连接操作。 |
| [`common.base`](../src/main/java/hellfirepvp/astralsorcery/common/base/package-info.java) | 可选模组标识、月相及树木类型等公共基础概念。 |
| [`common.block`](../src/main/java/hellfirepvp/astralsorcery/common/block/package-info.java) | 功能方块定义、交互入口与方块状态行为。 |
| [`common.block.altar`](../src/main/java/hellfirepvp/astralsorcery/common/block/altar/package-info.java) | 各阶合成祭坛方块及其交互差异。 |
| [`common.block.base`](../src/main/java/hellfirepvp/astralsorcery/common/block/base/package-info.java) | 方块复用基类及库存、网络和物品形态能力。 |
| [`common.block.base.template`](../src/main/java/hellfirepvp/astralsorcery/common/block/base/template/package-info.java) | 大理石、灌注木、植被和阶梯等方块模板。 |
| [`common.block.blackmarble`](../src/main/java/hellfirepvp/astralsorcery/common/block/blackmarble/package-info.java) | 炭黑大理石建筑方块及纹理变体。 |
| [`common.block.crystal`](../src/main/java/hellfirepvp/astralsorcery/common/block/crystal/package-info.java) | 聚能水晶方块与水晶种类标识。 |
| [`common.block.foliage`](../src/main/java/hellfirepvp/astralsorcery/common/block/foliage/package-info.java) | 模组发光植被方块。 |
| [`common.block.fountain`](../src/main/java/hellfirepvp/astralsorcery/common/block/fountain/package-info.java) | 万象泉引口及流体、矿物、漩涡模式方块。 |
| [`common.block.infusedwood`](../src/main/java/hellfirepvp/astralsorcery/common/block/infusedwood/package-info.java) | 灌注木建筑方块与装饰变体。 |
| [`common.block.marble`](../src/main/java/hellfirepvp/astralsorcery/common/block/marble/package-info.java) | 大理石建筑方块与装饰变体。 |
| [`common.block.ore`](../src/main/java/hellfirepvp/astralsorcery/common/block/ore/package-info.java) | 海蓝宝石、水晶和星辉金属矿物方块。 |
| [`common.block.properties`](../src/main/java/hellfirepvp/astralsorcery/common/block/properties/package-info.java) | 玻璃、大理石、木材等方块属性组合。 |
| [`common.block.util`](../src/main/java/hellfirepvp/astralsorcery/common/block/util/package-info.java) | 方块位置、匹配、几何和搜索辅助。 |
| [`common.block.util.iterator`](../src/main/java/hellfirepvp/astralsorcery/common/block/util/iterator/package-info.java) | 按形状、层与随机邻近关系生成方块位置。 |
| [`common.blockentity.altar`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/altar/package-info.java) | 祭坛及其状态、配方等级与星光存储。 |
| [`common.blockentity.base`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/base/package-info.java) | 同步、tick、所有权、多方块与网络方块实体基类。 |
| [`common.blockentity.base.network`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/base/network/package-info.java) | 星光源、接收器和传输方块实体基类。 |
| [`common.blockentity.capability`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/capability/package-info.java) | 容器提供、命名库存与流体访问的方块实体能力契约。 |
| [`common.blockentity.crystal`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/crystal/package-info.java) | 水晶簇、采集水晶与水晶方块实体状态。 |
| [`common.blockentity.fluid`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/fluid/package-info.java) | 单流体容器的容量、精度及 NBT 状态实现。 |
| [`common.blockentity.fountain`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/fountain/package-info.java) | 喷泉与圣杯设备的容器状态和交互。 |
| [`common.blockentity.gateway`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/gateway/package-info.java) | 星门传送设备的状态及生命周期。 |
| [`common.blockentity.illumination`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/illumination/package-info.java) | 照明设备配置、更新和方块交互状态。 |
| [`common.blockentity.infusion`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/infusion/package-info.java) | 液体注入设备及其容器、配方与同步状态。 |
| [`common.blockentity.inventory`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/inventory/package-info.java) | 方块实体库存的物品槽存储、过滤与菜单共享。 |
| [`common.blockentity.network`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/network/package-info.java) | 祭坛、仪式、树木信标及透镜等设备的星光节点实现。 |
| [`common.blockentity.observation`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/observation/package-info.java) | 观测台和望远镜设备状态。 |
| [`common.blockentity.refraction`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/refraction/package-info.java) | 折射台设备与配方交互状态。 |
| [`common.blockentity.ritual`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/ritual/package-info.java) | 仪式链接与仪式基座状态。 |
| [`common.blockentity.starlight`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/starlight/package-info.java) | 星光源、接收器和传输方块实体。 |
| [`common.blockentity.treebeacon`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/treebeacon/package-info.java) | 树木信标及组件的玩家绑定和颜色状态。 |
| [`common.blockentity.utility`](../src/main/java/hellfirepvp/astralsorcery/common/blockentity/utility/package-info.java) | 透明与隐形方块实体的渲染状态和同步。 |
| [`common.capability`](../src/main/java/hellfirepvp/astralsorcery/common/capability/package-info.java) | 区块地下流体储量及 Forge capability 提供者。 |
| [`common.cmd`](../src/main/java/hellfirepvp/astralsorcery/common/cmd/package-info.java) | Astral Sorcery 管理命令的根入口。 |
| [`common.cmd.argument`](../src/main/java/hellfirepvp/astralsorcery/common/cmd/argument/package-info.java) | 星座命令参数的解析与建议。 |
| [`common.cmd.sub`](../src/main/java/hellfirepvp/astralsorcery/common/cmd/sub/package-info.java) | 共鸣、研究、经验、重置和序列化等管理子命令。 |
| [`common.config`](../src/main/java/hellfirepvp/astralsorcery/common/config/package-info.java) | ConfigAS 静态配置访问入口。 |
| [`common.constellation`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/package-info.java) | 星座定义、能力接口、注册与星空基础协调。 |
| [`common.constellation.effect`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/effect/package-info.java) | 仪式效果的生命周期、参数、提供者与注册。 |
| [`common.constellation.effect.aoe`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/effect/aoe/package-info.java) | 各星座仪式的范围作用实现。 |
| [`common.constellation.effect.base`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/effect/base/package-info.java) | 列表式仪式效果和实体收集的公共基类。 |
| [`common.constellation.engraving`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/engraving/package-info.java) | 蚀刻星图数据与蚀刻效果契约。 |
| [`common.constellation.mantle`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/mantle/package-info.java) | 星斗披风效果契约、查询与注册。 |
| [`common.constellation.mantle.effect`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/mantle/effect/package-info.java) | 各星座披风的具体能力效果。 |
| [`common.constellation.star`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/star/package-info.java) | 星座星点位置与连线的值类型。 |
| [`common.constellation.world`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/world/package-info.java) | 世界天文上下文、星座分布与天体事件调度。 |
| [`common.constellation.world.event`](../src/main/java/hellfirepvp/astralsorcery/common/constellation/world/event/package-info.java) | 日食、月食与流星雨等天体事件定义。 |
| [`common.container`](../src/main/java/hellfirepvp/astralsorcery/common/container/package-info.java) | 祭坛、观星台与宝典的菜单和容器交互。 |
| [`common.container.factory`](../src/main/java/hellfirepvp/astralsorcery/common/container/factory/package-info.java) | 容器与菜单提供者的创建适配。 |
| [`common.container.slot`](../src/main/java/hellfirepvp/astralsorcery/common/container/slot/package-info.java) | 星座聚焦、星图及不可点击槽位规则。 |
| [`common.crafting`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/package-info.java) | 祭坛、注入、转化、流体交互和聚星缸的核心配方模型与执行。 |
| [`common.crafting.builder`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/builder/package-info.java) | 祭坛、聚星缸、注入和转化配方的数据构建器。 |
| [`common.crafting.builtin`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/builtin/package-info.java) | 星座升级、水晶数量及属性复制合并等内置配方。 |
| [`common.crafting.custom`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/custom/package-info.java) | 染色变更等定制配方适配。 |
| [`common.crafting.helper`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/helper/package-info.java) | 配方查询和构建过程辅助。 |
| [`common.crafting.helper.ingredient`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/helper/ingredient/package-info.java) | 水晶与流体自定义原料及序列化。 |
| [`common.crafting.nojson`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/package-info.java) | 共鸣、冻结、熔化及星能液等代码驱动配方注册表。 |
| [`common.crafting.nojson.attunement`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/attunement/package-info.java) | 玩家与水晶共鸣的配方定义。 |
| [`common.crafting.nojson.attunement.active`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/attunement/active/package-info.java) | 玩家与水晶共鸣的运行中配方状态。 |
| [`common.crafting.nojson.fountain`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/fountain/package-info.java) | 万象泉流体与漩涡效果及运行上下文。 |
| [`common.crafting.nojson.freezing`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/freezing/package-info.java) | 方块与流体冻结的代码配方。 |
| [`common.crafting.nojson.meltable`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/meltable/package-info.java) | 方块、物品及熔炉来源的熔化配方。 |
| [`common.crafting.nojson.starlight`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/nojson/starlight/package-info.java) | 星能液中的晶簇生成、水晶成长合并与灌注木转化。 |
| [`common.crafting.recipe`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/recipe/package-info.java) | 门控配方、祭坛配方与转化和注入上下文。 |
| [`common.crafting.recipe.altar`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/recipe/altar/package-info.java) | 简单祭坛配方的活动执行实例。 |
| [`common.crafting.recipe.altar.effect`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/recipe/altar/effect/package-info.java) | 祭坛制作过程的光束、粒子与聚焦表现效果。 |
| [`common.crafting.recipe.infusion`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/recipe/infusion/package-info.java) | 液体注入配方的活动执行实例。 |
| [`common.crafting.recipe.interaction.jei`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/recipe/interaction/jei/package-info.java) | 流体交互结果在 JEI 中的展示适配。 |
| [`common.crafting.serializer`](../src/main/java/hellfirepvp/astralsorcery/common/crafting/serializer/package-info.java) | 祭坛、聚星缸、注入、流体交互和转化配方序列化。 |
| [`common.crystal`](../src/main/java/hellfirepvp/astralsorcery/common/crystal/package-info.java) | 水晶属性、生成、计算及物品和方块实体属性能力。 |
| [`common.crystal.calc`](../src/main/java/hellfirepvp/astralsorcery/common/crystal/calc/package-info.java) | 水晶属性计算的来源与用途定义。 |
| [`common.crystal.property`](../src/main/java/hellfirepvp/astralsorcery/common/crystal/property/package-info.java) | 尺寸、纯度、形状及工具和仪式等晶体属性实现。 |
| [`common.crystal.source`](../src/main/java/hellfirepvp/astralsorcery/common/crystal/source/package-info.java) | 晶体与仪式属性来源及共鸣、特性来源实例。 |
| [`common.data`](../src/main/java/hellfirepvp/astralsorcery/common/data/package-info.java) | 以当前服务器世界为根的数据目录解析。 |
| [`common.data.config`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/package-info.java) | 公共与服务端配置的规格装配。 |
| [`common.data.config.base`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/base/package-info.java) | 配置条目、适配器、数据集与配置注册表的公共抽象。 |
| [`common.data.config.entry`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/entry/package-info.java) | 配方、实体、网络、设备、星能力和世界生成等配置条目。 |
| [`common.data.config.entry.common`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/entry/common/package-info.java) | 公共侧通用配置条目。 |
| [`common.data.config.registry`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/registry/package-info.java) | 附魔、转化、流体和矿物稀有度等配置驱动注册表。 |
| [`common.data.config.registry.sets`](../src/main/java/hellfirepvp/astralsorcery/common/data/config/registry/sets/package-info.java) | 配置注册表中权重、转化和黑名单等条目的数据结构。 |
| [`common.data.journal`](../src/main/java/hellfirepvp/astralsorcery/common/data/journal/package-info.java) | 宝典文字、配方和结构页面的数据模型。 |
| [`common.data.research`](../src/main/java/hellfirepvp/astralsorcery/common/data/research/package-info.java) | 玩家研究、发现、星能力进度及存储和同步协调。 |
| [`common.data.sync`](../src/main/java/hellfirepvp/astralsorcery/common/data/sync/package-info.java) | 命名同步通道注册、数据持有及全量和增量同步协调。 |
| [`common.data.sync.base`](../src/main/java/hellfirepvp/astralsorcery/common/data/sync/base/package-info.java) | 同步服务端状态、客户端镜像及旧 provider 和 reader 适配基类。 |
| [`common.data.sync.client`](../src/main/java/hellfirepvp/astralsorcery/common/data/sync/client/package-info.java) | 专用服务器可加载的同步镜像和解码器。 |
| [`common.data.sync.server`](../src/main/java/hellfirepvp/astralsorcery/common/data/sync/server/package-info.java) | 服务端权威数据和客户端同步快照。 |
| [`common.data.world`](../src/main/java/hellfirepvp/astralsorcery/common/data/world/package-info.java) | 星门、晶矿、星光与存储网络的世界缓存数据。 |
| [`common.data.world.cache`](../src/main/java/hellfirepvp/astralsorcery/common/data/world/cache/package-info.java) | 世界缓存域、缓存管理与异步 IO 调度。 |
| [`common.data.world.cache.base`](../src/main/java/hellfirepvp/astralsorcery/common/data/world/cache/base/package-info.java) | 全局及分区世界数据的基类和区段描述。 |
| [`common.effect`](../src/main/java/hellfirepvp/astralsorcery/common/effect/package-info.java) | 流血、免死与掉落修正等状态效果。 |
| [`common.enchantment`](../src/main/java/hellfirepvp/astralsorcery/common/enchantment/package-info.java) | 夜视、灼热及玩家 tick 附魔实现。 |
| [`common.enchantment.amulet`](../src/main/java/hellfirepvp/astralsorcery/common/enchantment/amulet/package-info.java) | 附魔护符的附魔选择、随机化与玩家效果管理。 |
| [`common.enchantment.dynamic`](../src/main/java/hellfirepvp/astralsorcery/common/enchantment/dynamic/package-info.java) | 运行时动态附魔类型与计算辅助。 |
| [`common.entity`](../src/main/java/hellfirepvp/astralsorcery/common/entity/package-info.java) | 光点、流星、星辉工具等实体逻辑与交互。 |
| [`common.entity.goal`](../src/main/java/hellfirepvp/astralsorcery/common/entity/goal/package-info.java) | 星辉工具的挖掘、伐木和攻击 AI 目标。 |
| [`common.entity.item`](../src/main/java/hellfirepvp/astralsorcery/common/entity/item/package-info.java) | 水晶、宝石、星辉金属及特殊掉落物实体。 |
| [`common.entity.technical`](../src/main/java/hellfirepvp/astralsorcery/common/entity/technical/package-info.java) | 抓钩与观星台辅助等技术实体。 |
| [`common.entity.util`](../src/main/java/hellfirepvp/astralsorcery/common/entity/util/package-info.java) | 实体查询、生成、移动和战利品辅助。 |
| [`common.event`](../src/main/java/hellfirepvp/astralsorcery/common/event/package-info.java) | 模组领域事件、注册事件及方块变化通知。 |
| [`common.event.handler`](../src/main/java/hellfirepvp/astralsorcery/common/event/handler/package-info.java) | 交互、自动连接、存储和缓存等 Forge 事件处理器。 |
| [`common.event.helper`](../src/main/java/hellfirepvp/astralsorcery/common/event/helper/package-info.java) | 事件总线适配、钩子端点和实体监听辅助。 |
| [`common.fluid`](../src/main/java/hellfirepvp/astralsorcery/common/fluid/package-info.java) | 星能液流体、流体方块与桶物品。 |
| [`common.integration`](../src/main/java/hellfirepvp/astralsorcery/common/integration/package-info.java) | 可选模组检测及 Botania、Curios、CraftTweaker、JEI 联动入口。 |
| [`common.integration.crt`](../src/main/java/hellfirepvp/astralsorcery/common/integration/crt/package-info.java) | CraftTweaker 配方管理器的移植适配。 |
| [`common.integration.jei`](../src/main/java/hellfirepvp/astralsorcery/common/integration/jei/package-info.java) | JEI 配方分类、配方展示与祭坛物品转移。 |
| [`common.integration.kubejs`](../src/main/java/hellfirepvp/astralsorcery/common/integration/kubejs/package-info.java) | KubeJS 配方 schema、组件及插件入口。 |
| [`common.item`](../src/main/java/hellfirepvp/astralsorcery/common/item/package-info.java) | 基础材料、宝典、观测工具及连接器等物品行为。 |
| [`common.item.armor`](../src/main/java/hellfirepvp/astralsorcery/common/item/armor/package-info.java) | 星斗披风及灌注皮革装备材质。 |
| [`common.item.base`](../src/main/java/hellfirepvp/astralsorcery/common/item/base/package-info.java) | 充能、聚焦、交互与方块存储等物品能力及复用类型。 |
| [`common.item.base.client`](../src/main/java/hellfirepvp/astralsorcery/common/item/base/client/package-info.java) | 物品动态颜色、手持与覆盖层绘制的客户端能力。 |
| [`common.item.block`](../src/main/java/hellfirepvp/astralsorcery/common/item/block/package-info.java) | 水晶、透镜、棱镜等方块的物品形态。 |
| [`common.item.crystal`](../src/main/java/hellfirepvp/astralsorcery/common/item/crystal/package-info.java) | 普通与共鸣水晶物品的共用实现和具体品种。 |
| [`common.item.dust`](../src/main/java/hellfirepvp/astralsorcery/common/item/dust/package-info.java) | 辉光粉、夜光粉及可使用粉末物品。 |
| [`common.item.gem`](../src/main/java/hellfirepvp/astralsorcery/common/item/gem/package-info.java) | 星能力宝石类型、属性及昼夜天空宝石物品。 |
| [`common.item.lens`](../src/main/java/hellfirepvp/astralsorcery/common/item/lens/package-info.java) | 彩色透镜物品、颜色类型与各类透镜作用。 |
| [`common.item.quality`](../src/main/java/hellfirepvp/astralsorcery/common/item/quality/package-info.java) | 璀璨宝石品质与镶框物品。 |
| [`common.item.tool`](../src/main/java/hellfirepvp/astralsorcery/common/item/tool/package-info.java) | 水晶及灌注水晶工具、材质和工具属性实现。 |
| [`common.item.useables`](../src/main/java/hellfirepvp/astralsorcery/common/item/useables/package-info.java) | 星能力封印与更替之星等一次性消耗物品。 |
| [`common.item.util`](../src/main/java/hellfirepvp/astralsorcery/common/item/util/package-info.java) | 物品栈构造、比较及通用物品操作。 |
| [`common.item.wand`](../src/main/java/hellfirepvp/astralsorcery/common/item/wand/package-info.java) | 各类星杖实现与可被星杖操作的能力接口。 |
| [`common.lib`](../src/main/java/hellfirepvp/astralsorcery/common/lib/package-info.java) | 方块、物品、星座和配方等注册句柄、声明与公共常量。 |
| [`common.loot`](../src/main/java/hellfirepvp/astralsorcery/common/loot/package-info.java) | 水晶、星座、星门颜色与幸运等战利品函数。 |
| [`common.loot.global`](../src/main/java/hellfirepvp/astralsorcery/common/loot/global/package-info.java) | 星能力虚空丢弃与灼热附魔的全局战利品修正器。 |
| [`common.network`](../src/main/java/hellfirepvp/astralsorcery/common/network/package-info.java) | 模组通道、消息注册及发送辅助。 |
| [`common.network.base`](../src/main/java/hellfirepvp/astralsorcery/common/network/base/package-info.java) | 普通消息与登录消息的编解码和处理基类。 |
| [`common.network.channel`](../src/main/java/hellfirepvp/astralsorcery/common/network/channel/package-info.java) | 普通发送及缓冲回复的通道适配。 |
| [`common.network.login.client`](../src/main/java/hellfirepvp/astralsorcery/common/network/login/client/package-info.java) | 客户端发往服务端的登录确认消息。 |
| [`common.network.login.server`](../src/main/java/hellfirepvp/astralsorcery/common/network/login/server/package-info.java) | 服务端发往客户端的登录期数据、星门与星能力快照。 |
| [`common.network.packet`](../src/main/java/hellfirepvp/astralsorcery/common/network/packet/package-info.java) | 研究知识同步消息。 |
| [`common.network.play.client`](../src/main/java/hellfirepvp/astralsorcery/common/network/play/client/package-info.java) | 客户端发往服务端的共鸣、研究、传送及设备操作请求。 |
| [`common.network.play.server`](../src/main/java/hellfirepvp/astralsorcery/common/network/play/server/package-info.java) | 服务端发往客户端的进度、效果与运行状态更新。 |
| [`common.patreon.data`](../src/main/java/hellfirepvp/astralsorcery/common/patreon/data/package-info.java) | 赞助者目录格式、下载与完整数据快照发布。 |
| [`common.patreon.effect`](../src/main/java/hellfirepvp/astralsorcery/common/patreon/effect/package-info.java) | 赞助者效果标识、不可变配置、工厂能力和目录索引。 |
| [`common.patreon.entity`](../src/main/java/hellfirepvp/astralsorcery/common/patreon/entity/package-info.java) | 赞助者伴随实体的位置、维度、NBT 与同步生命周期。 |
| [`common.patreon.manager`](../src/main/java/hellfirepvp/astralsorcery/common/patreon/manager/package-info.java) | 赞助者服务器 tick、伴随实体创建及差量更新。 |
| [`common.perk`](../src/main/java/hellfirepvp/astralsorcery/common/perk/package-info.java) | 星能力节点基类、属性汇总、等级、冷却和分配状态。 |
| [`common.perk.data`](../src/main/java/hellfirepvp/astralsorcery/common/perk/data/package-info.java) | 星能力树资源加载、预处理与类型解释。 |
| [`common.perk.data.builder`](../src/main/java/hellfirepvp/astralsorcery/common/perk/data/builder/package-info.java) | 星能力树数据生成与节点描述构建。 |
| [`common.perk.modifier`](../src/main/java/hellfirepvp/astralsorcery/common/perk/modifier/package-info.java) | 星能力属性修正、转换与动态属性修正实现。 |
| [`common.perk.node`](../src/main/java/hellfirepvp/astralsorcery/common/perk/node/package-info.java) | 根源、关键、主要及星座节点类别。 |
| [`common.perk.node.focus`](../src/main/java/hellfirepvp/astralsorcery/common/perk/node/focus/package-info.java) | 朦胧星座等聚焦节点的星能力效果。 |
| [`common.perk.node.key`](../src/main/java/hellfirepvp/astralsorcery/common/perk/node/key/package-info.java) | 闪电、流血、磁吸、飞行等关键星能力实现。 |
| [`common.perk.node.root`](../src/main/java/hellfirepvp/astralsorcery/common/perk/node/root/package-info.java) | 五个明亮星座的根源星能力节点。 |
| [`common.perk.node.socket`](../src/main/java/hellfirepvp/astralsorcery/common/perk/node/socket/package-info.java) | 星能力宝石插槽与可镶嵌物品能力。 |
| [`common.perk.reader`](../src/main/java/hellfirepvp/astralsorcery/common/perk/reader/package-info.java) | 星能力属性的统计读取、解释与显示值计算。 |
| [`common.perk.source`](../src/main/java/hellfirepvp/astralsorcery/common/perk/source/package-info.java) | 星能力属性来源、提供者与来源差异管理。 |
| [`common.perk.source.provider`](../src/main/java/hellfirepvp/astralsorcery/common/perk/source/provider/package-info.java) | 玩家已分配星能力的属性来源提供者。 |
| [`common.perk.source.provider.equipment`](../src/main/java/hellfirepvp/astralsorcery/common/perk/source/provider/equipment/package-info.java) | 装备产生的星能力属性来源与提供者。 |
| [`common.perk.tick`](../src/main/java/hellfirepvp/astralsorcery/common/perk/tick/package-info.java) | 玩家星能力的周期更新与 tick 能力契约。 |
| [`common.perk.tree`](../src/main/java/hellfirepvp/astralsorcery/common/perk/tree/package-info.java) | 星能力树中普通、主要、星座和宝石节点的树形描述。 |
| [`common.perk.type`](../src/main/java/hellfirepvp/astralsorcery/common/perk/type/package-info.java) | 移动、攻击、抗性及效果强度等星能力属性类型。 |
| [`common.perk.type.vanilla`](../src/main/java/hellfirepvp/astralsorcery/common/perk/type/vanilla/package-info.java) | 星能力属性与原版实体属性的映射适配。 |
| [`common.registry`](../src/main/java/hellfirepvp/astralsorcery/common/registry/package-info.java) | 公共内容、配方、世界生成及领域对象的注册装配。 |
| [`common.registry.extension`](../src/main/java/hellfirepvp/astralsorcery/common/registry/extension/package-info.java) | 扩展注册窗口与可冻结、重复键校验的注册表设施。 |
| [`common.starlight`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/package-info.java) | 星光源、接收器、传输能力与世界网络入口。 |
| [`common.starlight.network`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/network/package-info.java) | 星光网络连接、拓扑、区块跟踪与更新调度。 |
| [`common.starlight.network.handler`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/network/handler/package-info.java) | 星光网络驱动的方块转化处理。 |
| [`common.starlight.transmission`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/transmission/package-info.java) | 传输节点、源、接收器和棱镜的能力接口及连接辅助。 |
| [`common.starlight.transmission.base`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/transmission/base/package-info.java) | 基础传输节点、源、接收器与棱镜节点实现。 |
| [`common.starlight.transmission.base.crystal`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/transmission/base/crystal/package-info.java) | 水晶传输节点、棱镜节点与独立晶体星光源。 |
| [`common.starlight.transmission.registry`](../src/main/java/hellfirepvp/astralsorcery/common/starlight/transmission/registry/package-info.java) | 传输节点与独立星光源的 provider 注册和按 ID 创建。 |
| [`common.storage`](../src/main/java/hellfirepvp/astralsorcery/common/storage/package-info.java) | 方块存储网络、核心区域、缓存键及存储物品描述。 |
| [`common.structure`](../src/main/java/hellfirepvp/astralsorcery/common/structure/package-info.java) | 祭坛、星门、仪式及其他多方块设备的结构模式。 |
| [`common.structure.match.block`](../src/main/java/hellfirepvp/astralsorcery/common/structure/match/block/package-info.java) | 方块与方块状态的结构匹配条件。 |
| [`common.structure.match.blockentity`](../src/main/java/hellfirepvp/astralsorcery/common/structure/match/blockentity/package-info.java) | 结构中方块实体的匹配能力与基础实现。 |
| [`common.structure.match.structure`](../src/main/java/hellfirepvp/astralsorcery/common/structure/match/structure/package-info.java) | 可匹配、可放置结构的契约与结构实现。 |
| [`common.structure.match.util`](../src/main/java/hellfirepvp/astralsorcery/common/structure/match/util/package-info.java) | 结构方块数组、模式数组与坐标辅助。 |
| [`common.structure.types`](../src/main/java/hellfirepvp/astralsorcery/common/structure/types/package-info.java) | 多方块结构种类的标识与描述。 |
| [`common.tick`](../src/main/java/hellfirepvp/astralsorcery/common/tick/package-info.java) | 公共 tick 处理器注册与分侧分阶段调度。 |
| [`common.util`](../src/main/java/hellfirepvp/astralsorcery/common/util/package-info.java) | 现有跨领域游戏辅助的兼容入口。 |
| [`common.util.collection`](../src/main/java/hellfirepvp/astralsorcery/common/util/collection/package-info.java) | 交替集合、映射流和加权对象集合辅助。 |
| [`common.util.collision`](../src/main/java/hellfirepvp/astralsorcery/common/util/collision/package-info.java) | 射线遍历和自定义碰撞处理。 |
| [`common.util.color`](../src/main/java/hellfirepvp/astralsorcery/common/util/color/package-info.java) | 颜色混合、叠加与染料映射。 |
| [`common.util.concurrent`](../src/main/java/hellfirepvp/astralsorcery/common/util/concurrent/package-info.java) | 读写锁访问能力与并发辅助。 |
| [`common.util.damage`](../src/main/java/hellfirepvp/astralsorcery/common/util/damage/package-info.java) | 伤害来源包装和实体攻击辅助。 |
| [`common.util.data`](../src/main/java/hellfirepvp/astralsorcery/common/util/data/package-info.java) | 网络缓冲、JSON、名称标识、向量与数据序列化辅助。 |
| [`common.util.dispenser`](../src/main/java/hellfirepvp/astralsorcery/common/util/dispenser/package-info.java) | 流体容器的发射器行为适配。 |
| [`common.util.function`](../src/main/java/hellfirepvp/astralsorcery/common/util/function/package-info.java) | IO 与多参数函数能力契约。 |
| [`common.util.log`](../src/main/java/hellfirepvp/astralsorcery/common/util/log/package-info.java) | 分类日志与日志输出辅助。 |
| [`common.util.loot`](../src/main/java/hellfirepvp/astralsorcery/common/util/loot/package-info.java) | 掉落物收集、捕获及战利品生成。 |
| [`common.util.math`](../src/main/java/hellfirepvp/astralsorcery/common/util/math/package-info.java) | 通用数值、概率和渐进计算辅助。 |
| [`common.util.nbt`](../src/main/java/hellfirepvp/astralsorcery/common/util/nbt/package-info.java) | NBT 数据读写与 registry 序列化。 |
| [`common.util.object`](../src/main/java/hellfirepvp/astralsorcery/common/util/object/package-info.java) | 引用、侧向值与缓存对象辅助。 |
| [`common.util.order`](../src/main/java/hellfirepvp/astralsorcery/common/util/order/package-info.java) | 带依赖对象的排序契约与拓扑排序。 |
| [`common.util.reflection`](../src/main/java/hellfirepvp/astralsorcery/common/util/reflection/package-info.java) | 反射访问辅助与专用异常。 |
| [`common.util.sound`](../src/main/java/hellfirepvp/astralsorcery/common/util/sound/package-info.java) | 公共声音事件分类与播放辅助。 |
| [`common.util.tick`](../src/main/java/hellfirepvp/astralsorcery/common/util/tick/package-info.java) | 按 tick 过期的 token、映射和超时列表。 |
| [`common.util.time`](../src/main/java/hellfirepvp/astralsorcery/common/util/time/package-info.java) | 日期判断及时间冻结区域、控制器和同步描述。 |
| [`common.util.world`](../src/main/java/hellfirepvp/astralsorcery/common/util/world/package-info.java) | 天空聚能判断与世界种子缓存。 |
| [`common.world`](../src/main/java/hellfirepvp/astralsorcery/common/world/package-info.java) | 世界生成配置、模板遗迹基类与生物群系标签辅助。 |
| [`common.world.biome`](../src/main/java/hellfirepvp/astralsorcery/common/world/biome/package-info.java) | 受配置门控的生物群系特征注入。 |
| [`common.world.feature`](../src/main/java/hellfirepvp/astralsorcery/common/world/feature/package-info.java) | 替换方块和水晶矿生成特征。 |
| [`common.world.feature.config`](../src/main/java/hellfirepvp/astralsorcery/common/world/feature/config/package-info.java) | 方块替换生成特征的配置数据。 |
| [`common.world.marker`](../src/main/java/hellfirepvp/astralsorcery/common/world/marker/package-info.java) | 结构模板数据标记的处理入口。 |
| [`common.world.placement`](../src/main/java/hellfirepvp/astralsorcery/common/world/placement/package-info.java) | 概率、河床与世界条件等放置修饰。 |
| [`common.world.placement.config`](../src/main/java/hellfirepvp/astralsorcery/common/world/placement/config/package-info.java) | 概率与世界条件放置的配置描述。 |
| [`common.world.structure`](../src/main/java/hellfirepvp/astralsorcery/common/world/structure/package-info.java) | 古代、沙漠和小型神殿遗迹的结构定义。 |
| [`common.world.structure.feature`](../src/main/java/hellfirepvp/astralsorcery/common/world/structure/feature/package-info.java) | 各神殿遗迹的模板特征与生成实现。 |

### Mixin（2）

| 包 | 职责 |
| --- | --- |
| [`mixin`](../src/main/java/hellfirepvp/astralsorcery/mixin/package-info.java) | 实体、属性、区块及服务端交互等原版行为注入。 |
| [`mixin.client`](../src/main/java/hellfirepvp/astralsorcery/mixin/client/package-info.java) | 客户端世界、渲染器、物品与粒子注入。 |
