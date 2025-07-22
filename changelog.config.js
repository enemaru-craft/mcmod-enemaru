module.exports = {
    disableEmoji: false,
    format: '{emoji}{type}: {subject}',
    list: ['add', 'change', 'remove', 'wip', 'test', 'feat', 'fix', 'chore', 'docs', 'refactor', 'style', 'perf'],
    maxMessageLength: 64,
    minMessageLength: 3,
    questions: ['type', 'subject', 'body', 'breaking', 'issues'],
    scopes: [],
    types: {
      add: {
        description: 'ファイル・コードの追加',
        emoji: '➕',
        value: 'add'
      },
      change: {
        description: 'ファイル・コードの変更',
        emoji: '🔁',
        value: 'change'
      },
      remove: {
        description: 'ファイルなどの削除',
        emoji: '🗑️',
        value: 'remove'
      },
      wip: {
        description: '作業中のコミット',
        emoji: '🚧',
        value: 'wip'
      },
      chore: {
        description: 'ドキュメントの生成やビルドプロセス、ライブラリなどの変更',
        emoji: '🤖',
        value: 'chore'
      },
      docs: {
        description: 'ドキュメントのみの変更',
        emoji: '✏️',
        value: 'docs'
      },
      feat: {
        description: '新機能',
        emoji: '✨',
        value: 'feat'
      },
      fix: {
        description: '不具合の修正',
        emoji: '🐛',
        value: 'fix'
      },
      perf: {
        description: 'パフォーマンス改善を行うためのコードの変更',
        emoji: '⚡️',
        value: 'perf'
      },
      refactor: {
        description: 'バグ修正や機能の追加を行わないコードの変更',
        emoji: '💡',
        value: 'refactor'
      },
      style: {
        description: 'コードの処理に影響しない変更（スペースや書式設定など）',
        emoji: '🎨',
        value: 'style'
      },
      test: {
        description: 'テストコードの変更',
        emoji: '🚨',
        value: 'test'
      },
      messages: {
        type: 'Select the type of change that you\'re committing:',
        customScope: 'Select the scope this component affects:',
        subject: 'Write a short, imperative mood description of the change:\n',
        body: 'Provide a longer description of the change:\n ',
        breaking: 'List any breaking changes:\n',
        footer: 'Issues this commit closes, e.g #123:',
        confirmCommit: 'The packages that this commit has affected\n',
      },
    }
  };