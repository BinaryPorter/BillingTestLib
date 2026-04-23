# Billing Test Lib - 接入文档

## 概述

`billing-test-lib` 提供了一套计费页面测试框架，通过注解自动收集所有计费页面，生成统一的测试中心界面，支持切换国家/地区模拟不同市场的价格和 SKU。

---

## 一、模块依赖配置

### 1.1 settings.gradle

```gradle
include ':billing-test-lib:annotation'
include ':billing-test-lib:processor'
include ':billing-test-lib:runtime'
```

### 1.2 app/build.gradle

```gradle
plugins {
    id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}

android {
    // ...
}

dependencies {
    // 注解 - 标记计费页面（所有变体可用）
    implementation project(':billing-test-lib:annotation')

    // 运行时 - 仅 debug 构建包含
    debugImplementation project(':billing-test-lib:runtime')

    // KSP 处理器 - 编译时生成注册表
    ksp project(':billing-test-lib:processor')
}
```

---

## 二、实现集成契约

在 `app/src/debug/` 中创建 `BillingTestContract` 的实现类，桥接项目自身的计费逻辑。

### 2.1 必须实现的方法

| 方法 | 用途 |
|------|------|
| `setCountryCode(code: String)` | 设置调试国家代码到项目存储 |
| `getCountryCode(): String` | 读取当前调试国家代码 |
| `initSkuID()` | 根据国家+年龄初始化 SKU ID |
| `refreshSkuPrice(activity: Activity)` | 从 Play Billing 刷新 SKU 价格 |

### 2.2 可选实现的方法

| 方法 | 用途 |
|------|------|
| `getSkuList(): List<SkuInfo>` | 返回当前国家/档位的 SKU 列表，用于 UI 预览 |

### 2.3 示例实现

```kotlin
// app/src/debug/java/.../billing/GoFastingBillingTestContract.kt
class GoFastingBillingTestContract : BillingTestContract {

    override fun setCountryCode(code: String) {
        App.instance.userPrefs.debugCurrentCounty = code
    }

    override fun getCountryCode(): String {
        return App.instance.userPrefs.debugCurrentCounty
    }

    override fun initSkuID() {
        BillingSkuIDUtils.initSkuID()
    }

    override fun refreshSkuPrice(activity: Activity) {
        BillingManager(activity).getSkuPrice()
    }

    override fun getSkuList(): List<SkuInfo> {
        val types = listOf(
            BillingManager.TYPE_MONTH to "TYPE_MONTH",
            BillingManager.TYPE_YEAR to "TYPE_YEAR",
        )
        return types.map { (type, name) ->
            SkuInfo(
                typeName = name,
                skuId = BillingManager.getProductIdByType(type),
                price = VipBillingUtils.getPrice(type),
                currencySymbol = VipBillingUtils.getPriceSymbol(type)
            )
        }
    }
}
```

---

## 三、初始化

### 3.1 方式一：ContentProvider 自动初始化（推荐）

无需修改 `Application` 代码，利用 ContentProvider 在 `Application.onCreate()` 之前自动执行。

**AndroidManifest.xml**（debug 源码集）：

```xml
<application>
    <provider
        android:name=".billing.init.BillingTestInitProvider"
        android:authorities="${applicationId}.billing.test.init"
        android:exported="false"
        android:initOrder="100" />
</application>
```

**ContentProvider 实现**：

```kotlin
class BillingTestInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        BillingTest.init(GoFastingBillingTestContract())
        return true
    }
    override fun query(...): Cursor? = null
    override fun getType(...): String? = null
    override fun insert(...): Uri? = null
    override fun delete(...): Int = 0
    override fun update(...): Int = 0
}
```

### 3.2 方式二：手动初始化

在 `Application.onCreate()` 中调用：

```kotlin
BillingTest.init(GoFastingBillingTestContract())
```

> 注意：需使用 `if (BuildConfig.DEBUG)` 包裹，确保 Release 不执行。

---

## 四、标记内购页面

使用 `@BillingTestPage` 注解标记需要出现在测试中心的 Activity。

### 4.1 基本用法

```kotlin
@BillingTestPage(name = "Spring Sale", category = "Promotion")
class BillingSpringSaleActivity : BaseActivity() { ... }
```

### 4.2 使用自定义 Intent 工厂

