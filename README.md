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
import com.phper666.sforce.api.sdk.SforceApi;
import com.phper666.sforce.api.sdk.config.SdkConfig;
import com.phper666.sforce.api.sdk.config.AuthFlow;

var config = new SdkConfig()
    .setAuthFlow(AuthFlow.CLIENT_CREDENTIAL)
    .setClientId("your_client_id")
    .setClientSecret("your_client_secret")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);

// 创建
var created = api.create("Account", Map.of("Name", "Acme"));
String id = created.getId();

// 读取
Account acc = api.get("Account", id, Account.class);

// 更新
api.update("Account", id, Map.of("Name", "Acme Updated"));

// 删除
api.delete("Account", id);

// 按 External ID 创建或更新
api.upsert("Account", "External_Id__c", "ext-001", Map.of("Name", "Acme"));

// 按 External ID 读取
Account byExt = api.getByExternalId("Account", "External_Id__c", "ext-001", Account.class);

// 通用信息
String token = api.getAccessToken();
String endpoint = api.getApiEndpoint();
UserInfo user = api.getUserInfo(UserInfo.class);
```

## 认证方式

SDK 内置 4 种 OAuth 2.0 认证流程。

### Password Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.PASSWORD)
    .setUsername("user@example.com")
    .setPassword("password")
    .setClientId("client_id")
    .setClientSecret("client_secret");
```

### Client Credentials Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.CLIENT_CREDENTIAL)
    .setClientId("client_id")
    .setClientSecret("client_secret")
    .setLoginEndpoint("https://login.salesforce.com");
```

### Authorization Code Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.AUTHORIZATION_CODE)
    .setAuthorizationCode("code_from_callback")
    .setRedirectUri("https://localhost/callback")
    .setClientId("client_id")
    .setClientSecret("client_secret");
```

### Access Token Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.ACCESS_TOKEN)
    .setAccessToken("00D...")
    .setLoginEndpoint("https://mydomain.my.salesforce.com");
```

## 子 API 访问

```java
api.sobject()     // SObject CRUD、批量、Describe
api.query()       // SOQL / SOSL / Tooling API
api.composite()   // Composite API
api.bulk()        // Bulk API 2.0
api.file()        // 文件上传/下载
api.customCode()  // Apex REST / Invocable Actions
```

## SforceApi 便捷方法

`SforceApi` 在子 API 之上提供了一层快捷入口，并自动完成自定义对象 namespace 解析。

```java
// CRUD
CreateObjectResponse created = api.create("Account", accountData);
Account acc = api.get("Account", id, Account.class);
api.update("Account", id, accountData);
api.delete("Account", id);

// 带请求头的重载
created = api.create("Account", accountData, Map.of("Sforce-Auto-Assign", "FALSE"));
api.update("Account", id, accountData, Map.of("Sforce-Auto-Assign", "FALSE"));

// External ID
created = api.upsert("Account", "External_Id__c", "ext-001", accountData);
Account byExt = api.getByExternalId("Account", "External_Id__c", "ext-001", Account.class);

// 批量读取（自动按 2000 分组）
List<Account> list = api.batchGet("Account", ids, List.of("Id", "Name"), Account.class);

// 对象描述
ObjectDescribeResponse describe = api.describe("Account");

// 会话信息
String token = api.getAccessToken();
String endpoint = api.getApiEndpoint();
UserInfo user = api.getUserInfo(UserInfo.class);
```

## SobjectApi

通过 `api.sobject()` 访问，提供最完整的 SObject 操作能力。

### SObject CRUD

```java
var sobject = api.sobject();

CreateObjectResponse created = sobject.createSObject("Account", Map.of("Name", "Acme"));
String id = created.getId();

Account acc = sobject.getSObject("Account", id, Account.class);

Map<String, Object> asMap = sobject.getSObjectAsMap("Account", id);

sobject.updateSObject("Account", id, Map.of("Name", "Acme Updated"));
sobject.deleteSObject("Account", id);

// 按 External ID 创建或更新
CreateOrUpdateObjectResponse upserted = sobject.createOrUpdateSObject(
    "Account", "External_Id__c", "ext-001", Map.of("Name", "Acme"));

