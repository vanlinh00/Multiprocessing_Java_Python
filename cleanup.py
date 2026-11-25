import argparse
import json
import os
import sys
import time
from pathlib import Path


def clean_temp_workspace(workspace_dir: Path, max_age_seconds: int, dry_run: bool) -> dict:
    workspace_dir.mkdir(parents=True, exist_ok=True)
    now = time.time()
    cleaned_files = []
    bytes_freed = 0

    for entry in sorted(workspace_dir.iterdir()):
        if not entry.is_file():
            continue
        if not (entry.name.endswith(".tmp") or entry.name.endswith(".bak")):
            continue

        stat = entry.stat()
        age_seconds = now - stat.st_mtime
        if age_seconds < max_age_seconds:
            continue

        bytes_freed += stat.st_size
        cleaned_files.append(entry.name)
        if not dry_run:
            entry.unlink()

    return {
        "workspace": str(workspace_dir.resolve()),
        "dry_run": dry_run,
        "cleaned_count": len(cleaned_files),
        "bytes_freed": bytes_freed,
        "files": cleaned_files,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Isolated temporary file cleanup worker.")
    parser.add_argument(
        "--dir",
        default=os.environ.get("CLEANUP_WORKSPACE_DIR", "./tmp_workspace"),
        help="Target temporary workspace directory.",
    )
    parser.add_argument(
        "--max-age-seconds",
        type=int,
        default=0,
        help="Minimum file age in seconds before deletion.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Report files eligible for cleanup without deleting them.",
    )
    args = parser.parse_args()

    # Simulate external process work isolated from the main backend memory
    time.sleep(1)

    result = clean_temp_workspace(
        workspace_dir=Path(args.dir),
        max_age_seconds=max(0, args.max_age_seconds),
        dry_run=args.dry_run,
    )

    count = result["cleaned_count"]
    print(f"Cleaned {count} temporary files successfully! ({result['bytes_freed']} bytes freed)")
    print(f"CLEANUP_SUMMARY_JSON:{json.dumps(result)}", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
