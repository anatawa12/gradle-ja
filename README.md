<img src="http://gradle.org/img/gradle_logo.gif" />

Gradleは、ビルドの自動化および複数言語での開発をサポートするビルドツールです。
ソフトウェアのビルドやテスティング、様々なプラットフォームへの公開やデプロイに対し、Gradleは柔軟な運用モデルを提供し、コードのコンパイルやパッケージングからウェブサイトの公開まで、開発サイクルの全てを効率化します。
GradleはJavaやScala、Android、C/C++、Groovyなど複数言語とプラットフォームにまたがる自動化を前提に設計されており、またEclipseやIntelliJ、Jenkinsなど様々な開発ツールやインテグレーションサーバーと自然に統合させることができます。

詳細な情報については http://gradle.org を参照してください。

## ダウンロード

リリースバージョンおよびナイトリービルド版が http://gradle.org/downloads からダウンロードできます。

## ビルド

当然ですが、GradleはGradleによってビルドされています。Gradleは[ラッパー](http://gradle.org/docs/current/userguide/gradle_wrapper.html)という先進的な機能を備えているので、手でGradleをインストールしなくてもGradleを使って作業することができます。ラッパーはWindowsではバッチスクリプトとして、その他のOSではシェルスクリプトとして提供されます。

このプロジェクトは、そのラッパーを使ってビルドするべきです。一般的に言っても、ラッパーが同梱されているGradleプロジェクトはラッパーを使ってビルドしてください。それにより、プロジェクトが想定する正しいバージョンのGradleでビルドできます。

Gradleプロジェクト全体をビルドするには、チェックアウトしたディレクトリのルートで次のコマンドを実行してください。

    ./gradlew build

これで、全てのコードがコンパイルされ、全ドキュメントが生成され、テストも全て実施されます。これには、速いマシンでも一時間程度掛かるかもしれません。Gradleの全ての機能を仮想的に実行する結合テストを含む、数千ものテストが実行されるためです。テスト項目には、バージョン間の互換性チェック、サンプルやJavadoc内のスニペットが正しいかどうかの確認、デーモンプロセスの機能確認などが含まれています。

### ソースからインストールする

ソースツリーからインストールするには、以下のいずれかのコマンドを実行してください。

    ./gradlew install -Pgradle_installPath=/usr/local/gradle-source-build

これは、最小構成のインストールを行うコマンドです。Gradleを実行するのに必要なものだけがインストールされます（ドキュメントなどは含まれない）。`-Pgradle_installPath`でインストール先を指定しています。

インストールが完了すれば、以下のようにしてビルドを実行できます。

    /usr/local/gradle-source-build/bin/gradle ≪some task≫

ドキュメントなどを含むフルインストールを行うには次のコマンドを実行します。

    ./gradlew installAll -Pgradle_installPath=/usr/local/gradle-source-build

### サブプロジェクトで作業する

本プロジェクトのビルドでは、マルチプロジェクトの論理的な構造をカスタマイズできるGradleの機能を利用しています。本ビルドの全てのサブプロジェクトは`subprojects/`ディレクトリに置かれており、さらに[settings.gradle](https://github.com/gradle/gradle/blob/master/settings.gradle)の設定によりトップレベルの子プロジェクトとしてマップされています。

このため、例えば`core`サブプロジェクトは`subprojects/core`にありますが、次のようにしてビルドできます。

    ./gradlew core:build

docプロジェクトの場合はこうです。

    ./gradlew docs:build

その他のサブプロジェクトについても同様です。

## プロジェクトへの参加

Gradleプロジェクトに協力したい、またパッチやプルリクエストを送りたいといった際は、http://gradle.org/development を参照してください。開発者に連絡を取る方法について知ることができます。

### コードへのコントリビューション

ビルドというのはとても複雑なトピックなので、どなたであってもGradleのコードベースで作業を始めるならGradleの開発チームは喜んでサポートします。
ビルド時に困った点を修正したいといった場合は、細かいことでも遠慮なく開発チームまでご連絡ください。

単に何かを修正したいときや小さな機能を追加したいといった場合は、修正を加えたサブプロジェクトで`check`タスクを走らせれば、品質的にはたいてい十分です。たとえば、そのパッチが`launcher'に対するものであれば、次のようにタスクを実行します。

    ./gradlew launcher:check

これで、そのモジュールに対するテストとコード品質のチェックが実施されます。

### ドキュメントへのコントリビューション

[docs subproject](https://github.com/gradle/gradle/tree/master/subprojects/docs)のREADMEを参照してください。

## IDEで開く

### IntelliJ IDEA

プロジェクトのルートディレクトリで次のコマンドを実行するだけで、IDEAで開くことができるようになります。

    ./gradlew idea

このコマンドは、IDEAがプロジェクトを認識できるように適切なIDEAメタデータを生成します。

ただし、IDEAの不具合により、IDEAでのGradleのビルドは初回に限り失敗します。コンパイルエラーが発生した場合はビルドを再実行してください。

### Eclipse

#### Gradle Integration for Eclipse (by Pivotal)

The Gradle project is not currently buildable in Eclipse. This is something that will be rectified in the future.

次のコマンドを実行します。

    ./gradlew eclipse

This command generates Eclipse metadata that allows importing the project into Eclipse. However, you will have to do some manual fixes to the project's setup to make it work.

#### Gradle for Eclipse (by Nodeclipse/Enide)

With [Gradle(Enide) Eclipse plugin](http://marketplace.eclipse.org/content/gradle), you can import as general plugin or prepare before with `./gradlew eclipse`. 

Build is run via right-click on `build.gradle` <kbd>Run As -> gradle build Gradle Build</kbd>
