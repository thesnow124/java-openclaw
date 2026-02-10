#!/usr/bin/env python3
"""
Fix incorrect tool imports that reference domain.agent.tools instead of domain.tool
"""

import re
from pathlib import Path

DOMAIN_DIR = Path("/Users/gaoshuanglong/IdeaProjects/java-openclaw-lite/src/main/java/com/openclawlite/openclaw/domain")

def fix_file(file_path):
    """Fix tool imports in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content

        # Fix: com.openclawlite.openclaw.domain.agent.tools -> com.openclawlite.openclaw.domain.tool
        content = re.sub(
            r'import com\.openclawlite\.openclaw\.domain\.agent\.tools\.',
            'import com.openclawlite.openclaw.domain.tool.',
            content
        )

        # Only write if changed
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        return False
    except Exception as e:
        print(f"  Error: {e}")
        return False

def main():
    """Main fix function"""
    fixed_count = 0

    print("=" * 70)
    print("Fixing Tool Imports")
    print("=" * 70)
    print()

    # Find all Java files in domain directory
    for java_file in DOMAIN_DIR.rglob("*.java"):
        # Check if file contains the incorrect import
        try:
            with open(java_file, 'r', encoding='utf-8') as f:
                content = f.read()
                if 'import com.openclawlite.openclaw.domain.agent.tools.' in content:
                    print(f"Fixing: {java_file.relative_to(DOMAIN_DIR.parent.parent.parent)}")
                    if fix_file(java_file):
                        print(f"  ✅ Fixed")
                        fixed_count += 1
        except Exception as e:
            print(f"  Error checking {java_file}: {e}")

    print()
    print("=" * 70)
    print(f"Fixed {fixed_count} files")
    print("=" * 70)

if __name__ == "__main__":
    main()
