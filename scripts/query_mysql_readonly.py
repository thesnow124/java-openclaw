#!/usr/bin/env python3
import argparse
import datetime
import decimal
import json
import os
import re
import sys
from typing import Dict, List, Tuple
from urllib.parse import parse_qsl, urlparse


BLOCKED_KEYWORDS = {
    "insert",
    "update",
    "delete",
    "replace",
    "truncate",
    "drop",
    "alter",
    "create",
    "grant",
    "revoke",
    "call",
    "merge",
    "execute",
    "prepare",
    "deallocate",
    "lock",
    "unlock",
    "set",
    "transaction",
    "begin",
    "commit",
    "rollback",
    "analyze",
    "optimize",
    "repair",
    "checksum",
    "flush",
    "use",
}

BLOCKED_PATTERNS = [
    r"\binto\s+outfile\b",
    r"\binto\s+dumpfile\b",
    r"\bload\s+data\b",
]


def print_error(message: str) -> None:
    print(f"[ERROR] {message}", file=sys.stderr)


def get_env_value(primary_key: str, fallback_key: str) -> str:
    value = os.getenv(primary_key)
    if value:
        return value
    value = os.getenv(fallback_key)
    if value:
        return value
    raise ValueError(
        f"Missing environment variable '{primary_key}' "
        f"(fallback '{fallback_key}' is also empty)."
    )


def load_db_config() -> Dict[str, str]:
    return {
        "url": get_env_value("purchase.datasource.url", "PURCHASE_DATASOURCE_URL"),
        "username": get_env_value(
            "purchase.datasource.username", "PURCHASE_DATASOURCE_USERNAME"
        ),
        "password": get_env_value(
            "purchase.datasource.password", "PURCHASE_DATASOURCE_PASSWORD"
        ),
    }


def parse_jdbc_mysql_url(jdbc_url: str) -> Dict[str, object]:
    prefix = "jdbc:mysql://"
    if not jdbc_url.startswith(prefix):
        raise ValueError("Only JDBC MySQL URL is allowed, for example: jdbc:mysql://...")

    parsed = urlparse(jdbc_url[len("jdbc:") :])
    host = parsed.hostname
    port = parsed.port or 3306
    database = parsed.path.lstrip("/")
    params = dict(parse_qsl(parsed.query, keep_blank_values=True))

    if not host or not database:
        raise ValueError("Invalid JDBC URL, host or database is missing.")

    return {
        "host": host,
        "port": port,
        "database": database,
        "charset": params.get("characterEncoding", "utf8mb4"),
        "use_ssl": params.get("useSSL", "").lower() == "true",
    }


def strip_comments(sql: str) -> str:
    sql = re.sub(r"/\*.*?\*/", " ", sql, flags=re.DOTALL)
    sql = re.sub(r"--[^\n\r]*", " ", sql)
    sql = re.sub(r"#[^\n\r]*", " ", sql)
    return sql


def strip_literals(sql: str) -> str:
    sql = re.sub(r"'(?:''|\\'|[^'])*'", "''", sql)
    sql = re.sub(r'"(?:\\"|[^"])*"', '""', sql)
    sql = re.sub(r"`(?:``|[^`])*`", "``", sql)
    return sql


def ensure_readonly_sql(raw_sql: str, default_limit: int) -> str:
    sql = raw_sql.strip()
    if not sql:
        raise ValueError("SQL is empty.")
    if ";" in sql:
        raise ValueError("Semicolon is forbidden. Multi-statement execution is not allowed.")

    sql_no_comments = strip_comments(sql).strip()
    if not re.match(r"(?is)^(select|with)\b", sql_no_comments):
        raise ValueError("Only SELECT or WITH query is allowed.")

    sql_for_check = strip_literals(sql_no_comments).lower()
    tokens = set(re.findall(r"\b[a-z_]+\b", sql_for_check))
    blocked_tokens = sorted(tokens & BLOCKED_KEYWORDS)
    if blocked_tokens:
        raise ValueError(
            "Readonly check failed, forbidden keyword found: "
            + ", ".join(blocked_tokens)
        )

    for pattern in BLOCKED_PATTERNS:
        if re.search(pattern, sql_for_check, flags=re.IGNORECASE):
            raise ValueError(f"Readonly check failed, forbidden pattern found: {pattern}")

    if not re.search(r"(?i)\blimit\b", sql_no_comments):
        sql_no_comments = f"{sql_no_comments.rstrip()} LIMIT {default_limit}"

    return sql_no_comments


