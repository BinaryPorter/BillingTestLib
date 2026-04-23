# 内购测试中心公共方案

## 2.1 背景

### 现状

应用内存在 20+ 个内购页面，覆盖订阅（月/季/年/终身）、促销（春季/圣诞/黑五/新年/季中/小促）、挽留、折扣等多种场景。各页面的启动方式不统一，部分需要通过 Extra 传参（如折扣比例），测试时需要手动构造 Intent 逐个验证。

### 存在的问题

1. **缺少统一入口**：测试人员需要记忆每个内购页面的类名和所需参数，无法快速定位和启动目标页面
2. **多市场验证成本高**：产品支持 15+ 个国家/地区市场，每个市场的 SKU 和定价不同。切换国家验证需要借助 Play Billing Lab 或修改代码，流程繁琐
3. **SKU 不可见**：当前国家/档位下的实际 SKU ID、价格、货币符号等信息没有集中展示的入口，排查定价问题时需要打断点或加日志
4. **回归测试效率低**：每次新增内购页面后，QA 需要手动更新测试用例的跳转路径，缺少自动化收集机制

### 痛点

| 角色 | 痛点 |
|------|------|
| QA | 找不到页面入口，记不住传参要求，多市场切换耗时长 |
| 开发 | 新增页面后需手动维护测试入口，容易遗漏 |
| 产品 | 无法直观查看各市场的定价差异 |

---

## 2.2 目的

本方案旨在提供一个**编译时自动收集、运行时零侵入**的内购测试框架，达成以下目标：

1. **统一入口**：一个测试中心页面聚合所有内购页面，按分类分组展示，点击即可启动
2. **多市场模拟**：内置 15 个主流市场，一键切换国家代码，自动刷新 SKU 和价格
3. **SKU 透明化**：实时展示当前国家/档位下所有 SKU 的类型、价格、ID
4. **Release 零开销**：通过 Gradle 变体隔离 + 注解 `BINARY` 保留策略，确保 Release 包中不含任何测试代码
5. **新页面零配置**：只需一个注解，新页面自动出现在测试中心，无需手动维护列表

---

## 2.3 方案介绍

### 2.3.1 框架

```
billing-test-lib/
├── annotation/     注解定义模块（Kotlin JVM，无 Android 依赖）
│   └── @BillingTestPage      标记内购页面的注解
│       BillingTestPageEntry  运行时数据模型
│
├── processor/      KSP 注解处理器模块（Kotlin JVM）
│   └── BillingTestPageProcessor        扫描注解，生成注册表代码
│       BillingTestPageProcessorProvider  KSP SPI 入口
│
└── runtime/        Android 运行时模块（仅 debugImplementation）
    ├── BillingTest                   库初始化入口
    ├── BillingTestContract           项目桥接契约（接口）
    │   SkuInfo                      SKU 展示数据模型
    ├── BillingIntentFactory          自定义 Intent 工厂接口
    ├── BillingTestPageRegistryProvider 反射加载生成的注册表
    ├── PageLauncher                  页面启动器
    ├── PlayBillingLabHelper          Play Billing Lab 快捷跳转
    ├── data/Countries                国家/地区数据
    └── ui/BillingTestCenterActivity  测试中心主界面
```

三模块的 Gradle 依赖关系：

```
app 模块
  ├── implementation   annotation     ← 所有变体可访问注解
  ├── debugImplementation runtime     ← 仅 debug APK 包含运行时
  └── ksp              processor      ← 编译时执行，生成代码

processor → annotation（读取注解定义）
runtime   → annotation（使用 BillingTestPageEntry 数据类）
```

### 2.3.2 思路

整体思路分为两个阶段：

**编译时** — 注解收集与代码生成

通过 KSP（Kotlin Symbol Processing）在编译期扫描所有标记了 `@BillingTestPage` 的类，提取注解参数（name、category、description、intentFactory），生成一个 `BillingTestPageRegistry` 单例对象。该对象包含一个 `pages: List<BillingTestPageEntry>` 字段，列出所有注册的页面信息。

这一步的核心价值是**自动化**：开发者新增内购页面时，只需加一个注解，无需修改任何其他文件，构建后页面自动出现在测试中心。

**运行时** — 注册表加载与页面启动

测试中心 Activity 通过 `BillingTestPageRegistryProvider` 以反射方式加载生成的 `BillingTestPageRegistry`，获取页面列表并按 category 分组展示。用户点击某个页面时，`PageLauncher` 根据配置启动 Activity：
- 无 `intentFactory`：直接通过类名反射创建 Intent 并启动
- 有 `intentFactory`：通过类名反射实例化工厂对象，调用其 `createIntent()` 获取带参数的 Intent

