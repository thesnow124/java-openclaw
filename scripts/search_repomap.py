#!/usr/bin/env python3
import argparse
import json
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Tuple


DEFAULT_BACKEND_MAP_FILE = "/Users/gaoshuanglong/Desktop/PmsCode/back/repomap_output.txt"
DEFAULT_FRONTEND_MAP_FILE = "/Users/gaoshuanglong/Desktop/PmsCode/front/repomap_output.txt"
DEFAULT_MAP_FILE = DEFAULT_BACKEND_MAP_FILE

PATH_LINE_RE = re.compile(r"^[\(']*\s*(/[^:\n]+):\s*$")
RANK_LINE_RE = re.compile(r"^\(Rank value:\s*([0-9.]+)\)")
CODE_LINE_RE = re.compile(r"^\s*(\d+):\s*(.*)$")
TERM_RE = re.compile(r"[A-Za-z0-9_./:-]{2,}|[\u4e00-\u9fff]{2,}")


@dataclass
class MapEntry:
    path: str
    rank: float
    block: str
    source_map: str


def normalize_text(raw_text: str) -> str:
    # RepoMapper output may contain escaped newlines.
    if raw_text.count("\n") < 20 and "\\n" in raw_text:
        raw_text = raw_text.replace("\\r\\n", "\n").replace("\\n", "\n")
    return raw_text


def parse_entries(raw_text: str, source_map: str) -> List[MapEntry]:
    lines = normalize_text(raw_text).splitlines()
    entries: List[MapEntry] = []
    i = 0

    while i < len(lines):
        path_match = PATH_LINE_RE.match(lines[i].strip())
        if not path_match:
            i += 1
            continue

        path = path_match.group(1)
        i += 1
        rank = 0.0

        if i < len(lines):
            rank_match = RANK_LINE_RE.match(lines[i].strip())
            if rank_match:
                rank = float(rank_match.group(1))
                i += 1

        block_lines: List[str] = []
        while i < len(lines):
            current = lines[i]
            if PATH_LINE_RE.match(current.strip()):
                break
            if "FileReport(" in current:
                i = len(lines)
                break
            block_lines.append(current)
            i += 1

        entries.append(
            MapEntry(
                path=path,
                rank=rank,
                block="\n".join(block_lines).strip(),
                source_map=source_map,
            )
        )

    return entries


def extract_terms(query: str) -> List[str]:
    raw_terms = TERM_RE.findall(query)
    if not raw_terms and query.strip():
        raw_terms = [query.strip()]

    terms: List[str] = []
    seen = set()
    for term in raw_terms:
        key = term.lower() if term.isascii() else term
        if key in seen:
            continue
        seen.add(key)
        terms.append(term)
    return terms


def contains_term(text: str, term: str) -> bool:
    if term.isascii():
        return term.lower() in text.lower()
    return term in text


def count_term(text: str, term: str) -> int:
    if term.isascii():
        return text.lower().count(term.lower())
    return text.count(term)


def collect_matched_lines(block: str, matched_terms: List[str], max_lines: int) -> List[Dict[str, object]]:
    if max_lines <= 0:
        return []

    results: List[Dict[str, object]] = []
    for line in block.splitlines():
        code_line_match = CODE_LINE_RE.match(line)
        if not code_line_match:
            continue
        text = code_line_match.group(2).strip()
        if not any(contains_term(text, term) for term in matched_terms):
            continue
        results.append(
            {
                "line": int(code_line_match.group(1)),
                "text": text[:220],
            }
        )
        if len(results) >= max_lines:
            break
    return results


def search_entries(entries: List[MapEntry], terms: List[str], show_lines: int) -> List[Dict[str, object]]:
    hits: List[Dict[str, object]] = []

    for entry in entries:
        score = entry.rank
        matched_terms: List[str] = []

        for term in terms:
            path_hits = count_term(entry.path, term)
            block_hits = count_term(entry.block, term)
            if path_hits == 0 and block_hits == 0:
                continue
            matched_terms.append(term)
            score += path_hits * 6.0 + block_hits * 1.5

        if not matched_terms:
            continue

        if len(matched_terms) == len(terms):
            score += 4.0

        hits.append(
            {
                "path": entry.path,
                "source_map": entry.source_map,
                "rank": round(entry.rank, 4),
                "score": round(score, 4),
                "matched_keywords": matched_terms,
                "matched_lines": collect_matched_lines(entry.block, matched_terms, show_lines),
            }
        )

    hits.sort(key=lambda item: (-item["score"], -item["rank"], item["source_map"], item["path"]))
    return hits