def analyze_explain(explain_rows: List[Dict[str, object]], full_scan_rows: int) -> List[str]:
    risks: List[str] = []
    for row in explain_rows:
        access_type = str(row.get("type", "")).upper()
        table_name = str(row.get("table", "unknown"))
        rows_value = row.get("rows")
        try:
            estimated_rows = int(rows_value) if rows_value is not None else 0
        except (ValueError, TypeError):
            estimated_rows = 0

        if access_type == "ALL" and estimated_rows >= full_scan_rows:
            risks.append(
                f"Potential full scan on table '{table_name}' "
                f"(type=ALL, rows={estimated_rows})."
            )
    return risks


def json_default(value):
    if isinstance(value, decimal.Decimal):
        return str(value)
    if isinstance(value, (datetime.datetime, datetime.date, datetime.time)):
        return value.isoformat()
    if isinstance(value, bytes):
        return value.decode("utf-8", errors="replace")
    return str(value)


def run_query(sql: str, db_conf: Dict[str, str], full_scan_rows: int) -> Tuple[List[Dict], List[Dict]]:
    try:
        import pymysql
    except ImportError as exc:
        raise RuntimeError(
            "Missing dependency 'pymysql'. Install with: python3 -m pip install pymysql"
        ) from exc

    jdbc_conf = parse_jdbc_mysql_url(db_conf["url"])
    connect_kwargs = {
        "host": jdbc_conf["host"],
        "port": jdbc_conf["port"],
        "user": db_conf["username"],
        "password": db_conf["password"],
        "database": jdbc_conf["database"],
        "charset": jdbc_conf["charset"],
        "cursorclass": pymysql.cursors.DictCursor,
        "connect_timeout": 10,
        "read_timeout": 30,
        "write_timeout": 30,
        "autocommit": True,
    }
    if jdbc_conf["use_ssl"]:
        connect_kwargs["ssl"] = {}

    with pymysql.connect(**connect_kwargs) as conn:
        with conn.cursor() as cursor:
            explain_sql = f"EXPLAIN {sql}"
            cursor.execute(explain_sql)
            explain_rows = list(cursor.fetchall())

            risks = analyze_explain(explain_rows, full_scan_rows)
            if risks:
                raise RuntimeError("EXPLAIN risk check failed: " + " | ".join(risks))

            cursor.execute(sql)
            result_rows = list(cursor.fetchall())
            return explain_rows, result_rows


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Run readonly MySQL queries with safety checks (EXPLAIN first)."
    )
    parser.add_argument("--sql", required=True, help="Single SELECT/WITH SQL statement.")
    parser.add_argument(
        "--limit",
        type=int,
        default=200,
        help="Default LIMIT to append when SQL has no LIMIT.",
    )
    parser.add_argument(
        "--full-scan-rows",
        type=int,
        default=200000,
        help="Block when EXPLAIN has type=ALL and rows >= threshold.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.limit <= 0:
        print_error("--limit must be > 0.")
        return 2
    if args.full_scan_rows <= 0:
        print_error("--full-scan-rows must be > 0.")
        return 2

    try:
        db_conf = load_db_config()
        safe_sql = ensure_readonly_sql(args.sql, args.limit)
        explain_rows, result_rows = run_query(safe_sql, db_conf, args.full_scan_rows)
    except Exception as exc:
        print_error(str(exc))
        return 1

    output = {
        "safe_sql": safe_sql,
        "row_count": len(result_rows),
        "explain": explain_rows,
        "rows": result_rows,
    }
    print(json.dumps(output, ensure_ascii=False, indent=2, default=json_default))
    return 0


if __name__ == "__main__":
    sys.exit(main())