Account byExt = sobject.getSObjectByExternalId(
    "Account", "External_Id__c", "ext-001", Account.class);
```

### 自定义对象

自定义对象后缀 `__c` 会被自动拼接 namespace（例如 `MyObj__c` → `myns__MyObj__c`）。

```java
String cId = sobject.createCObject("MyObj__c", Map.of("Name", "Value"));

MyObj obj = sobject.getCObject("MyObj__c", cId, MyObj.class);

sobject.updateCObject("MyObj__c", cId, Map.of("Name", "Updated"));
sobject.deleteCObject("MyObj__c", cId);

CreateOrUpdateObjectResponse r = sobject.createOrUpdateCObject(
    "MyObj__c", "Ext__c", "ext-001", Map.of("Name", "Value"));

MyObj byExt = sobject.getCObjectByExternalId(
    "MyObj__c", "Ext__c", "ext-001", MyObj.class);
```

> **命名空间自动检测**：如果传入的对象名已包含命名空间前缀（如 `otherns__MyObj__c`），SDK 会自动识别并跳过全局配置，不会重复追加前缀。详见[命名空间](#命名空间)。

### 批量操作

```java
import com.phper666.sforce.api.sdk.model.CompositeBodyObject;
import com.phper666.sforce.api.sdk.model.CompositeObject;

// 批量创建
var create1 = new CompositeBodyObject();
create1.setObjectType("Account");
create1.setBody(Map.of("Name", "A"));

List<CreateObjectResponse> created = sobject.batchCreateSObjects(List.of(create1));

// 批量创建或更新（按 External ID，最大 200 条/批）
// 方式 1：传入 CompositeObject 子类
public class AccountRecord extends CompositeObject {
    public String Name;
    public String External_Id__c;
}

var upsert1 = new AccountRecord();
upsert1.setObjectType("Account");
upsert1.Name = "A";
upsert1.External_Id__c = "ext-001";

List<CreateOrUpdateObjectResponse> upserted = sobject.batchCreateOrUpdateSObjects(
    "Account", "External_Id__c", List.of(upsert1));

// 方式 2：传入 Map（key 为 External ID 值，value 为记录字段）
Map<String, CreateOrUpdateObjectResponse> upsertedByMap = sobject.batchCreateOrUpdateSObjects(
    "Account", "External_Id__c",
    Map.of(
        "ext-001", Map.of("Name", "A"),
        "ext-002", Map.of("Name", "B")));

// 批量更新自定义对象
var update1 = new CompositeBodyObject();
update1.setObjectType("myns__MyObj__c");
update1.setBody(Map.of("Id", "a0B...", "Name", "Updated"));

List<UpdateObjectResponse> updated = sobject.batchUpdateCObjects(List.of(update1));

// 批量读取（自动按 2000 分组）
List<Account> accounts = sobject.batchGetSObjects(
    "Account", List.of("001...", "001..."), List.of("Id", "Name"), Account.class);

// 批量删除（最多 200 条）
List<DeleteObjectResponse> deleted = sobject.batchDeleteObjects(
    List.of("001...", "001..."), false);
```

### 对象描述

```java
// 原始 JSON
String json = sobject.describeObject("Account");

// 反序列化为对象
ObjectDescribeResponse describe = sobject.getSObjectDescribe("Account");

// 自定义对象描述
ObjectDescribeResponse custom = sobject.getCObjectDescribe("MyObj__c");
```

### 列出对象与批量描述

```java
// 列出当前用户有权限访问的所有对象
List<SObjectMetadata> objects = sobject.listObjects();

// 客户端分页
PageQueryResponse<SObjectMetadata> page = sobject.listObjects(1, 10);

// 批量描述多个对象（每次 Composite 最多 25 个）
List<ObjectDescribeResponse> describes = sobject.describeObjects(
    List.of("Account", "Contact", "Opportunity"));
```

### Platform Event Schema

```java
String schema = sobject.getPlatformEventSchema("MyEvent__e");
```

### 关系查询与更新

```java
// 查询 Account 下的 Contacts
PageQueryResponse<Contact> contacts = sobject.getSObjectsByRelationship(
    "Account", accountId, "Contacts", Contact.class);

// 通过关系字段更新
sobject.updateSObjectByRelationship(
    "Account", accountId, "Owner", Map.of("OwnerId", newOwnerId));