当页面需要通过 Extra 传参时：

```kotlin
@BillingTestPage(
    name = "Timeline 60%",
    category = "Discount",
    intentFactory = "com.go.fasting.billing.Timeline60Factory"
)
class VipBillingActivityTimeline : BaseActivity() { ... }
```

### 4.3 注解参数说明

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | String | 是 | 在测试中心列表显示的名称 |
| `category` | String | 否 | 分类名称，默认 `"OTHER"`，用于分组显示 |
| `description` | String | 否 | 页面描述，默认空 |
| `intentFactory` | String | 否 | 自定义 Intent 工厂类的全限定名 |

### 4.4 Intent 工厂实现

在 `app/src/debug/` 中实现 `BillingIntentFactory` 接口：

```kotlin
class Timeline60Factory : BillingIntentFactory {
    override fun createIntent(context: Context): Intent {
        return Intent(context, VipBillingActivityTimeline::class.java).apply {
            putExtra("vipDiscount", "60")
        }
    }
}
```

---

## 五、添加调试入口

在现有的 DebugActivity（或其他调试页面）中添加跳转入口。

### 5.1 创建入口设置（debug 源码集）

```kotlin
// app/src/debug/java/.../activity/debug/BillingTestCenterEntry.kt
object BillingTestCenterEntry {
    fun setup(view: View) {
        val button = view.findViewById<View>(R.id.billing_test_center)
        button?.setOnClickListener {
            val context = view.context
            context.startActivity(
                Intent(context, BillingTestCenterActivity::class.java)
            )
        }
    }
}
```

### 5.2 在 DebugActivity 中调用（反射方式，避免 Release 依赖）

```kotlin
// 在 DebugActivity 的 initView() 中
try {
    val entryClass = Class.forName("com.go.fasting.activity.debug.BillingTestCenterEntry")
    val instance = entryClass.getDeclaredField("INSTANCE").get(null)
    val setupMethod = entryClass.getDeclaredMethod("setup", View::class.java)
    setupMethod.invoke(instance, binding.root)
} catch (e: Exception) {
    // Release 构建中忽略
}
```

---

## 六、测试中心功能

测试中心（`BillingTestCenterActivity`）提供以下功能：

### 6.1 国家/地区切换

- 下拉选择器选择国家
- 横向滚动 Chip 快速选择常用国家
- 点击 "Apply" 应用国家并刷新 SKU

内置 15 个市场：US、KR、DE、FR、GB、CA、IT、BR、ES、ID、IN、TH、PH、RO、JP

### 6.2 SKU 价格预览

如果 `getSkuList()` 返回非空列表，会显示当前国家/档位的所有 SKU 信息：
- 类型名称（如 TYPE_MONTH、TYPE_YEAR）
- 价格 + 货币符号
- SKU ID

### 6.3 计费页面列表

所有标记了 `@BillingTestPage` 的页面按分类分组显示，点击即可启动。

### 6.4 Play Billing Lab

提供快捷入口跳转到 Google Play Billing Lab 应用，用于修改国家。

### 6.5 Reset 按钮

重置当前国家设置并重新展示 SKU。

---

## 七、完整接入 Checklist

- [ ] `settings.gradle` 添加三个子模块
- [ ] `app/build.gradle` 添加 KSP 插件和依赖
- [ ] 实现 `BillingTestContract`（`app/src/debug/`）
- [ ] 通过 ContentProvider 或手动方式初始化
- [ ] `debug/AndroidManifest.xml` 注册 ContentProvider（如使用方式一）
- [ ] 在计费 Activity 上添加 `@BillingTestPage` 注解
- [ ] 需要传参的页面实现 `BillingIntentFactory`
- [ ] DebugActivity 中添加跳转入口
- [ ] 构建 debug APK 验证测试中心正常显示

---

## 八、扩展

### 添加新国家

修改 `runtime` 模块中的 `CountryInfo.kt`：

```kotlin
CountryInfo("cn", "China", "\uD83C\uDDE8\uD83C\uDDF3", "CNY")
```

### 添加新页面

在 Activity 类上添加注解即可，无需修改其他代码：

```kotlin
@BillingTestPage(name = "Summer Sale", category = "Promotion")
class BillingSummerSaleActivity : BaseActivity() { ... }
```

重新构建后页面会自动出现在测试中心。
