---
name: weather
description: 获取当前天气和预报（无需 API 密钥）
homepage: https://wttr.in/:help
metadata: { "openclaw": { "emoji": "🌤️", "requires": { "bins": ["curl"] } } }
---

# 天气查询

两个免费服务，无需 API 密钥。

## wttr.in（主要服务）

快速单行命令：

```bash
curl -s "wttr.in/Beijing?format=3"
# 输出：Beijing: ⛅️ +8°C
```

紧凑格式：

```bash
curl -s "wttr.in/Shanghai?format=%l:+%c+%t+%h+%w"
# 输出：Shanghai: ⛅️ +8°C 71% ↙5km/h
```

完整预报：

```bash
curl -s "wttr.in/Guangzhou?T"
```

格式代码：`%c` 天气 · `%t` 温度 · `%h` 湿度 · `%w` 风速 · `%l` 地点 · `%m` 月亮

提示：

- URL 编码空格：`wttr.in/New+York`
- 机场代码：`wttr.in/PEK`
- 单位：`?m`（公制）`?u`（美制）
- 仅今天：`?1` · 仅当前：`?0`
- PNG：`curl -s "wttr.in/Beijing.png" -o /tmp/weather.png`

## Open-Meteo（备用服务，JSON）

免费，无需密钥，适合程序化使用：

```bash
curl -s "https://api.open-meteo.com/v1/forecast?latitude=39.9&longitude=116.4&current_weather=true"
```

查找城市的坐标，然后查询。返回 JSON 格式的温度、风速、天气代码。

文档：https://open-meteo.com/en/docs
