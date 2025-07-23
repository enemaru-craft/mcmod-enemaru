# mcmod-enemaru
## IDEAの設定
1. **JDKのバージョンを設定**
- JDK-21をTemurinからダウンロード
- 設定（歯車アイコン）→プロジェクト構造→プロジェクトのタブからSDKを**temurin-21**に、言語レベルをSDKデフォルトに
2. **Gradleで使うJavaのバージョンを設定**
- GradleタブからGradle設定（歯車アイコン）を開く
- Gradle JVMをプロジェクトSDKに設定
3. **起動設定（Optional）**
- 実行構成の三点リーダを押して、構成→編集
- JetBrainsRuntimeをダウンロードしてきて、ProgramFilesなどに配置
- ビルドと実行のところから、配置したフォルダを選択
- Javaバージョン選択のドロップボックスの真下の起動時変数に以下を追記
    ```
    -XX:+AllowEnhancedClassRedefinition
    ```
