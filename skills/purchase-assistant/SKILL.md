---
name: purchase-assistant
description: Analyze procurement-system questions with backend source `/Users/gaoshuanglong/Desktop/PmsCode/back/purchase`, frontend source `/Users/gaoshuanglong/Desktop/PmsCode/front/AddEspPortLang_2026-02-09`, and dual RepoMapper snapshots (`/Users/gaoshuanglong/Desktop/PmsCode/back/repomap_output.txt`, `/Users/gaoshuanglong/Desktop/PmsCode/front/repomap_output.txt`) for fast candidate narrowing. Use for API/UI behavior, business rules, call chains, error root-cause analysis, and implementation lookup. For explicit acceptance/reconciliation data checks with concrete business data, run readonly SQL only through `/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/query_mysql_readonly.py` with EXPLAIN-first safety checks.
---

# Purchase Assistant

## Goal

Answer purchase-system questions quickly with evidence from backend code, frontend code, RepoMapper snapshots, and readonly database queries.

## Scope

- Backend: `/Users/gaoshuanglong/Desktop/PmsCode/back/purchase`
- Frontend: `/Users/gaoshuanglong/Desktop/PmsCode/front/AddEspPortLang_2026-02-09`
- Backend RepoMap: `/Users/gaoshuanglong/Desktop/PmsCode/back/repomap_output.txt`
- Frontend RepoMap: `/Users/gaoshuanglong/Desktop/PmsCode/front/repomap_output.txt`

## Fast Workflow (RepoMap First)

1. Read `/Users/gaoshuanglong/Desktop/PmsCode/back/purchase/AGENTS.md` before backend analysis.
2. Extract query keywords from user input (Chinese/English business terms, API path, class name, error code).
3. Classify scope:
   - Backend signal (`controller/service/dao/java/api`) -> `backend`.
   - Frontend signal (`page/component/route/hook/tsx`) -> `frontend`.
   - Unclear or cross-end call chain -> `all`.
4. Run RepoMap prefilter first:
   - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope backend --query "<keywords>" --top 20 --paths-only`
   - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope frontend --query "<keywords>" --top 20 --paths-only`
   - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope all --query "<keywords>" --top 30 --paths-only`
5. If prefilter returns paths, run targeted `rg` on those files first; do not start with full-repo `rg`.
6. If candidates are empty or weak, fallback to module `rg`, then full-repo `rg`.
7. Trace end-to-end call chain: frontend entry -> API request -> controller -> service -> manager -> dao/integration.
8. If the question includes concrete business data for acceptance/reconciliation, execute readonly SQL by script only.
9. Output conclusion plus key evidence in required format.

## RepoMap Notes

1. RepoMap is a snapshot, not live code. Always open and verify current source files before final conclusion.
2. RepoMap is a prefilter only. Final evidence must come from real files in backend/frontend repositories.
3. If prefilter has no useful hit, fallback to global search in backend modules (`purchase-web`, `purchase-service`, `purchase-manager`, `purchase-dao`, `purchase-api`, `purchase-integration`) and frontend modules (`apps`, `libs`).
4. Avoid exposing sensitive config values from snapshot output. Cite keys/locations, do not echo secrets.

## Mandatory DB Query Rule

所有查库必须使用 `/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/query_mysql_readonly.py` 执行，不允许直接手写执行 SQL。

## Readonly Query Constraints

1. Only query SQL (`SELECT` or `WITH`) is allowed.
2. Any write-risk keyword is forbidden (`INSERT/UPDATE/DELETE/REPLACE/TRUNCATE/DROP/ALTER/CREATE/GRANT/REVOKE/CALL/INTO OUTFILE/...`).
3. Semicolon is forbidden to prevent multi-statement execution.
4. If no `LIMIT` exists, auto-append `LIMIT 200`.
5. Execute `EXPLAIN` before query and block risky full-table scans.

## DB Connection

Load from environment variables:

- `purchase.datasource.url` (fallback `PURCHASE_DATASOURCE_URL`)
- `purchase.datasource.username` (fallback `PURCHASE_DATASOURCE_USERNAME`)
- `purchase.datasource.password` (fallback `PURCHASE_DATASOURCE_PASSWORD`)

## Required Output Format

- `结论`：direct answer first.
- `关键依据`：
  - File paths.
  - Class or function names.
  - Key call chain (for example `Controller -> Service -> Manager -> DAO/Integration`).
- `说明`：mark assumptions or uncertainty explicitly.

## Search Playbook

- RepoMap prefilter:
  - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope backend --query "<keyword|api|class|error>" --top 20 --paths-only`
  - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope frontend --query "<keyword|route|page|component|hook>" --top 20 --paths-only`
  - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/search_repomap.py --scope all --query "<keyword>" --top 30 --paths-only`
- Backend search:
  - `rg -n "关键词|error.code|/api/path|ClassName" /Users/gaoshuanglong/Desktop/PmsCode/back/purchase`
  - `rg -n "methodName|fieldName" /Users/gaoshuanglong/Desktop/PmsCode/back/purchase/purchase-service /Users/gaoshuanglong/Desktop/PmsCode/back/purchase/purchase-web`
  - `rg -n "enum .*|class .*|interface .*" /Users/gaoshuanglong/Desktop/PmsCode/back/purchase`
- Frontend search:
  - `rg -n "关键词|api|contractCode|route|componentName" /Users/gaoshuanglong/Desktop/PmsCode/front/AddEspPortLang_2026-02-09/apps /Users/gaoshuanglong/Desktop/PmsCode/front/AddEspPortLang_2026-02-09/libs`
- Readonly query:
  - `python3 /Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/skills/purchase-assistant/scripts/query_mysql_readonly.py --sql "<SQL>"`
