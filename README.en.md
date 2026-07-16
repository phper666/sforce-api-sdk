<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-中文-blue.svg" alt="中文"></a>
  <a href="README.en.md"><img src="https://img.shields.io/badge/lang-English-red.svg" alt="English"></a>
</p>

<h1 align="center">sforce-api-sdk</h1>

<p align="center">
  Salesforce REST API SDK for Java 17+
</p>

<p align="center">
  A clean, practical Java SDK for Salesforce REST API — CRUD, batch, SOQL/SOSL, Bulk API, file operations, and more
</p>

---

## Features

- **SObject CRUD** — create / get / update / delete / upsert
- **Batch operations** — composite API batch create, update, upsert, delete
- **SOQL + SOSL** — query, queryAll (auto-pagination), count, search
- **Bulk API 2.0** — large data import/export
- **SOQL Builder** — type-safe, lambda-based query builder
- **File operations** — Chatter upload, ContentDocument download
- **Composite API** — multiple requests in one call
- **Custom Object support** — automatic namespace resolution (`__c`/`__e`)
- **Debug mode** — request/response logging with masked tokens
- **Spring Boot auto-configuration**

## Quick Start

### Maven

```xml
<dependency>
    <groupId>com.phper666</groupId>
    <artifactId>sforce-api-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Basic Usage

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

// Upsert by external ID
api.upsert("Account", "External_Id__c", "ext-001", data);

// SOQL with auto-pagination
PageQueryResponse<Account> all = api.query().soqlQueryAll(
    "SELECT Id FROM Account", Account.class);

// SOSL
api.query().soslQuery("FIND {Acme}");

// Batch
api.sobject().batchCreateSObjects(records);
api.sobject().batchCreateOrUpdateSObjects("Account", "External_Id__c", records);

// Bulk API 2.0
api.bulk().createBulkApiJob(request, csvFile, timeout);

// Custom Object (auto-namespace)
api.create("MyObj__c", data);  // → myns__MyObj__c

// Describe
api.describe("Account");
```

## Authentication

The SDK supports four OAuth flows. Set the flow with `SdkConfig.setAuthFlow(AuthFlow)`.

### Password Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.PASSWORD)
    .setUsername("user@example.com")
    .setPassword("password+securityToken")
    .setClientId("client_id")
    .setClientSecret("client_secret")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);
```

### Client Credentials Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.CLIENT_CREDENTIAL)
    .setClientId("client_id")
    .setClientSecret("client_secret")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);
```

### Authorization Code Flow

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.AUTHORIZATION_CODE)
    .setClientId("client_id")
    .setClientSecret("client_secret")
    .setAuthorizationCode("auth_code")
    .setRedirectUri("https://callback.example.com/oauth")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);
```

### Access Token Flow

Use an existing access token. The SDK skips the login call.

```java
var config = new SdkConfig()
    .setAuthFlow(AuthFlow.ACCESS_TOKEN)
    .setAccessToken("00Dxx...")
    .setLoginEndpoint("https://login.salesforce.com");

var api = new SforceApi(config);
```

## SforceApi Facade

Convenience methods that route to the underlying sub-APIs.

```java
// Create
CreateObjectResponse created = api.create("Account", Map.of("Name", "Acme"));

// Get by ID
Account account = api.get("Account", "001xx...", Account.class);

// Update
api.update("Account", "001xx...", Map.of("Name", "Acme Updated"));

// Delete
api.delete("Account", "001xx...");

// Upsert by external ID
api.upsert("Account", "External_Id__c", "ext-001", Map.of("Name", "Acme"));

// Get by external ID
Account account = api.getByExternalId(
    "Account", "External_Id__c", "ext-001", Account.class);

// Session metadata
String token = api.getAccessToken();
String endpoint = api.getApiEndpoint();
UserInfo user = api.getUserInfo(UserInfo.class);

// Describe an object
ObjectDescribeResponse describe = api.describe("Account");
```

## SobjectApi

Access via `api.sobject()`.

### SObject CRUD

```java
// Create
CreateObjectResponse resp = api.sobject().createSObject("Account", Map.of("Name", "Acme"));

