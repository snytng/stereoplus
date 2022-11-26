# Stereo+プラグイン

## 説明
astah*プロジェクトのモデルにあるステレオタイプの追加、削除、ハイライトするプラグインです。

# ダウンロード
- [ここ](https://github.com/snytng/stereoplus/releases/latest)から`linkplus-<version>.jar`をダウンロードして下さい。

# インストール
- ダウンロードしたプラグインファイルをastah*アプリケーションにドラッグドロップするか、Program Files\asta-professionals\pluginsに置いて下さい。
- （注意）ashta* professional 8.4以上でしか動作しません。

# 機能説明
- Stereo+プラグインタブを開いて、編集したいクラス図の背景を一度クリックすると準備完了です。
  - 初期状態では規定のステレオタイプが編集できるボタンが並んでいます。 
- ステレオタイプを追加・削除します。
  - ステレオタイプを追加したいクラスを選択して、プラグインのステレオタイプをクリックするとステレオタイプを追加できます。
    - 同じ列にあるステレオタイプは排他選択になります。２つを同時に選べません。
    - 他の列にあるステレオタイプは同時に追加することができます。
  - 削除したい場合には同じ列にある`-----`ボタンをクリックしてください。
- ステレオタイプをハイライトします。
  - クラス図の背景を一度クリックした後に、プラグイン上の`ステレオタイプボタン`にマウスオーバーします。
    - ステレオタイプのついたクラスの背景色が、各ステレオタイプに対応付けられた色に一時的に変わります。
  - 先頭の`ステレオタイプグループボタン`にマウスオーバーします。
    - ステレオタイプグループのステレオタイプがあるクラスの色を変えます。
  - `Colorize`チェックボックスをOFFにするとハイライト機能をオフにします。
- クラス図で選択したクラスがどのステレオタイプを持っているかを表示します。
  - 選択したクラス図が持っている`ステレオタイプボタン`の文字と枠線が赤になります。
- 独自のステレオタイプグループを定義する
  - 独自のステレオタイプを定義します。
    - プロジェクトの中に`_stereotype/definition`という名前のクラス図を作ります。
    - クラス図にあるパッケージ名がステレオタイプグループ名に、クラス名がステレオタイプ名に、クラスの色がハイライト色になります。
  - 独自のステレオタイプを読み込みます。
    - プラグイン右側にある`Updateボタン`を押すと、独自のステレオタイプを読み込みます。
    - デフォルトに戻したいときには、`Defaultボタン`を押します。

## 免責事項
このastah* pluginは無償で自由に利用することができます。
このastah* pluginを利用したことによる、astah*ファイルの破損により生じた生じたあらゆる損害等について一切責任を負いません。

## 変更履歴
- V0.4
    - 公開版の初版リリース

以上
