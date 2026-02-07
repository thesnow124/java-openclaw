---
name: github
description: 使用 GitHub CLI (gh) 与 GitHub 交互，管理 issue、PR、CI 等
metadata:
  openclaw:
    emoji: 🐙
    requires:
      bins: [gh]
    install:
      - id: brew
        kind: brew
        formula: gh
        bins: [gh]
        label: 安装 GitHub CLI (brew)
      - id: apt
        kind: apt
        package: gh
        bins: [gh]
        label: 安装 GitHub CLI (apt)
---

# GitHub 技能

使用 `gh` CLI 与 GitHub 交互。在非 git 目录中时，务必指定 `--repo owner/repo`，或直接使用 URL。

## Pull Requests

查看 PR 的 CI 状态：

```bash
gh pr checks 55 --repo owner/repo
```

列出最近的工作流运行：

```bash
gh run list --repo owner/repo --limit 10
```

查看运行记录并查看失败的步骤：

```bash
gh run view <run-id> --repo owner/repo
```

仅查看失败步骤的日志：

```bash
gh run view <run-id> --repo owner/repo --log-failed
```

## Issues

列出 issues：

```bash
gh issue list --repo owner/repo --state open --limit 20
```

查看特定 issue：

```bash
gh issue view 123 --repo owner/repo
```

创建 issue：

```bash
gh issue create --repo owner/repo --title "标题" --body "描述"
```

## 高级查询

使用 `gh api` 命令访问其他子命令未提供的数据。

获取 PR 的特定字段：

```bash
gh api repos/owner/repo/pulls/55 --jq '.title, .state, .user.login'
```

搜索 PR：

```bash
gh search prs --repo owner/repo --state open --query "is:pr is:open"
```

## JSON 输出

大多数命令支持 `--json` 输出结构化数据。可以使用 `--jq` 过滤：

```bash
gh issue list --repo owner/repo --json number,title --jq '.[] | "\(.number): \(.title)"'
```

## 使用技巧

### 查看状态
```bash
# 查看 PR 状态
gh pr status

# 查看 CI 状态
gh pr checks

# 查看评论
gh pr view 55 --comments
```

### 操作 PR
```bash
# 合并 PR
gh pr merge 55 --squash --delete-branch

# 关闭 PR
gh pr close 55 --comment "不再需要"

# 重新打开 PR
gh pr reopen 55
```

### 查看仓库信息
```bash
# 查看仓库信息
gh repo view owner/repo

# 查看仓库统计
gh repo view owner/repo --json name,description,stargazerCount

# 查看语言分布
gh repo view owner/repo --json languages --jq '.languages'
```

## 注意事项

1. **认证**：首次使用需要登录 `gh auth login`
2. **仓库选择**：在 git 目录中自动检测，否则需要指定 `--repo`
3. **输出格式**：使用 `--json` 和 `--jq` 处理复杂输出
4. **调试**：添加 `--debug` 标志查看详细信息
5. **限制**：注意 API 速率限制

## 常见用例

### 快速查看 PR 状态
```bash
gh pr view --json title,state,mergeable --jq '.state'
```

### 批量操作
```bash
# 关闭所有旧 PR
gh pr list --state open --json number --jq '.[].number' | xargs -I {} gh pr close {}
```

### 查看失败的工作流
```bash
gh run list --workflow=ci.yml --json databaseId,conclusion --jq '.[] | select(.conclusion == "failure") | .databaseId' | xargs -I {} gh run view {}
```

## 错误处理

### 常见错误

**未登录**：
```
错误: gh not authenticated
解决: gh auth login
```

**仓库未找到**：
```
错误: could not find repo
解决: 检查仓库名称或使用完整 owner/repo 格式
```

**权限不足**：
```
错误: resource not accessible
解决: 确认有访问权限或使用正确的认证 token
```
