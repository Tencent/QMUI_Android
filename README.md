 <p align="center">
  <img src="https://cloud.githubusercontent.com/assets/1190261/26751376/63f96538-486a-11e7-81cf-5bc83a945207.png" width="220" height="220" alt="Banner" />
</p>

# QMUI_Android

QMUI Android 的设计目的是用于辅助快速搭建一个具备基本设计还原效果的 Android 项目，同时利用自身提供的丰富控件及兼容处理，让开发者能专注于业务需求而无需耗费精力在基础代码的设计上。不管是新项目的创建，或是已有项目的维护，均可使开发效率和项目质量得到大幅度提升。

[![QMUI Team Name](https://img.shields.io/badge/Team-QMUI-brightgreen.svg?style=flat)](https://github.com/QMUI "QMUI Team")
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://opensource.org/licenses/MIT "Feel free to contribute.")

## 功能特性
### 全局 UI 配置
只需要修改一份配置表就可以调整 App 的全局样式，包括组件颜色、导航栏、对话框、列表等。一处修改，全局生效。

### 丰富的 UI 控件
提供丰富常用的 UI 控件，例如 BottomSheet、Tab、圆角 ImageView、下拉刷新等，使用方便灵活，并且支持自定义控件的样式。

### 高效的工具方法
提供高效的工具方法，包括设备信息、屏幕信息、键盘管理、状态栏管理等，可以解决各种常见场景并大幅度提升开发效率。

## 支持 Android 版本
QMUI Android 支持 API Level 21+。

## 使用方法
可以在工程中的 qmuidemo 项目中查看各组件的使用。

## 隐私与安全
1. 框架会调用 android.os.Build 下的字段读取 brand、model 等信息，用于区分不同的设备。
2. 框架会尝试读取系统设置获取是否是全面屏手势
