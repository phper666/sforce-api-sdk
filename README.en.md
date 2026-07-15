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
api.sobject().batchCreate(records);
api.sobject().batchUpsert("Account", "External_Id__c", records);

// Bulk API 2.0
api.bulk().createBulkApiJob(request, csvFile, timeout);

// Custom Object (auto-namespace)
api.create("MyObj__c", data);  // → myns__MyObj__c

// Describe
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
    custom-object-namespace: myns    # optional
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

## Sub-API Accessors

```java
api.sobject()     // CRUD + batch + describe
api.query()       // SOQL + SOSL + Tooling API
api.bulk()        // Bulk API 2.0
api.file()        // file upload/download
api.composite()   // composite requests
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
