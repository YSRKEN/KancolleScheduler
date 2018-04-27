# KancolleScheduler(艦これスケジューラー)
Expedition Schedule Maker by Java+JavaFX

# 概要
　[艦これ](http://www.dmm.com/netgame/feature/kancolle.html)の遠征スケジュールを組み立てるためのソフトウェアです。  
　Java製ですので動作OSを問いません。

# 使い方

　**KCS.jarをクリックすれば起動できます**。できない人は[JRE](https://java.com/ja/download/)を入れましょう。

<img src="attach:readme.png">

- この四角のブロックを「遠征タスク」と呼びます
- 遠征タスクをマウスでドラッグ＆ドロップすると移動できます
- 遠征タスクをダブルクリックすると、その遠征についての詳細情報が表示されます
- 遠征タスクの色は、獲得できる資材によって色分けしています(複数色ある場合はグラデーション)。

<img src="attach:readme2.png">

- スクロールバーの上・メニューバーの下にある範囲(タイムテーブル)において、
 - 遠征タスクのない場所を右クリックすると、挿入可能な遠征タスクの一覧が表示されます(クリックで追加)
 - 遠征タスクがある場所を右クリックすると、大発による収益増加率や各種フラグを変更でき、またコピー・削除もできます
 - 遠征タスクをコピーした場合、「遠征タスクがない場所」をクリックすると**「貼り付け操作」**が始まります
 - **「貼り付け操作」**の間、「遠征タスクがない場所」でマウスドラッグ操作を行うと、貼り付けられたタスクが出現し、**後はドラッグ＆ドロップの操作と同じです**
- 「ファイル(F)」メニューから、遠征スケジュールをCSV形式で保存・読み込み可能です
- 「操作(A)」メニューから、1日における遠征の収益を表示したり、全ての遠征タスクを削除したりできます
- 「ヘルプ(H)」メニューから、バージョン情報を表示できます

# 注意点

- 遠征スケジュール(CSV形式)の文字コードはUTF-8であり、外部のテキストエディタ等で文字コードを変えてしまうと正常に読み取れなくなります

# 謝辞

　Readme作成・表示に[かんたんMarkdown](http://tatesuke.github.io/KanTanMarkdown/)を使用させていただいております。

# 更新履歴

## Ver.1.3.0(2018/04/27)

 - 内部コードを大幅にリファクタリング
 - ドラッグ後に画面全体が白っぽくなったまま直らない不具合を修正
 - 遠征を色分けしてわかりやすくした

## Ver.1.2.0(2018/04/21)

 - 遠征タスクをコピー・貼り付けできるようにした
 - それぞれの遠征タスクについて、収益増加率や各種フラグを保存できるようにした

## Ver.1.1.0(2018/04/20)

 - マウスで遠征タスクをドラッグできなくなることがあった不具合を修正
 - 新遠征のデータを追加
 - 遠征スケジュールを保存する際、読み込みだけでなく書き込みでもUTF-8であることが保証されるようになった
 - 右クリックした際、不要なメニュー項目は表示しないようにした

## Ver.1.0.0(2018/04/19)

初版

# 作者

YSR([@YSRKEN](https://twitter.com/YSRKEN), [ysr.ken@gmail.com](mailto:ysr.ken@gmail.com))
