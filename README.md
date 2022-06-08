# NP Android 接入文档

## 安装 SDK

### 1. 根 `build.gradle` 文件配置仓库

```gradle
repositories {
  # ...
  maven { url 'https://jitpack.io' }
}
```

### 2. 需要依赖的 module 的 `build.gradle` 添加依赖

```gradle
implementation 'com.github.arksjs:Np-Android:v1.0.0'
```

## 集成

### 1. 在 `AndroidManifest.xml` 添加网络请求

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. 初始化 SDK

```java
public static void init(Application app, String appKey)
```

如：

```java
GreeNp.init(Application.this, "APP_KEY");
```

### 3. 查看日志输出

可以通过查询 tag=GreeNp 来获取 SDK 的打印日志

## 高级功能

1. 自定义事件统计

自定义事件需要在后台进行配置，具体可以查看产品说明书。

| 参数  | 类型              | 必需 | 默认值 | 描述                                                                                         |
| ----- | ----------------- | ---- | ------ | -------------------------------------------------------------------------------------------- |
| key   | String            | 是   |        | 描述用户的动作名称，不超过 255 个字符，支持特殊字符                                          |
| value | String/JSONObject | 否   |        | 当值为 JSONObject 类型时，每个 key 长度不能超过 255 个字符，其携带 key 的个数不能超过 100 个 |

使用方式：

```java
JSONObject json = new JSONObject();
try {
    json.put("id", 3);
    json.put("name", "tom");
} catch (Exception e) {
    e.printStackTrace();
}

// 上报事件
GreeNp.trackEvent("event_name", json);  // 属性为 JSONObject
GreeNp.trackEvent("event_name", "1");   // 属性为 stirng
GreeNp.trackEvent("event_name");        // 无属性
```

2. 搜索词统计

获取系统或网站的访客搜索关键词数据。

```java
GreeNp.trackSearch(searchWord)
```

| 参数       | 类型   | 必需 | 默认值 | 描述                                    |
| ---------- | ------ | ---- | ------ | --------------------------------------- |
| searchWord | string | 是   |        | 搜索词，不超过 255 个字符，支持特殊字符 |

使用方式：

```java
GreeNp.trackSearch('搜索词')
```