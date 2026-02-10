#!/usr/bin/env python3
"""
Fix package declarations that were missed during migration
"""

import os
import re
from pathlib import Path

BASE_DIR = Path("/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/src/main/java/com/openclawlite/openclaw/domain")

# Package fixes needed
PACKAGE_FIXES = {
    "tool/": ("package com.openclawlite.agent.tools;", "package com.openclawlite.openclaw.domain.tool;"),
}

def fix_package_in_file(file_path, old_package, new_package):
    """Fix package declaration in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Check if old package exists
        if old_package not in content:
            return False

        # Replace package
        content = content.replace(old_package, new_package)

        # Write back
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)

        return True
    except Exception as e:
        print(f"  Error processing {file_path}: {e}")
        return False

def main():
    """Main fix function"""
    fixed_count = 0

    print("=" * 70)
    print("Fixing Package Declarations")
    print("=" * 70)
    print()

    # Fix tool/ directory
    tool_dir = BASE_DIR / "tool"
    if tool_dir.exists():
        for java_file in tool_dir.glob("*.java"):
            print(f"Checking: {java_file.name}")
            if fix_package_in_file(java_file,
                                   "package com.openclawlite.agent.tools;",
                                   "package com.openclawlite.openclaw.domain.tool;"):
                print(f"  ✅ Fixed package declaration")
                fixed_count += 1
            else:
                print(f"  ℹ️  No fix needed or already correct")

    print()
    print("=" * 70)
    print(f"Fixed {fixed_count} files")
    print("=" * 70)

if __name__ == "__main__":
    main()
