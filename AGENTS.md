# AGENTS.md — sforce-api-sdk

Salesforce REST API / Bulk API 2.0 Java SDK（Java 21，Maven 多模块：`sforce-api-core` + `sforce-api-spring-boot-starter`）。

## 强制开发流程：分支 + PR，禁止直接推 main

**所有改动（代码 / 测试 / 文档 / 配置）必须通过 Pull Request 合并到 main。**

1. 从最新 main 开分支

   ```bash
   git checkout main && git pull
   git checkout -b <type>/<short-name>     # feat/ fix/ test/ docs/ chore/
   ```

2. 提交（Conventional Commits：`feat:` `fix:` `test:` `docs:` `chore:`；破坏性变更标题加 `!`）
3. push 分支并开 PR（base: `main`）
4. **CI 必须通过**（构建 + 单元测试 + javadoc）才能合并
5. 合并后删除分支（本地 + 远端）

> main 已启用分支保护规则（ruleset `main`）：要求 PR + `CI` 状态检查通过 + 禁止 force push。
>
> **唯一例外**：Release workflow 的自动化提交（`release: vX.Y.Z`、`chore: bump to X.Y.Z-SNAPSHOT`）由 workflow 通过 bypass 直推 main —— 这是发布机制的组成部分。**人工提交不得使用该通道。**

## 发布流程

- main 常态版本：`<next>-SNAPSHOT`
- 发布：GitHub → Actions → **Release** → Run workflow（选择 bump：`patch` / `minor` / `major`）
  - **发布版本 = 当前 pom 版本去掉 `-SNAPSHOT`**；bump 选项只决定发布后 main 停在哪个 SNAPSHOT，不影响本次发布版本
  - 例：pom 为 `0.0.8-SNAPSHOT`，选 `minor` → 发布 **0.0.8**，main 变 `0.1.0-SNAPSHOT`
- 产物：Maven Central（`io.github.phper666`）+ GitHub Release（自动生成 notes）

## 构建与测试

```bash
mvn clean test                     # 单元测试（必须全绿）
mvn clean install -DskipTests      # 构建（不先 install 时 javadoc:jar 会因 starter 模块依赖解析失败）
mvn javadoc:jar                    # 必须零警告（Maven Central 硬要求）
```

### Live 集成测试（真实 Salesforce org）

需要环境变量 `SF_INSTANCE_URL` / `SF_CLIENT_ID` / `SF_CLIENT_SECRET`：

```bash
mvn test -pl sforce-api-core -Dtest=SforceApiLiveTest -DfailIfNoTests=false \
  "-Djunit.jupiter.conditions.deactivate=org.junit.*DisabledCondition"
```

- 测试类标了 `@Disabled`，靠上面的 property 临时启用
- 环境限制（org 存储满、对象无权限）用 `Assumptions.abort` 跳过，不算失败
- 该 org 的已知约束：**Account 对象不可用** → 用 `pickQueryableObject()` 选可用对象（优先 Contact）

## 代码约定

- Java 21；简单不可变 DTO 用 `record`，可变/复杂模型用 Lombok `@Data` + `@Accessors(chain = true)`
- **javadoc 零警告**：类与 record 需有描述；record 组件写 `@param`，公开方法写 `@param` / `@return`
- 测试：JUnit 5；HTTP 层用 OkHttp `Interceptor` mock（不依赖真实网络）
- **破坏性 API 变更**：提交标题加 `!`（如 `fix(bulk)!:`），PR 说明迁移方式
- 新功能必须带测试；Bulk / REST 关键路径优先补 live 验证
- 手术式修改：只碰必须碰的，不顺手重构相邻代码

## 已知坑（改动相关区域前先读）

- **Bulk API 2.0 `/resultPages` 的实际响应与官方文档不一致**：实际字段是 `resultChunks[]` / `resultLink`（文档写 `resultPages[]` / `resultUrl`），且 `resultLink` 是版本-less 相对路径（`/jobs/query/...`）。改动该区域**必须跑 live 测试**。
- 查询 job 结果保留 7 天；不再需要时用 `deleteBulkQueryJob` 清理。
- 每个 HTTP 请求计 1 次 API 调用（org 共享配额；Developer Edition 15,000/天）。
- `runQueryJobs` 返回有序 `List<QueryJobResult>`；旧版 `Map<String, File>` 会让重复查询互相覆盖。
- 自定义 `OkHttpClient` 对**所有**请求生效（含 auth token 端点）——请求统计类拦截器需自行排除 `/services/oauth2/token`。