// Create with custom headers
Map<String, String> headers = Map.of("Sforce-Auto-Assign", "FALSE");
CreateObjectResponse resp2 = api.sobject().createSObject("Account", data, headers);

// Get by ID
Account account = api.sobject().getSObject("Account", "001xx...", Account.class);

// Get as Map
Map<String, Object> accountMap = api.sobject().getSObjectAsMap("Account", "001xx...");

// Get by external ID
Account account = api.sobject().getSObjectByExternalId(
    "Account", "External_Id__c", "ext-001", Account.class);

// Update
api.sobject().updateSObject("Account", "001xx...", Map.of("Name", "Acme Updated"));

// Update with custom headers
api.sobject().updateSObject("Account", "001xx...", data, headers);

// Upsert
CreateOrUpdateObjectResponse upserted = api.sobject().createOrUpdateSObject(
    "Account", "External_Id__c", "ext-001", data);

// Upsert with custom headers
CreateOrUpdateObjectResponse upserted2 = api.sobject().createOrUpdateSObject(
    "Account", "External_Id__c", "ext-001", data, headers);

// Delete
api.sobject().deleteSObject("Account", "001xx...");
```

### Custom Object CRUD

Custom objects are resolved using the configured namespace and the `__c` suffix.

```java
// Create — objectType "MyObj__c" resolves to "myns__MyObj__c"
String id = api.sobject().createCObject("MyObj__c", data);

// Get
MyObj obj = api.sobject().getCObject("MyObj__c", id, MyObj.class);

// Update
api.sobject().updateCObject("MyObj__c", id, data);

// Delete
api.sobject().deleteCObject("MyObj__c", id);

// Upsert
CreateOrUpdateObjectResponse resp = api.sobject().createOrUpdateCObject(
    "MyObj__c", "External_Id__c", "ext-001", data);

// Get by external ID
MyObj obj = api.sobject().getCObjectByExternalId(
    "MyObj__c", "External_Id__c", "ext-001", MyObj.class);
```

> **Auto-detect namespace**: Passing an already-qualified name like `otherns__MyObj__c` skips the global namespace — no double prefix. See "[Namespace](#namespace)".

### Batch Operations

```java
// Batch create
List<CompositeBodyObject> records = List.of(
    new CompositeBodyObject()
        .setObjectType("Account")
        .setBody(Map.of("Name", "Acme")),
    new CompositeBodyObject()
        .setObjectType("Account")
        .setBody(Map.of("Name", "Globex"))
);
List<CreateObjectResponse> created = api.sobject().batchCreateSObjects(records);

// Batch create with headers
List<CreateObjectResponse> created2 = api.sobject().batchCreateSObjects(records, headers);

// Batch upsert by external ID
List<CompositeObject> upsertRecords = List.of(
    new CompositeObject().setObjectType("Account"),
    new CompositeObject().setObjectType("Account")
);
// populate records via map/POJO as needed
List<CreateOrUpdateObjectResponse> upserted = api.sobject().batchCreateOrUpdateSObjects(
    "Account", "External_Id__c", upsertRecords);

// Batch update
List<UpdateObjectResponse> updated = api.sobject().batchUpdateCObjects(records);
List<UpdateObjectResponse> updated2 = api.sobject().batchUpdateCObjects(records, headers);

// Batch get by IDs
List<Account> accounts = api.sobject().batchGetSObjects(
    "Account",
    List.of("001xx...", "001yy..."),
    List.of("Id", "Name"),
    Account.class);

// Batch delete
List<DeleteObjectResponse> deleted = api.sobject().batchDeleteObjects(
    List.of("001xx...", "001yy..."), false);

// Batch upsert from map of external ID → record
Map<String, Account> byExternalId = Map.of("ext-001", acct1, "ext-002", acct2);
Map<String, CreateOrUpdateObjectResponse> results = api.sobject().batchCreateOrUpdateSObjects(
    "Account", "External_Id__c", byExternalId);

// Batch upsert custom objects
Map<String, CreateOrUpdateObjectResponse> cResults = api.sobject().batchCreateOrUpdateCObjects(
    "MyObj__c", "External_Id__c", byExternalId);
```

### Describe

```java
// Raw JSON describe
String json = api.sobject().describeObject("Account");

