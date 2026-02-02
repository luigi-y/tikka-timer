#!/bin/bash

# Git hooks 설치 스크립트
# 사용법: ./scripts/install-hooks.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
HOOKS_DIR="$PROJECT_ROOT/.git/hooks"
SOURCE_DIR="$SCRIPT_DIR/git-hooks"

echo "📦 Installing git hooks..."

# pre-commit hook 설치
if [ -f "$SOURCE_DIR/pre-commit" ]; then
    cp "$SOURCE_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
    chmod +x "$HOOKS_DIR/pre-commit"
    echo "✅ pre-commit hook installed"
else
    echo "❌ pre-commit hook source not found"
    exit 1
fi

echo ""
echo "🎉 Git hooks installed successfully!"
echo ""
echo "설치된 hooks:"
echo "  - pre-commit: 커밋 전 ktlint 검사"
