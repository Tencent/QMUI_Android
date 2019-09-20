# QMUI 2.x 重要变更

## 概述

1. 升级到 Androidx
2. minSdkVersion 升级到 API 16
3. 提供换肤(Dark Mode)支持
4. `QMUI.Compat` 主题改名为 `QMUI`，并移除旧的 `QMUI` 主题

## 组件变更

### QMUITopBar

- 使用 `QMUIQQFaceView` 来承载标题和副标题
- 更改父类为 `QMUIRelativeLayout`，使用 QMUILayout 的分割线实现，移除配置项：`qmui_topbar_need_separator`，`qmui_topbar_separator_height`， `qmui_topbar_separator_color`。
- 移除配置项 `qmui_topbar_bg_color`, 使用官方实现`android:background` 替代。

### QMUIQQFaceView

- 可以通过 `QMUIQQFaceCompiler.setDefaultQQFaceManager()` 设置 QQFaceManager, App 不再需要定义一个子类。

### QMUITabSegment

- 完全重构，与旧版本完全不兼容

### QMUIPopup

- 完全重构，与旧版本完全不兼容

