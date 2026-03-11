# 底色通 - 智能证件照制作小程序

<div align="center">

![底色通](https://img.shields.io/badge/底色通-智能证件照-blue?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?style=for-the-badge)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?style=for-the-badge)

一键上传，智能换底，快速生成专业证件照

</div>

---

## ✨ 项目简介

**底色通**是一款基于 UniApp + Spring Boot 的智能证件照制作小程序，支持上传照片、一键换底色、调整尺寸、美化效果，快速生成符合标准的证件照。

### 🎯 核心功能

- ✅ **智能抠图** - 基于腾讯云 AI 人像分割技术
- ✅ **一键换底** - 支持白、红、蓝、绿、灰 5 种背景色
- ✅ **标准尺寸** - 一寸、二寸、小一寸等标准证件照尺寸
- ✅ **实时预览** - 参数调整即时预览，所见即所得
- ✅ **美化效果** - 亮度调整、磨皮美颜
- ✅ **快速保存** - 一键保存到相册

### 🌟 产品优势

- 🚀 **简单易用** - 只需上传照片，自动生成证件照
- 🎨 **效果专业** - 腾讯云 AI 抠图 + 图像处理算法
- ⚡ **即时预览** - 参数调整实时预览，无需等待
- 💾 **智能缓存** - 预览结果缓存，提升用户体验
- 🔧 **性能优化** - 图片压缩、内存管理、异步处理

### 📱 在线体验

- **小程序：** 扫码或搜索"底色通"（暂未发布）
- **Web 版：** 暂未开放
- **演示视频：** [待补充]

---

## 🛠️ 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| [UniApp](https://uniapp.dcloud.net.cn/) | 最新 | 跨平台小程序开发框架 |
| [Vue 3](https://vuejs.org/) | 最新 | 渐进式 JavaScript 框架 |
| JavaScript | ES6+ | 核心语言 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| [Spring Boot](https://spring.io/projects/spring-boot) | 3.5.4 | Java 应用框架 |
| Java | 17 | 编程语言 |
| [Maven](https://maven.apache.org/) | 3.x | 项目构建工具 |

### 第三方服务

| 服务 | 用途 |
|------|------|
| [腾讯云 BDA API](https://cloud.tencent.com/product/bda) | 人像分割/智能抠图 |
| [阿里云函数计算](https://www.aliyun.com/product/fc) | 后端部署 |

---

## 📦 安装与运行

### 前置要求

- 📱 **前端：** HBuilderX 或 uni-app CLI
- ☕ **后端：** Java 17 + Maven 3.x
- 🔑 **腾讯云账号：** 开通 BDA 服务并获取 SecretId/SecretKey
- ☁️ **阿里云账号：** 函数计算（可选，用于部署）

---

### 后端开发环境搭建

#### 1. 克隆项目

```bash
git clone https://github.com/djzhhil/idphoto.git
cd idphoto/back
```

#### 2. 配置腾讯云密钥

在 `src/main/resources/application.properties` 中添加：

```properties
# 服务器配置
server.port=8081

# 腾讯云配置
tencent.secret-id=你的SecretId
tencent.secret-key=你的SecretKey
tencent.region=ap-shanghai
```

**获取腾讯云密钥：**
1. 登录 [腾讯云控制台](https://console.cloud.tencent.com/)
2. 进入 [访问管理 - API密钥管理](https://console.cloud.tencent.com/cam/capi)
3. 创建或使用现有的 SecretId/SecretKey

#### 3. 安装依赖并运行

```bash
# Maven 编译
mvn clean install

# 运行项目
mvn spring-boot:run

# 或直接使用 IDE（IntelliJ IDEA / Eclipse）运行 IdPhotoApplication
```

#### 4. 验证启动

访问 `http://localhost:8081/api/preview-idphoto`，应返回 405 方法不允许，说明服务正常启动。

---

### 前端开发环境搭建

#### 1. 安装 HBuilderX

下载并安装 [HBuilderX](https://dcloud.io/hbuilderx.html)

#### 2. 导入项目

1. 打开 HBuilderX
2. 文件 → 导入 → 从本地项目导入
3. 选择 `idphoto/front` 目录

#### 3. 配置 API 地址

编辑 `front/utils/request.js`，确保 `BASE_URL` 指向正确的后端地址：

```javascript
const ENV = {
  local: {
    baseURL: "http://localhost:8081",  // 本地开发
  },
  prod: {
    baseURL: "https://idphoto-kqffccmbmb.cn-hangzhou.fcapp.run",  // 生产环境
  },
};

// 切换环境
const BASE_URL = ENV.local.baseURL;  // 本地测试时使用 local
```

#### 4. 运行项目

1. 在 HBuilderX 中打开 `idphoto/front` 项目
2. 点击工具栏的"运行" → "运行到小程序模拟器"
3. 选择微信开发者工具（需安装）或内置浏览器模拟器

#### 5. 微信小程序调试

1. 在 `manifest.json` 中配置微信小程序 AppID
2. 点击"运行" → "运行到小程序模拟器" → "微信开发者工具"
3. 在微信开发者工具中查真机调试

---

## 📂 项目结构

```
idphoto/
├── back/                                    # 后端（Spring Boot）
│   ├── pom.xml                              # Maven 配置
│   └── src/main/
│       ├── java/com/example/idphoto/
│       │   ├── IdPhotoApplication.java     # 启动类
│       │   ├── controller/
│       │   │   └── ImageController.java    # 图片处理 API 控制器
│       │   ├── services/
│       │   │   ├── ImageProcessingService.java  # 图片处理服务
│       │   │   └── TencentClientService.java   # 腾讯云客户端
│       │   ├── config/
│       │   │   ├── CorsConfig.java         # 跨域配置
│       │   │   └── MemoryManagementConfig.java  # 内存管理配置
│       │   └── dto/
│       │       ├── ImageRequest.java       # 图片请求 DTO
│       │       ├── EditImageRequest.java   # 编辑请求 DTO
│       │       └── ImageResponse.java      # 图片响应 DTO
│       └── resources/
│           └── application.properties      # 配置文件
│
└── front/                                   # 前端（UniApp 小程序）
    ├── App.vue                              # 应用入口
    ├── main.js                              # 主入口文件
    ├── pages.json                           # 页面路由配置
    ├── manifest.json                        # 应用配置
    ├── project.config.json                  # 项目配置
    ├── pages/                               # 页面目录
    │   ├── index/
    │   │   └── index.vue                   # 首页（上传照片）
    │   ├── edit/
    │   │   └── edit.vue                    # 编辑页（调参数 + 预览）
    │   └── result/
    │       └── result.vue                  # 结果页（下载/保存）
    ├── utils/
    │   └── request.js                      # HTTP 请求工具类
    └── unpackage/                           # 编译输出目录
```

---

## 🔌 API 接口文档

### 基础信息

- **Base URL：** `http://localhost:8081/api`（本地）或 `https://idphoto-kqffccmbmb.cn-hangzhou.fcapp.run/api`（生产）
- **Content-Type：** `application/json`

---

### 1. POST /api/remove-bg - 去底色（旧接口）

**功能：** 上传照片并替换背景色

**请求参数：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "background": "white"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| image | string | 是 | 图片 Base64 编码（含 data:image/jpeg;base64, 前缀） |
| background | string | 否 | 背景颜色：white/red/blue（默认：white） |

**成功响应：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "message": "生成成功"
}
```

**失败响应：**
```json
{
  "image": "",
  "message": "生成失败：调用腾讯云抠图失败"
}
```

---

### 2. POST /api/preview-idphoto - 预览证件照（推荐）

**功能：** 预览模式证件照，支持实时调整参数，带缓存优化

**请求参数：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "size": "1inch",
  "brightness": 50,
  "smoothness": 5,
  "bgColor": "white",
  "previewMode": true,
  "quality": "quick"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| image | string | 是 | 图片 Base64 编码（含前缀） |
| size | string | 否 | 证件照尺寸：1inch/2inch/small1inch（默认：1inch） |
| brightness | int | 否 | 亮度调整：0-100（默认：50） |
| smoothness | int | 否 | 磨皮程度：0-20（默认：0） |
| bgColor | string | 否 | 背景颜色：white/red/blue/green/gray（默认：white） |
| previewMode | boolean | 否 | 预览模式（默认：true） |
| quality | string | 否 | 输出质量：quick/high（默认：quick） |

**成功响应：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "message": "预览生成成功(缓存)"
}
```

**失败响应：**
```json
{
  "image": "",
  "message": "预览生成失败：内存不足，请尝试较小尺寸的图片"
}
```

---

### 3. POST /api/generate-idphoto - 生成证件照（高质量）

**功能：** 高质量模式证件照生成，适合最终下载

**请求参数：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "size": "1inch",
  "brightness": 50,
  "smoothness": 5,
  "bgColor": "white"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| image | string | 是 | 图片 Base64 编码（含前缀） |
| size | string | 否 | 证件照尺寸：1inch/2inch/small1inch（默认：1inch） |
| brightness | int | 否 | 亮度调整：0-100（默认：50） |
| smoothness | int | 否 | 磨皮程度：0-20（默认：0） |
| bgColor | string | 否 | 背景颜色：white/red/blue/green/gray（默认：white） |

**成功响应：**
```json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "message": "生成成功"
}
```

---

## 🎨 功能特性详解

### 1. 智能抠图

基于腾讯云 AI 人像分割技术，自动识别照片中的人物区域，去除原背景。

- **算法：** 深度学习人像分割模型
- **准确率：** > 95%（正面免冠照片）
- **响应时间：** < 2 秒

### 2. 尺寸调整

支持多种标准证件照尺寸：

| 尺寸名称 | 分辨率（像素） | 用途 |
|---------|---------------|------|
| 一寸 | 295 x 413 | 常规格式身份证、求职简历 |
| 二寸 | 413 x 579 | 护照、签证 |
| 小一寸 | 260 x 378 | 驾驶证、工作证 |

**智能裁剪：**
- 自动计算最佳裁剪区域
- 保持人像居中
- 保存面部完整

### 3. 美化效果

#### 亮度调整
- 范围：0-100%（默认：50%）
- 0%：最暗
- 100%：最亮
- 使用 RescaleOp 算法实现

#### 磨皮美颜
- 范围：0-20（默认：0）
- 0：无磨皮
- 20：最大磨皮效果
- 使用 ConvolveOp 模糊滤镜实现

### 4. 背景颜色

| 颜色 | 应用场景 |
|------|---------|
| 白色 | 证件照通用底色 |
| 红色 | 工作证、学历证书 |
| 蓝色 | 签证、护照 |
| 绿色 | 部分特殊证件 |
| 灰色 | 护照背景色 |

### 5. 性能优化

#### 图片缓存
- 基于内存缓存（最大 50 张）
- 缓存键：基于图片内容 + 尺寸 + 参数生成
- 自动清理旧缓存

#### 内存管理
- OutOfMemoryError 处理
- 自动 GC 触发
- 支持低质量/高质量两种输出模式

#### 图片压缩
- 前端上传时自动压缩（compressed 模式）
- 后端支持 JPEG 压缩参数调整（0.5-0.95）

---

## 🚢 部署指南

### 后端部署

#### 方案一：阿里云函数计算（FC）

1. **准备 JAR 包**
   ```bash
   cd back
   mvn clean package -DskipTests
   ```

2. **上传到阿里云函数计算**
   - 创建函数：Java 运行时
   - 上传 JAR 包：`target/IdPhoto-0.0.1-SNAPSHOT.jar`
   - 配置环境变量：`TENCENT_SECRET_ID`、`TENCENT_SECRET_KEY`、`TENCENT_REGION`
   - 配置触发器：HTTP 触发器

3. **获取访问地址**
   - 部署成功后，阿里云会提供 HTTP 访问地址
   - 格式：`https://{function-id}.{region}.fcapp.run`

#### 方案二：服务器部署

1. **上传 JAR 包到服务器**
   ```bash
   scp target/IdPhoto-0.0.1-SNAPSHOT.jar user@server:/path/to/deploy/
   ```

2. **启动服务**
   ```bash
   java -jar IdPhoto-0.0.1-SNAPSHOT.jar --server.port=8081
   ```

3. **使用 Nginx 反向代理（可选）**
   ```nginx
   server {
       listen 80;
       server_name idphoto.example.com;

       location /api/ {
           proxy_pass http://localhost:8081/api/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

4. **使用 systemd 管理服务（可选）**
   ```bash
   # 创建 service 文件
   sudo nano /etc/systemd/system/idphoto.service

   # 内容：
   [Unit]
   Description=IdPhoto Service
   After=network.target

   [Service]
   ExecStart=/usr/bin/java -jar /path/to/IdPhoto-0.0.1-SNAPSHOT.jar
   Restart=always
   User=www-data

   [Install]
   WantedBy=multi-user.target

   # 启动服务
   sudo systemctl start idphoto
   sudo systemctl enable idphoto
   ```

---

### 前端部署

#### 微信小程序

1. **配置 AppID**
   - 打开 `manifest.json`
   - 点击"微信小程序配置"
   - 填入小程序 AppID

2. **上传代码**
   - HBuilderX → 发行 → 小程序-微信
   - 登录微信开发者工具
   - 上传代码

3. **提交审核**
   - 微信公众平台后台
   - 版本管理 → 提交审核
   - 填写审核信息

4. **发布上线**
   - 审核通过后发布

#### 其他小程序平台

- **支付宝小程序**：发行 → 小程序-支付宝
- **百度小程序**：发行 → 小程序-百度
- **抖音小程序**：发行 → 小程序-抖音
- **H5 Web版**：发行 → H5

---

## 📊 开发计划

### ✅ 已完成（V1.0 基础版）

- [x] 图片上传功能
- [x] 腾讯云抠图集成
- [x] 尺寸调整（1寸、2寸、小一寸）
- [x] 背景颜色替换（5种颜色）
- [x] 亮度调整
- [x] 磨皮功能
- [x] 预览和下载
- [x] 跨域配置
- [x] 图片缓存
- [x] 内存优化

### ⏳ 进行中（V1.1 增强版）

- [ ] 自动居中裁剪优化
- [ ] 加载动画/进度条
- [ ] 历史记录云端同步
- [ ] 异步处理优化
- [ ] 性能监控

### 🔜 待开发（V2.0 专业版）

- [ ] AI 换装（衬衫、西装）
- [ ] 背景自定义（HEX 或调色盘）
- [ ] 批量生成多种底色
- [ ] 云端历史记录
- [ ] 用户系统（微信登录、OpenID 绑定）
- [ ] OpenCV 集成（高级图像处理）

### 💰 待开发（V3.0 商业版）

- [ ] 会员中心页面
- [ ] 支付集成（微信支付）
- [ ] 广告插屏
- [ ] 分享裂变（好友助力）
- [ ] 积分/次数系统
- [ ] 日志与监控系统

---

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出建议！

### 贡献流程

1. **Fork 项目** → GitHub 上 Fork 本仓库
2. **创建分支** → `git checkout -b feature/your-feature`
3. **提交更改** → `git commit -m 'feat: 添加新功能'`
4. **推送分支** → `git push origin feature/your-feature`
5. **创建 Pull Request** → 在 GitHub 上提交 PR

### Commit 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat:` 新功能
- `fix:` 修复 Bug
- `docs:` 文档更新
- `style:` 代码格式调整
- `refactor:` 重构
- `test:` 测试相关
- `chore:` 构建/工具链更新

**示例：**
```bash
git commit -m "feat: 添加批量生成多底色功能"
git commit -m "fix: 修复亮度调整不生效的问题"
```

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议。

---

## 🙏 致谢

感谢以下第三方服务和框架：

- [腾讯云 BDA API](https://cloud.tencent.com/product/bda) - 智能人像分割
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 应用框架
- [UniApp](https://uniapp.dcloud.net.cn/) - 跨平台开发框架
- [Vue 3](https://vuejs.org/) - 渐进式 JavaScript 框架
- [阿里云函数计算](https://www.aliyun.com/product/fc) - 无服务器计算平台

---

## 📞 联系方式

- **作者：** djzhhil
- **GitHub：** https://github.com/djzhhil/idphoto
- **问题反馈：** [GitHub Issues](https://github.com/djzhhil/idphoto/issues)

---

## ⚠️ 注意事项

1. **腾讯云配额：** 腾讯云 BDA API 有免费配额，超出需付费
2. **图片大小：** 建议上传图片不超过 5MB
3. **网络环境：** 抠图 API 调用需要网络连接
4. **隐私保护：** 用户照片不会存储在服务器端

---

## 📝 更新日志

### v1.0.0 (2026-03-10)

**发布内容：**
- ✅ 基础版上线
- ✅ 支持一键换底色
- ✅ 支持标准证件照尺寸
- ✅ 支持亮度和磨皮调整
- ✅ 部署到阿里云函数计算

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！**

Made with ❤️ by djzhhil

</div>