同时，通过 `BillingTestContract` 契约接口桥接项目自身的国家代码切换和 SKU 初始化逻辑，实现测试中心与业务代码的解耦。

### 2.3.3 分模块介绍

#### annotation 模块

**原理**：纯 Kotlin JVM 模块，定义两个类。

- `@BillingTestPage` — 类级注解，`@Retention(BINARY)` 保留到 class 文件但运行时不可见。参数包括 `name`（显示名）、`category`（分类）、`description`（描述）、`intentFactory`（自定义 Intent 工厂全限定类名）
- `BillingTestPageEntry` — data class，作为编译生成代码和运行时之间的数据传输对象

**交互**：被 app 模块和 processor 模块共同依赖。app 模块用它标记 Activity，processor 模块在编译期读取它。

#### processor 模块

**原理**：KSP 注解处理器，通过 Java SPI（`META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`）注册到 KSP 编译管线。

处理流程：
1. KSP 插件在 `ksp<Variant>Kotlin` 任务执行时，通过 `ServiceLoader` 加载 `BillingTestPageProcessorProvider`
2. 调用 `Provider.create(environment)` 创建 `BillingTestPageProcessor` 实例
3. 调用 `Processor.process(resolver)` 扫描所有带 `@BillingTestPage` 注解的类声明
4. 提取注解参数，构建 `PageEntry` 列表
5. 通过 `codeGenerator.createNewFile()` 生成 `com.billing.test.generated.BillingTestPageRegistry` 源码文件
6. 返回 `emptyList()` 表示所有符号已处理完毕，无需追加轮次

**生成产物示例**（`app/build/generated/ksp/.../BillingTestPageRegistry.kt`）：

```kotlin
package com.billing.test.generated

object BillingTestPageRegistry {
    val pages: List<BillingTestPageEntry> = listOf(
        BillingTestPageEntry(
            name = "Spring Sale",
            category = "Promotion",
            activityClassName = "com.go.fasting.billing.promotion.activity.BillingSpringSaleActivity",
            intentFactoryClassName = null
        ),
        // ... 其他页面
    )
}
```

**交互**：仅在编译时执行，不参与运行时。输出文件会被 app 模块的 Kotlin 编译器自动纳入编译路径。

#### runtime 模块

**BillingTest（初始化入口）**

```kotlin
object BillingTest {
    fun init(contract: BillingTestContract)  // Application.onCreate() 或 ContentProvider 中调用
}
```

接收项目实现的 `BillingTestContract`，保存为全局单例。所有运行时功能通过该 contract 桥接项目逻辑。

**BillingTestContract（项目桥接契约）**

```kotlin
interface BillingTestContract {
    fun setCountryCode(code: String)           // 设置调试国家代码
    fun getCountryCode(): String               // 获取当前调试国家代码
    fun initSkuID()                            // 按国家+年龄初始化 SKU
    fun refreshSkuPrice(activity: Activity)    // 从 Billing 服务刷新价格
    fun getSkuList(): List<SkuInfo>            // 返回当前 SKU 列表（可选）
}
```

这是框架与业务代码唯一的接触面。项目在 `app/src/debug/` 中实现此接口，将国家切换、SKU 初始化等逻辑桥接过来。

**BillingTestPageRegistryProvider（注册表加载）**

```kotlin
object BillingTestPageRegistryProvider {
    fun getPages(): List<BillingTestPageEntry> {
        val registryClass = Class.forName("com.billing.test.generated.BillingTestPageRegistry")
        val pagesField = registryClass.getDeclaredField("pages")
        return pagesField.get(null) as List<BillingTestPageEntry>
    }
}
```

通过反射加载 KSP 生成的 `BillingTestPageRegistry` 对象，读取 `pages` 字段。反射失败时返回空列表（不会崩溃）。

**PageLauncher（页面启动器）**

根据 `BillingTestPageEntry` 的 `intentFactoryClassName` 字段决定启动方式：

- `null`：`Class.forName(activityClassName)` → `Intent(context, clazz)` → `startActivity`
- 非空：`Class.forName(intentFactoryClassName)` → 实例化为 `BillingIntentFactory` → 调用 `createIntent(context)` 获取 Intent → `startActivity`

**BillingTestCenterActivity（测试中心界面）**