// Typed describe
ObjectDescribeResponse describe = api.sobject().getSObjectDescribe("Account");

// Custom object describe
ObjectDescribeResponse cDescribe = api.sobject().getCObjectDescribe("MyObj__c");

// List all objects visible to current user
List<SObjectMetadata> objects = api.sobject().listObjects();

// Paginated list
PageQueryResponse<SObjectMetadata> page = api.sobject().listObjects(1, 50);

// Batch describe multiple objects
List<ObjectDescribeResponse> describes = api.sobject().describeObjects(
    List.of("Account", "Contact", "Opportunity"));
```

### Platform Event

```java
String schema = api.sobject().getPlatformEventSchema("My_Event__e");
```

### Relationship Queries

```java
// Query child records via relationship
PageQueryResponse<Contact> contacts = api.sobject().getSObjectsByRelationship(
    "Account", "001xx...", "Contacts", Contact.class);

// Update a relationship
api.sobject().updateSObjectByRelationship(
    "Account", "001xx...", "Contacts", Map.of("LastName", "Smith"));
```

## Namespace

Namespaces apply only to **custom objects** (`__c`) and **platform events** (`__e`). Standard objects (`Account`, `Contact`, etc.) never have namespaces.

### Object Name

Configure the global namespace via `SdkConfig.setCustomObjectNamespace()`. The SDK auto-detects whether a name is already qualified:

| Name | Global namespace | Actual request |
|:---|:---:|:---|
| `MyObj__c` | `myns` | `myns__MyObj__c` — prepend global prefix |
| `otherns__MyObj__c` | `myns` | `otherns__MyObj__c` — **already qualified, skip** |
| `Account` | `myns` | `Account` — standard object, pass through |

### Field Name

When creating/updating records, **custom field names may also need namespace prefixes**:

| Field | Origin | Namespace needed? |
|:---|:---|:---:|
| `Name`, `Id`, `OwnerId` | System standard fields | ❌ No |
| `LocalField__c` | Created locally in this org | ❌ No |
| `myns__PackageField__c` | Defined by a managed package | ✅ Yes |

Use `@AppendCustomNamespace` + `@SerializedName` on your DTO. The namespace prefix is read from the **global `SdkConfig.setCustomObjectNamespace()`** — same config as the object name:

```java
@AppendCustomNamespace                // ← enable field-level namespace prefixing
public class OrderDTO {
    // Standard field — handled manually
    private String Name;

    // Package field — global namespace prepended automatically
    @SerializedName("PackageField__c") // → serialized as myns__PackageField__c
    private String packageField;

    // Field from another package — already qualified, skipped
    @SerializedName("otherns__Field__c") // → otherns__Field__c, no double-prefix
    private String otherNsField;

    // Local custom field — no @SerializedName, excluded from auto-serialization
    private String localPickup__c;
}
```

> `@AppendCustomNamespace` only affects fields annotated with `@SerializedName` that end in `__c` / `__e`. Fields without `@SerializedName` are **not included** in auto-serialization.

Mixed usage (global namespace = `myns`):

```java
var data = new OrderDTO();
data.setName("Test Order");
data.setPackageField("pkg-value");
data.setOtherNsField("cross-ns value");
// localPickup__c has no @SerializedName, not serialized — skip

// Pass Order__c → resolved to myns__Order__c automatically
api.create("Order__c", data);
// Sends: {"Name":"Test Order","myns__PackageField__c":"pkg-value","otherns__Field__c":"cross-ns value"}
// Resolution:
//   Order__c          → resolveType → myns__Order__c               (auto-prepend)
//   PackageField__c   → @SerializedName + appendNamespace → myns__PackageField__c (auto-prepend)
//   otherns__Field__c → @SerializedName + contains("__")  → otherns__Field__c     (already ns, skip)
//   Name              → no @SerializedName → excluded from auto-serialization
```

## QueryApi

Access via `api.query()`.

### SOQL

```java
// Single-page query
PageQueryResponse<Account> page = api.query().soqlQuery(
    "SELECT Id, Name FROM Account LIMIT 200", Account.class);