```

## 命名空间

命名空间（Namespace）仅适用于**自定义对象**（`__c`）和**平台事件**（`__e`）。标准对象（`Account`、`Contact` 等）没有命名空间。

### 对象名

全局命名空间通过 `SdkConfig.setCustomObjectNamespace()` 配置。SDK 会自动检测传入的名称是否已包含命名空间：

| 传入名称 | 全局命名空间 | 实际请求 |
|:---|:---:|:---|
| `MyObj__c` | `myns` | `myns__MyObj__c` — 追加全局前缀 |
| `otherns__MyObj__c` | `myns` | `otherns__MyObj__c` — **已有前缀，跳过** |
| `Account` | `myns` | `Account` — 标准对象，透传 |

### 字段名

插入/更新数据时，**自定义字段名也需要 namespace 前缀**。规则：

| 字段 | 来源 | 是否需要 namespace |
|:---|:---|:---:|
| `Name`, `Id`, `OwnerId` | 系统标准字段 | ❌ 不需要 |
| `LocalField__c` | 本地 org 自己创建 | ❌ 不需要 |
| `myns__PackageField__c` | managed package 定义 | ✅ 需要 |

DTO 类通过 `@AppendCustomNamespace` + `@SerializedName` 控制。namespace 前缀**从全局 `SdkConfig.setCustomObjectNamespace()` 读取**，和对象名走同一个配置：

```java
@AppendCustomNamespace                // ← 启用字段 namespace 自动拼接
public class OrderDTO {
    // 标准字段 — 不处理，由调用方自行赋值
    private String Name;

    // Package 字段 — 自动拼接全局 namespace
    @SerializedName("PackageField__c") // → 序列化为 myns__PackageField__c
    private String packageField;

    // 其他 package 的字段 — 已含前缀，自动跳过
    @SerializedName("otherns__Field__c") // → otherns__Field__c，不重复追加
    private String otherNsField;

    // 本地自定义字段 — 不加 @SerializedName 则不参与自动序列化
    private String localPickup__c;
}
```

> `@AppendCustomNamespace` 只影响带 `@SerializedName` 且以 `__c`/`__e` 结尾的字段。不加 `@SerializedName` 的字段**不参与自动序列化**，需自己处理。

混合使用示例（全局 namespace = `myns`）：

```java
var data = new OrderDTO();
data.setName("测试单");
data.setPackageField("pkg-value");
data.setOtherNsField("跨命名空间值");
// localPickup__c 不加 @SerializedName，不会序列化，不用赋值

// 传 Order__c → 自动解析为 myns__Order__c
api.create("Order__c", data);
// 实际发送: {"Name":"测试单","myns__PackageField__c":"pkg-value","otherns__Field__c":"跨命名空间值"}
// 解析过程:
//   Order__c          → resolveType → myns__Order__c         (自动拼接)
//   PackageField__c   → @SerializedName + appendNamespace → myns__PackageField__c (自动拼接)
//   otherns__Field__c → @SerializedName + contains("__")  → otherns__Field__c     (已有前缀跳过)
//   Name              → 无 @SerializedName → 不参与自动序列化
```

## QueryApi

通过 `api.query()` 访问，支持 SOQL、SOSL 与 Tooling API。

### SOQL

```java
var query = api.query();

// 单次查询
PageQueryResponse<Account> page = query.soqlQuery(
    "SELECT Id, Name FROM Account WHERE Name = 'Acme'", Account.class);

// 自动翻页，返回全部记录
PageQueryResponse<Account> all = query.soqlQueryAll(
    "SELECT Id, Name FROM Account", Account.class);

// 计数
int count = query.soqlQueryCount("SELECT Id FROM Account");

// 手动翻页
PageQueryResponse<Account> next = query.soqlQueryNext(
    page.getNextRecordsUrl(), Account.class);
```

### SOSL

```java
// 普通 SOSL
SOSLQueryResponse result = query.soslQuery(
    "FIND {Acme} IN ALL FIELDS RETURNING Account(Id, Name)");

// 参数化搜索（GET）
SOSLQueryResponse result2 = query.soslQueryWithParameter("q=Acme");