功能区域：
1. **国家切换区** — Spinner 下拉选择 + 横向 Chip 快速选择，内置 15 个市场（US/KR/DE/FR/GB/CA/IT/BR/ES/ID/IN/TH/PH/RO/JP）
2. **SKU 预览区** — 展示当前国家/档位下的 SKU 类型、价格、ID
3. **页面列表区** — 按 category 分组展示所有 `@BillingTestPage` 标记的页面，点击启动
4. **Play Billing Lab** — 快捷跳转 Google 官方计费测试工具
5. **Reset** — 重置国家设置并刷新 SKU

**PlayBillingLabHelper**

封装 Google Play Billing Lab（`com.google.android.apps.play.billingtestcompanion`）的检测与跳转逻辑。未安装时引导用户前往 Play Store 下载。

### 2.3.4 接入方法

#### 第一步：模块引入

`settings.gradle` 添加三个子模块：

```gradle
include ':billing-test-lib:annotation'
include ':billing-test-lib:processor'
include ':billing-test-lib:runtime'
```

`app/build.gradle` 添加依赖：

```gradle
plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    implementation project(':billing-test-lib:annotation')
    debugImplementation project(':billing-test-lib:runtime')
    ksp project(':billing-test-lib:processor')
}
```

#### 第二步：实现桥接契约

在 `app/src/debug/java/` 中创建 `BillingTestContract` 的实现：

```kotlin
class AppBillingTestContract : BillingTestContract {

    override fun setCountryCode(code: String) {
        // 桥接到项目的国家代码存储
        UserPreferences.debugCurrentCounty = code
    }

    override fun getCountryCode(): String {
        return UserPreferences.debugCurrentCounty
    }

    override fun initSkuID() {
        BillingSkuIDUtils.initSkuID()
    }

    override fun refreshSkuPrice(activity: Activity) {
        BillingManager(activity).getSkuPrice()
    }

    override fun getSkuList(): List<SkuInfo> {
        return listOf(
            SkuInfo("月度", getSkuId(MONTH), getPrice(MONTH), getSymbol(MONTH)),
            SkuInfo("年度", getSkuId(YEAR), getPrice(YEAR), getSymbol(YEAR))
        )
    }
}
```

#### 第三步：初始化

推荐使用 ContentProvider 自动初始化，无需修改 Application 代码。

`app/src/debug/AndroidManifest.xml`：

```xml
<provider
    android:name=".billing.BillingTestInitProvider"
    android:authorities="${applicationId}.billing.test.init"
    android:exported="false"
    android:initOrder="100" />
```

`app/src/debug/java/.../billing/BillingTestInitProvider.kt`：

```kotlin
class BillingTestInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        BillingTest.init(AppBillingTestContract())
        return true
    }
    // query/insert/delete/update 返回 null/0 即可
}
```

#### 第四步：标记内购页面

在 Activity 类上添加注解：

```kotlin
@BillingTestPage(name = "Spring Sale", category = "Promotion")
class BillingSpringSaleActivity : BaseActivity() { ... }
```

需要传参的页面，额外指定 `intentFactory` 并在 debug 源码集中实现工厂：

```kotlin
@BillingTestPage(
    name = "Timeline 60%",
    category = "Discount",
    intentFactory = "com.go.fasting.billing.Timeline60Factory"
)
class VipBillingActivityTimeline : BaseActivity() { ... }

// app/src/debug/java/.../billing/Timeline60Factory.kt
class Timeline60Factory : BillingIntentFactory {
    override fun createIntent(context: Context) =
        Intent(context, VipBillingActivityTimeline::class.java).apply {
            putExtra("vipDiscount", "60")
        }
}
```

#### 第五步：添加调试入口

在 DebugActivity 中添加跳转：

```kotlin
// 使用反射避免 Release 编译依赖
try {
    val entryClass = Class.forName("com.go.fasting.activity.debug.BillingTestCenterEntry")
    // ... 反射调用 setup 方法
} catch (_: Exception) { }
```

#### 接入 Checklist

- [ ] `settings.gradle` 添加三个子模块
- [ ] `app/build.gradle` 添加 KSP 插件和三行依赖
- [ ] `app/src/debug/` 实现 `BillingTestContract`
- [ ] `debug/AndroidManifest.xml` 注册 `ContentProvider`
- [ ] 在内购 Activity 上添加 `@BillingTestPage` 注解
- [ ] 需要传参的页面实现 `BillingIntentFactory`
- [ ] DebugActivity 中添加跳转入口
- [ ] 构建 debug APK 验证测试中心正常显示