// Auto-paginate all records
PageQueryResponse<Account> all = api.query().soqlQueryAll(
    "SELECT Id, Name FROM Account", Account.class);

// Count rows
int count = api.query().soqlQueryCount("SELECT Id FROM Account");

// Fetch next page from a URL returned by Salesforce
PageQueryResponse<Account> next = api.query().soqlQueryNext(
    "/services/data/v62.0/query/01gxx...", Account.class);
```

### SOSL

```java
// Simple SOSL
SOSLQueryResponse response = api.query().soslQuery("FIND {Acme}");

// Parameterized search via query string
SOSLQueryResponse response2 = api.query().soslQueryWithParameter("q=Acme&defaultLimit=20");

// Parameterized search via body
var body = new ParameterizedSearchRequestBody(
    "Acme", "20", null, null, null, null, null,
    null, null, null, null, null, null, false, null, null);
SOSLQueryResponse response3 = api.query().soslQueryWithParameter(body);
```

### Tooling API

```java
PageQueryResponse<ApexClass> classes = api.query().toolingApiSoqlQuery(
    "SELECT Id, Name FROM ApexClass", ApexClass.class);
```

## CompositeApi

Access via `api.composite()`.

```java
var req1 = new CompositeRequest();
req1.setMethod("GET");
req1.setUrl("/services/data/v62.0/sobjects/Account/001xx...");
req1.setReferenceId("account");

var req2 = new CompositeRequest();
req2.setMethod("GET");
req2.setUrl("/services/data/v62.0/sobjects/Contact/describe");
req2.setReferenceId("contactDescribe");

var body = new CompositeRequestBody();
body.setAllOrNone(false);
body.setCompositeRequest(List.of(req1, req2));

CompositeResponseBody response = api.composite().compositeRequest(body);

// With custom headers
CompositeResponseBody response2 = api.composite().compositeRequest(body, headers);
```

## BulkApi

Access via `api.bulk()`.

```java
// Create a job and upload CSV
var request = new BulkApiCreateJobRequest()
    .setObject("Account")
    .setOperation(BulkApi.JobOperation.UPSERT)
    .setExternalIdFieldName("External_Id__c")
    .setLineEnding(BulkApi.LineEnding.LF)
    .setColumnDelimiter(BulkApi.ColumnDelimiter.COMMA);

File csv = new File("accounts.csv");
BulkApiJobDetailResponse job = api.bulk().createBulkApiJob(
    request, csv, new TimeoutSettings());

// Check job status
BulkApiJobDetailResponse status = api.bulk().getBulkApiJob(
    job.getId(), new TimeoutSettings());

// Download successful results
api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.SUCCESSFUL_RESULT,
    new File("success.csv"), new TimeoutSettings());

// Download failed results
api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.FAILED_RESULT,
    new File("failed.csv"), new TimeoutSettings());

// Download unprocessed records
api.bulk().downloadBulkApiJobResult(
    job.getId(), BulkApi.JobResultType.UNPROCESSED_RESULT,
    new File("unprocessed.csv"), new TimeoutSettings());
```

### BulkApi Enums

- `BulkApi.JobOperation` — `INSERT`, `DELETE`, `HARD_DELETE`, `UPDATE`, `UPSERT`
- `BulkApi.JobResultType` — `SUCCESSFUL_RESULT`, `FAILED_RESULT`, `UNPROCESSED_RESULT`
- `BulkApi.ColumnDelimiter` — `BACKQUOTE`, `CARET`, `COMMA`, `PIPE`, `SEMICOLON`, `TAB`
- `BulkApi.LineEnding` — `LF`, `CRLF`
- `BulkApi.JobState` — `OPEN`, `UPDATE_COMPLETE`, `ABORTED`, `JOB_COMPLETE`, `FAILED`

## FileApi

Access via `api.file()`.

### Chatter File

```java
// Upload a file to Chatter
String contentDocumentId = api.file().uploadChatterFile(new File("report.pdf"));

// Upload with custom timeout
String contentDocumentId2 = api.file().uploadChatterFile(
    new File("report.pdf"), new TimeoutSettings());

