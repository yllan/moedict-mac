# 萌典 Mac 版
教育部國語辭典轉換為 Mac 原生辭典計劃。

## Prerequisite
  * ☕ jdk ≥ 1.6
  * [scala 2.10.0+](http://www.scala-lang.org/downloads)
  * [sbt 0.12.2+](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html)
  * [Dictionary Development Kit](https://developer.apple.com/downloads/index.action) 登入後抓 Auxiliary Tools for Xcode。為了方便，我把 build 要用到的工具 copy 到 ./bin 裡面了，只是要 build 的話可以不用抓。

## How to Build
  
  1. 先抓 kcwu 的資料庫

    `curl http://kcwu.csie.org/~kcwu/tmp/moedict/development.sqlite3.bz2 | bzcat > development.sqlite3`

  2. 使用 g0v 的 db2unicode.pl 將資料庫中以圖代字的 `<img src="xxxx">` 的轉換成 unicode

        curl -O https://raw.github.com/g0v/moedict-epub/master/sym.txt
        curl https://raw.github.com/g0v/moedict-epub/master/db2unicode.pl | perl | sqlite3 development.unicode.sqlite3

  3. 將 development.unicode.sqlite3 轉換成 moedict_template/MoeDictionary.xml

        sbt run # 第一次跑 sbt 要抓很多東西會很久

  4. 利用 Apple 的 Dictionary Development Kit 將原始的 moedict_template 的資料轉換成我們要的資料

        cd moedict_templates
        make # 等一陣子，在我的機器上要花 12mins
        make install 

## Reference

[Apple Dictionary Services Programming Guide](https://developer.apple.com/library/mac/#documentation/UserExperience/Conceptual/DictionaryServicesProgGuide/Introduction/Introduction.html)