// 参数化搜索（POST）
var body = new ParameterizedSearchRequestBody(
    "Acme", null, null, null, null, null, null,
    null, null, null, null, null, null, true, null, null);
SOSLQueryResponse result3 = query.soslQueryWithParameter(body);
```

### Tooling API

```java
PageQueryResponse<ApexClass> tooling = query.toolingApiSoqlQuery(
    "SELECT Id, Name FROM ApexClass", ApexClass.class);
```

## CompositeApi

通过 `api.composite()` 访问，单次请求最多 25 个子请求。

```java
var req = new CompositeRequest();
req.setMethod("GET");
req.setUrl("/services/data/v62.0/sobjects/Account/001...");
req.setReferenceId("account-001");

var body = new CompositeRequestBody();
body.setAllOrNone(false);
body.setCollateSubrequests(true);
body.setCompositeRequest(List.of(req));

CompositeResponseBody resp = api.composite().compositeRequest(body);
resp.getCompositeResponse().forEach(cr -> {
    System.out.println(cr.getReferenceId() + " -> " + cr.getHttpStatusCode());
});
```

## BulkApi

通过 `api.bulk()` 访问，实现 Bulk API 2.0 大数据导入导出。

```java
import com.phper666.sforce.api.sdk.BulkApi;
import com.phper666.sforce.api.sdk.model.BulkApiCreateJobRequest;
import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;

var request = new BulkApiCreateJobRequest()
    .setObject("Account")
    .setOperation(BulkApi.JobOperation.INSERT)
    .setColumnDelimiter(BulkApi.ColumnDelimiter.COMMA)
    .setLineEnding(BulkApi.LineEnding.LF);

File csv = new File("accounts.csv");
TimeoutSettings timeout = new TimeoutSettings();

// 创建任务并上传 CSV
BulkApiJobDetailResponse job = api.bulk().createBulkApiJob(request, csv, timeout);

// 查询任务状态
BulkApiJobDetailResponse detail = api.bulk().getBulkApiJob(job.getId(), timeout);

// 下载结果
api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.SUCCESSFUL_RESULT, new File("success.csv"), timeout);

api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.FAILED_RESULT, new File("failed.csv"), timeout);

api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.UNPROCESSED_RESULT, new File("unprocessed.csv"), timeout);
```

### 相关枚举

| 枚举 | 说明 |
| --- | --- |
| `BulkApi.JobOperation` | `INSERT` / `DELETE` / `HARD_DELETE` / `UPDATE` / `UPSERT` |
| `BulkApi.JobResultType` | `SUCCESSFUL_RESULT` / `FAILED_RESULT` / `UNPROCESSED_RESULT` |
| `BulkApi.ColumnDelimiter` | `BACKQUOTE` / `CARET` / `COMMA` / `PIPE` / `SEMICOLON` / `TAB` |
| `BulkApi.LineEnding` | `LF` / `CRLF` |
| `BulkApi.JobState` | `OPEN` / `UPLOAD_COMPLETE` / `ABORTED` / `JOB_COMPLETE` / `FAILED` |

## FileApi

通过 `api.file()` 访问，支持 Chatter 文件与 ContentDocument 下载。

```java
import com.phper666.sforce.api.sdk.model.DownloadContentDocumentRequest;
import com.phper666.sforce.api.sdk.config.SdkTypes.TimeoutSettings;

// 上传 Chatter 文件
String fileId = api.file().uploadChatterFile(new File("report.pdf"));
String fileId2 = api.file().uploadChatterFile(new File("report.pdf"), new TimeoutSettings());

// 生成 Chatter 文件下载直链
String downloadUrl = api.file().generateChatterFileDownloadUrl(fileId);

// 下载 ContentDocument（按前缀/后缀生成临时文件）
File doc = api.file().downloadContentDocument(
    new DownloadContentDocumentRequest("069...", "/tmp", "doc", ".pdf"));

File doc2 = api.file().downloadContentDocument(
    new DownloadContentDocumentRequest("069...", "/tmp", "doc", ".pdf"),
    new TimeoutSettings());