// Generate a browser download URL
String url = api.file().generateChatterFileDownloadUrl(contentDocumentId);
```

### ContentDocument

```java
// Download to a temp file with explicit naming
var request = new DownloadContentDocumentRequest(
    "069xx...", "/tmp", "report-", ".pdf");
File file = api.file().downloadContentDocument(request);

// Download with custom timeout
File file2 = api.file().downloadContentDocument(request, new TimeoutSettings());

// Download and infer filename from Content-Disposition
File file3 = api.file().downloadContentDocumentFile("069xx...");
File file4 = api.file().downloadContentDocumentFile("069xx...", new TimeoutSettings());
```

## CustomCodeApi

Access via `api.customCode()`.

### Apex REST

```java
// POST to an Apex REST endpoint
String json = api.customCode().runApex(
    "/myService/*", SdkTypes.HttpMethod.POST, Map.of("key", "value"));

// GET with no body
String json2 = api.customCode().runApex(
    "/myService/records", SdkTypes.HttpMethod.GET, null);
```

### Invocable Actions

```java
// Invoke a standard action
String result = api.customCode().invokeInvocableActions(
    "/services/data/v62.0/actions/standard/flow/Run_Flow", inputs);

// Get action schema
String schema = api.customCode().getInvocableActionSchema(
    "/services/data/v62.0/actions/standard/flow/Run_Flow");

// List standard actions
ListInvocableActionResult actions = api.customCode().listStandardInvocableActions();

// List custom actions
ListInvocableActionResult flows = api.customCode().listCustomInvocableActions(
    CustomCodeApi.CustomActionType.FLOW);
ListInvocableActionResult apexActions = api.customCode().listCustomInvocableActions(
    CustomCodeApi.CustomActionType.APEX);
ListInvocableActionResult prompts = api.customCode().listCustomInvocableActions(
    CustomCodeApi.CustomActionType.GENERATE_PROMPT_RESPONSE);

// Quick action metadata
String quickAction = api.customCode().getQuickAction("Account", "Send_Email");
```

## SOQL Builder

Build type-safe SOQL queries with lambdas.

```java
String soql = new SoqlBuilder<Contact>()
    .select(Contact::getId, Contact::getName)
    .where(Contact::getFirstName).eq("test")
    .and(Contact::getAnnualRevenue).gt(100000)
    .orderByDesc(Contact::getCreatedDate)
    .limit(100)
    .build();
// → SELECT Id, Name FROM Contact
//   WHERE FirstName = 'test' AND AnnualRevenue > 100000
//   ORDER BY CreatedDate DESC LIMIT 100

// More operators
String soql2 = new SoqlBuilder<Account>()
    .select(Account::getId, Account::getName)
    .where(Account::getIndustry).eq("Technology")
    .and(Account::getAnnualRevenue).ge(1000000)
    .and(Account::getName).in(List.of("Acme", "Globex"))
    .or(nested -> nested
        .where(Account::getBillingCity).eq("San Francisco")
        .and(Account::getBillingCountry).eq("USA"))
    .build();

// Group by and count
String soql3 = new SoqlBuilder<Account>()
    .select("Industry", "COUNT(Id) cnt")
    .groupBy(Account::getIndustry)
    .build();
```

## Spring Boot Auto-Configuration

Add the starter:

```xml
<dependency>
    <groupId>com.phper666</groupId>
    <artifactId>sforce-api-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Configure in `application.yml`:

```yaml
sforce:
  api:
    connected-apps:
      main:
        consumer-key: xxx
        consumer-secret: xxx
        login-endpoint: https://login.salesforce.com
      secondary:
        consumer-key: xxx
        consumer-secret: xxx
        login-endpoint: https://test.salesforce.com
    custom-object-namespace: myns    # optional
    debug: true
    debug-log-body: true
```

Inject the factory and use it:

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

You can also target a specific domain:

```java
SforceApi api = factory.getForceClient("main", "https://mydomain.my.salesforce.com");
```

## Debug Mode

```yaml
sforce:
  api:
    debug: true
    debug-log-body: true
    debug-body-max-size: 4096
```

DEBUG level: URL, status code, timing. TRACE level: body content. Authorization header masked as `Bearer ***`.

## Requirements

- Java 17+
- No Spring Boot dependency required for core module

## License

MIT