def selected_map_sources(args: argparse.Namespace) -> List[Tuple[str, Path]]:
    if args.map_file:
        return [("custom", Path(args.map_file))]

    if args.scope == "backend":
        return [("backend", Path(args.backend_map_file))]
    if args.scope == "frontend":
        return [("frontend", Path(args.frontend_map_file))]

    return [
        ("backend", Path(args.backend_map_file)),
        ("frontend", Path(args.frontend_map_file)),
    ]


def load_entries(map_sources: List[Tuple[str, Path]]) -> Tuple[List[MapEntry], List[str]]:
    entries: List[MapEntry] = []
    warnings: List[str] = []

    for source_name, map_path in map_sources:
        if not map_path.exists():
            warnings.append(f"Map file not found for {source_name}: {map_path}")
            continue

        raw_text = map_path.read_text(encoding="utf-8", errors="replace")
        parsed = parse_entries(raw_text, source_name)
        if not parsed:
            warnings.append(f"No entries parsed for {source_name}: {map_path}")
            continue

        entries.extend(parsed)

    return entries, warnings


def unique_paths_from_hits(hits: List[Dict[str, object]], top: int) -> List[str]:
    max_items = max(top, 1)
    paths: List[str] = []
    seen = set()

    for hit in hits:
        path = str(hit["path"])
        if path in seen:
            continue
        seen.add(path)
        paths.append(path)
        if len(paths) >= max_items:
            break

    return paths


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Search RepoMapper snapshot outputs and return top candidate files."
    )
    parser.add_argument("--query", required=True, help="Keywords, API path, class name, or error text.")
    parser.add_argument(
        "--scope",
        choices=["backend", "frontend", "all"],
        default="all",
        help="Which RepoMap source set to query when --map-file is not provided.",
    )
    parser.add_argument(
        "--map-file",
        default=None,
        help=(
            "Single RepoMap file path. If provided, it overrides --scope and backend/frontend map file settings. "
            f"Legacy default was {DEFAULT_MAP_FILE}."
        ),
    )
    parser.add_argument(
        "--backend-map-file",
        default=DEFAULT_BACKEND_MAP_FILE,
        help=f"Backend RepoMap file. Default: {DEFAULT_BACKEND_MAP_FILE}",
    )
    parser.add_argument(
        "--frontend-map-file",
        default=DEFAULT_FRONTEND_MAP_FILE,
        help=f"Frontend RepoMap file. Default: {DEFAULT_FRONTEND_MAP_FILE}",
    )
    parser.add_argument("--top", type=int, default=20, help="Max number of matched files to return.")
    parser.add_argument(
        "--show-lines",
        type=int,
        default=3,
        help="How many matched snippet lines to return per file (ignored for --paths-only).",
    )
    parser.add_argument(
        "--paths-only",
        action="store_true",
        help="Print matched file paths only (one path per line).",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Print JSON output instead of plain text.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    terms = extract_terms(args.query)
    if not terms:
        print("[ERROR] No usable query terms.", file=sys.stderr)
        return 2

    map_sources = selected_map_sources(args)
    entries, warnings = load_entries(map_sources)
    if not entries:
        for warning in warnings:
            print(f"[WARN] {warning}", file=sys.stderr)
        checked = ", ".join(str(path) for _, path in map_sources)
        print(f"[ERROR] No searchable entries loaded. Checked: {checked}", file=sys.stderr)
        return 2

    all_hits = search_entries(entries, terms, args.show_lines)
    max_items = max(args.top, 1)
    final_hits = all_hits[:max_items]

    if args.paths_only:
        for path in unique_paths_from_hits(all_hits, args.top):
            print(path)
        return 0

    payload = {
        "scope": "custom" if args.map_file else args.scope,
        "query": args.query,
        "terms": terms,
        "map_files": {name: str(path) for name, path in map_sources},
        "warnings": warnings,
        "entry_count": len(entries),
        "hit_count": len(final_hits),
        "hits": final_hits,
    }

    if args.json:
        print(json.dumps(payload, ensure_ascii=False, indent=2))
        return 0

    print(f"Scope: {payload['scope']}")
    print(f"Map files: {', '.join(f'{k}={v}' for k, v in payload['map_files'].items())}")
    if warnings:
        for warning in warnings:
            print(f"Warning: {warning}")
    print(f"Query terms: {', '.join(terms)}")
    print(f"Parsed entries: {payload['entry_count']}")
    print(f"Matched files: {payload['hit_count']}")
    for idx, hit in enumerate(final_hits, start=1):
        print(
            f"{idx}. [{hit['source_map']}] {hit['path']} "
            f"(score={hit['score']:.4f}, rank={hit['rank']:.4f}, terms={','.join(hit['matched_keywords'])})"
        )
        for line in hit["matched_lines"]:
            print(f"   {line['line']}: {line['text']}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
