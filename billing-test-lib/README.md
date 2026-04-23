# Billing Test Lib

Android 计费页面测试框架，通过注解 + KSP 编译时代码生成，提供统一的计费测试中心界面，支持多国家价格模拟、SKU 预览和所有计费页面的快速启动。

## 模块结构

```
billing-test-lib/
├── annotation/    # 注解定义（Kotlin JVM）
├── processor/     # KSP 注解处理器（Kotlin JVM）
└── runtime/       # Android 运行时库
```

## 技术方案

| 阶段 | 组件 | 技术 |
|------|------|------|
| 编译时 | annotation + processor | KSP 2.0.20 |
| 运行时 | runtime + generated code | 反射 + ContentProvider 自动初始化 |

### 工作流程

```
@BillingTestPage 注解 → KSP 处理器扫描 → 生成 BillingTestPageRegistry
→ 反射加载注册表 → BillingTestCenterActivity 展示页面列表
→ 点击启动对应计费页面
```

## Release 零开销

- `runtime` 模块仅通过 `debugImplementation` 引入，不进入 Release APK
- `@BillingTestPage` 使用 `@Retention(BINARY)`，无运行时保留
- R8/ProGuard 自动移除所有未使用的测试代码

## 核心依赖

- Kotlin 2.0.20 / Java 17
- KSP 2.0.20-1.0.24
- Android SDK 23+ / compileSdk 35
- Material Components / RecyclerView
