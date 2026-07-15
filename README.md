<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-中文-blue.svg" alt="中文"></a>
  <a href="README.en.md"><img src="https://img.shields.io/badge/lang-English-red.svg" alt="English"></a>
</p>

<h1 align="center">sforce-api-sdk</h1>

<p align="center">
  Salesforce REST API SDK for Java 17+
</p>

<p align="center">
  精简、实用的 Salesforce REST API Java SDK，支持 CRUD、批量操作、SOQL/SOSL 查询、Bulk API、文件操作等
</p>

---

## 特性

- **SObject CRUD** — create / get / update / delete / upsert
- **批量操作** — composite API 批量创建、更新、删除
- **SOQL + SOSL** — 查询、自动分页、计数、搜索
- **Bulk API 2.0** — 大数据导入导出
- **SOQL Builder** — Lambda 类型安全查询构造器
- **文件操作** — Chatter 上传、ContentDocument 下载
- **Composite API** — 单次请求执行多个操作
- **自定义对象** — 自动 namespace 解析（`__c`/`__e`）
- **Debug 模式** — 请求/响应日志，token 自动脱敏
- **Spring Boot 自动配置**

## 快速开始

### Maven

```xml
<dependency>
    <groupId>com.phper666</groupId>
    <artifactId>sforce-api-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基础用法

```java
var config = new SdkConfig()
    .setClientId("your_client_id")
    .setClientSecret("your_client_secret")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);

// CRUD
var result = api.create("Account", Map.of("Name", "Acme"));
String id = result.getId();

Account acc = api.get("Account", id, Account.class);
api.update("Account", id, Map.of("Name", "Acme Updated"));
api.delete("Account", id);

// 按 External ID 更新
api.upsert("Account", "External_Id__c", "ext-001", data);

// SOQL 自动分页
PageQueryResponse<Account> all = api.query().soqlQueryAll(
    "SELECT Id FROM Account", Account.class);

// SOSL
api.query().soslQuery("FIND {Acme}");

// 批量操作
api.sobject().batchCreate(records);
api.sobject().batchUpsert("Account", "External_Id__c", records);

// Bulk API 2.0
api.bulk().createBulkApiJob(request, csvFile, timeout);

// 自定义对象（自动拼 namespace）
api.create("MyObj__c", data);  // → myns__MyObj__c

// 获取对象描述
api.describe("Account");
```

### Spring Boot

```xml
<dependency>
    <groupId>com.phper666</groupId>
    <artifactId>sforce-api-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

```yaml
sforce:
  api:
    connected-apps:
      main:
        consumer-key: xxx
        consumer-secret: xxx
        login-endpoint: https://login.salesforce.com
    custom-object-namespace: myns    # 可选
    debug: true
```

```java
@Service
public class MyService {
    private final SforceApi api;

    public MyService(SforceApiFactory factory) {
        this.api = factory.getForceClient("main");
    }

    public void sync() {
        api.create("Contact", contact);
    }
}
```

## 子 API 访问

```java
api.sobject()     // CRUD + 批量 + describe
api.query()       // SOQL + SOSL + Tooling API
api.bulk()        // Bulk API 2.0
api.file()        // 文件上传/下载
api.composite()   // 复合请求
api.customCode()  // Apex REST + invocable actions
api.getAccessToken()
api.getUserInfo(UserInfo.class)
```

## SOQL Builder

```java
String soql = new SoqlBuilder<Contact>()
    .select(Contact::getId, Contact::getName)
    .where(Contact::getFirstName).eq("test")
    .and(Contact::getAnnualRevenue).gt(100000)
    .build();
// → SELECT Id, Name FROM Contact
//   WHERE FirstName = 'test' AND AnnualRevenue > 100000
```

## Debug 模式

```yaml
sforce:
  api:
    debug: true
    debug-log-body: true
    debug-body-max-size: 4096
```

DEBUG 级别输出 URL、状态码、耗时；TRACE 级别输出 body。Authorization header 自动脱敏为 `Bearer ***`。

## 环境要求

- Java 17+
- core 模块无 Spring 依赖

## License

MIT
