# OneTap 一键通

<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" height="120" alt="OneTap Logo">
  <br>
  <h3>为老年人量身定制的简化版Android桌面应用</h3>
  <p>让科技更贴心，让操作更简单</p>
</div>

## 📱 项目简介

OneTap（一键通）是一款专为老年人设计的Android桌面启动器应用。它简化了智能手机的操作界面，提供大字体、高对比度的显示效果，让老年人能够轻松使用智能手机的基本功能。

### ✨ 主要特性

- 🏠 **简化桌面**：替代系统桌面，提供简洁明了的主界面
- 👥 **联系人管理**：大头像显示，一键打微信视频、微信语音、拨打电话
- 📱 **常用应用**：快速访问常用APP，支持自定义排序
- 🌤️ **天气显示**：实时显示当前天气和农历日期
- 🔊 **语音辅助**：支持语音反馈，帮助视力不佳的用户
- 🎨 **主题设置**：支持蓝色/橙色主题，高对比度模式
- ♿ **无障碍设计**：大字体、高对比度、简化操作

## 🏗️ 技术架构

### 开发环境
- **开发语言**：Kotlin
- **最小SDK版本**：API 24 (Android 7.0)
- **目标SDK版本**：API 35 (Android 15)
- **编译SDK版本**：API 35

### 核心技术栈
- **UI框架**：Jetpack Compose + Material3
- **架构模式**：MVVM + Repository Pattern
- **依赖注入**：Dagger Hilt
- **数据库**：Room Database
- **数据存储**：DataStore Preferences
- **网络请求**：Retrofit + OkHttp
- **图片加载**：Coil
- **异步处理**：Kotlin Coroutines
- **导航**：Navigation Compose

### 项目结构
```
app/src/main/java/tech/huangsh/onetap/
├── data/                    # 数据层
│   ├── local/              # 本地数据存储
│   ├── model/              # 数据模型
│   ├── remote/             # 网络API
│   └── repository/         # 数据仓库
├── di/                     # 依赖注入模块
├── service/                # 系统服务
├── ui/                     # UI层
│   ├── activity/           # Activity
│   ├── screens/            # Compose屏幕
│   └── theme/              # 主题配置
├── utils/                  # 工具类
└── viewmodel/              # ViewModel
```

## 🚀 功能模块

### 1. 主界面 (HomeScreen)
- 显示当前时间、日期、星期
- 显示农历日期
- 实时天气信息
- 联系人快速拨号网格
- 常用应用快速启动

### 2. 联系人管理
- 添加/编辑/删除联系人
- 支持头像设置
- 拖拽排序功能
- 从系统通讯录导入
- 支持电话、微信语音、微信视频通话

### 3. 应用管理
- 扫描已安装应用
- 自定义常用应用列表
- 应用分类显示
- 拖拽排序

### 4. 设置功能
- 语音设置（语音反馈、语速、音量）
- 显示设置（字体大小、对比度）
- 主题设置（蓝色/橙色主题）
- 开机自启动设置

### 5. 微信集成
- 微信视频通话
- 微信语音通话
- 无障碍服务支持

## 📋 权限说明

应用需要以下权限来提供完整功能：

- **电话权限**：拨打电话功能
- **联系人权限**：读取和管理联系人
- **相机权限**：拍摄联系人头像
- **存储权限**：保存图片和数据
- **位置权限**：获取天气信息
- **网络权限**：获取天气数据
- **悬浮窗权限**：显示提醒窗口
- **无障碍服务**：微信自动化操作
- **开机启动权限**：自动启动应用

## 🛠️ 开发设置

### 环境要求
- Android Studio 2024.2.1 或更高版本
- JDK 8 或更高版本
- Android SDK API 35
- Gradle 8.10.0

### 构建项目
1. 克隆项目到本地
```bash
git clone https://github.com/yourusername/OneTap.git
cd OneTap
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖
```bash
./gradlew build
```

4. 运行项目
```bash
./gradlew installDebug
```

### 主要依赖版本
- Kotlin: 2.0.21
- Compose BOM: 2024.09.00
- Hilt: 2.56.2
- Room: 2.6.1
- Retrofit: 2.9.0
- Navigation: 2.7.5

## 📦 安装说明

### 从源码构建
1. 确保满足开发环境要求
2. 构建Release版本：
```bash
./gradlew assembleRelease
```
3. 安装APK文件到设备

### 首次使用
1. 安装后首次启动会请求必要权限
2. 建议设置为默认桌面应用
3. 根据使用习惯配置联系人和常用应用

## 🤝 贡献指南

欢迎为OneTap项目贡献代码！

### 贡献流程
1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

### 代码规范
- 遵循Kotlin编码规范
- 使用有意义的变量和函数命名
- 添加适当的注释
- 确保代码通过所有测试

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

## 🙏 Thanks

- [wechat_video_call](https://github.com/davidche1116/wechat_video_call)

---

<div align="center">
  <p>让科技更有温度，让每一次触碰都充满关怀 ❤️</p>
</div>