// 下载 ContentDocument 并按响应头命名
File named = api.file().downloadContentDocumentFile("069...");
File named2 = api.file().downloadContentDocumentFile("069...", new TimeoutSettings());
```

## CustomCodeApi

通过 `api.customCode()` 访问，支持 Apex REST、Invocable Actions 与 Quick Action。

### Apex REST

```java
import com.phper666.sforce.api.sdk.config.SdkTypes.HttpMethod;

String result = api.customCode().runApex(
    "/services/apexrest/myService", HttpMethod.POST, Map.of("key", "value"));
```

### Invocable Actions

```java
// 调用 Action
String resp = api.customCode().invokeInvocableActions(
    "/services/data/v62.0/actions/standard/emailSimple",
    Map.of("inputs", List.of(Map.of("emailAddresses", "a@b.com"))));

// 获取 Action Schema
String schema = api.customCode().getInvocableActionSchema(
    "/services/data/v62.0/actions/standard/emailSimple");

// 列出标准 Action
ListInvocableActionResult standard = api.customCode().listStandardInvocableActions();

// 列出自定义 Action
ListInvocableActionResult custom = api.customCode().listCustomInvocableActions(
    CustomCodeApi.CustomActionType.FLOW);
// 可选类型：FLOW / APEX / GENERATE_PROMPT_RESPONSE
```

### Quick Action

```java
String quick = api.customCode().getQuickAction("Account", "Account.MyQuickAction");
```

## SOQL Builder

`SoqlBuilder<T>` 通过方法引用构造类型安全的 SOQL，自动解析字段名与 namespace。

```java
import com.phper666.sforce.api.sdk.builder.SoqlBuilder;

String soql = new SoqlBuilder<Account>()
    .select(Account::getId, Account::getName)
    .eq(Account::getName, "Acme")
    .gt(Account::getAnnualRevenue, 100000)
    .orderByDesc(Account::getCreatedDate)
    .limit(100)
    .build();

// 输出：SELECT Id, Name FROM Account
//       WHERE Name = 'Acme' AND AnnualRevenue > 100000
//       ORDER BY CreatedDate DESC LIMIT 100
```

### 更多条件示例

```java
String soql = new SoqlBuilder<Contact>()
    .select(Contact::getId, Contact::getEmail)
    .eq(Contact::getFirstName, "John")
    .or(o -> o.eq(Contact::getLastName, "Doe")
              .isNotNull(Contact::getEmail))
    .orderByAsc(Contact::getLastName)
    .offset(0)
    .limit(50)
    .build();
```

### 聚合与字段函数

```java
String count = new SoqlBuilder<Account>()
    .selectCount(Account::getId, "cnt", false)
    .ge(Account::getCreatedDate, "2024-01-01")
    .build();

String all = new SoqlBuilder<Account>()
    .selectFieldAll()
    .limit(10)
    .build();
```

## Spring Boot 自动配置

### 依赖

```xml
<dependency>
    <groupId>com.phper666</groupId>
    <artifactId>sforce-api-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置

```yaml
sforce:
  api:
    custom-object-namespace: myns    # 可选
    connected-apps:
      main:
        consumer-key: xxx
        consumer-secret: xxx
        login-endpoint: https://login.salesforce.com
        debug: true
        debug-log-body: true
```

### 注入使用

```java
import com.phper666.sforce.api.sdk.autoconfigure.SforceApiFactory;
import com.phper666.sforce.api.sdk.SforceApi;

@Service
public class MyService {
    private final SforceApi api;

    public MyService(SforceApiFactory factory) {
        this.api = factory.getForceClient("main");
    }

    public void sync() {
        api.create("Contact", Map.of("LastName", "Li"));
    }
}
```

### 多应用支持

```java
SforceApi main = factory.getForceClient("main");
SforceApi sandbox = factory.getForceClient("sandbox");
SforceApi sandboxWithDomain = factory.getForceClient("sandbox", "https://test.salesforce.com");
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

## 发布

GitHub Actions 自动发布。触发方式：

1. 仓库 → **Actions** → **Release** → **Run workflow**
2. 选择版本号类型：`patch` / `minor` / `major`
3. CI 自动：发布到 Maven Central → 创建 Release → 更新 main 分支版本号

详见 [`.github/workflows/release.yml`](.github/workflows/release.yml)。

## 环境要求

- Java 17+
- core 模块无 Spring 依赖

## License

MIT
