# TypeProf: 抽象解釈に基づくRubyコードの型解析ツール

## とりあえずデモ

[demo.md](demo.md) を参照。

## TypeProfの使い方

app.rb を解析する。

```
$ typeprof app.rb
```

一部のメソッドの型を指定した sig/app.rbs とともに app.rb を解析する。

```
$ typeprof sig/app.rbs app.rb
```

典型的な使用法は次の通り。

```
$ typeprof sig/app.rbs app.rb -o sig/app.gen.rbs
```

オプションは次の通り。

* `-o OUTFILE`: 標準出力ではなく、指定ファイル名に出力する
* `-q`: 解析の進捗を表示しない
* `-v`: `-fshow-errors`の別名
* `-d`: 解析の詳細ログを表示する（現状ではデバッグ用出力に近い）
* `-I DIR`: `require`のファイル探索ディレクトリを追加する
* `-r GEMNAME`: `GEMNAME`に対応するRBSをロードする
* `--exclude-dir DIR`: `DIR`以下のファイルの解析結果を出力から省略する。後に指定されているほうが優先される（`--include-dir foo --exclude-dir foo/bar`の場合う、foo/bar/baz.rbの結果は出力されず、foo/baz.rbの結果は出力される）。
* `--include-dir DIR`: `DIR`以下のファイルの解析結果を出力に含める。後に指定されているほうが優先される（`--exclude-dir foo --include-dir foo/bar`の場合、
foo/bar/baz.rbの結果は出力されるが、foo/baz.rbの結果は出力されない）。
* `--show-errors`: 実行中に見つけたバグの可能性を出力します（多くの場合、大量のfalse positiveが出ます）。
* `--show-untyped`: デフォルトでは`A | untyped`と推定されたところを単に`A`と出力しますが、より生の出力、つまり`A | untyped`と出力します。
* `--type-depth-limit=NUM`: （後で書く）

## TypeProfとは

TypeProfは、Rubyプログラムを型レベルで抽象的に実行するインタプリタです。
解析対象のプログラムを実行し、メソッドが受け取ったり返したりする型、インスタンス変数に代入される型を集めて出力します。
すべての値はオブジェクトそのものではなく、原則としてオブジェクトの所属するクラスに抽象化されます（次節で詳説）。

メソッドを呼び出す例を用いて説明します。

```
def foo(n)
  p n      #=> Integer
  n.to_s
end

p foo(42)  #=> String
```

TypeProfの解析結果は次の通り。

```
$ typeprof test.rb
# Revealed types
#  test.rb:2 #=> Integer
#  test.rb:6 #=> String

# Classes
class Object
  def foo : (Integer) -> String
end
```

`foo(42)`というメソッド呼び出しが実行されると、`Integer`オブジェクトの`42`ではなく、「`Integer`」という型（抽象値）が渡されます。
メソッド`foo`は`n.to_s`が実行します。
すると、組み込みメソッドの`Integer#to_s`が呼び出され、「String」という型が得られるので、メソッド`foo`はそれを返します。
これらの実行結果の観察を集めて、TypeProfは「メソッド`foo`は`Integer`を受け取り、`String`を返す」という情報をRBSの形式で出力します。
また、`p`の引数は`Revealed types`として出力されます。

インスタンス変数は、通常のRubyではオブジェクトごとに記憶される変数ですが、TypeProfではクラス単位に集約されます。

```
class Foo
  def initialize
    @a = 42
  end

  attr_accessor :a
end

Foo.new.a = "str"

p Foo.new.a #=> Integer | String
```

```
$ typeprof test.rb
# Revealed types
#  test.rb:11 #=> Integer | String

# Classes
class Foo
  attr_accessor a : Integer | String
  def initialize : -> Integer
end
```


## TypeProfの扱う抽象値

前述の通り、TypeProfはRubyの値を型のようなレベルに抽象化して扱います。
ただし、クラスオブジェクトなど、一部の値は抽象化しません。
紛らわしいので、TypeProfが使う抽象化された値のことを「抽象値」と呼びます。

TypeProfが扱う抽象値は次のとおりです。

* クラスのインスタンス
* クラスオブジェクト
* シンボル
* `untyped`
* 抽象値のユニオン
* コンテナクラスのインスタンス
* Procオブジェクト

クラスのインスタンスはもっとも普通の値です。
`Foo.new`というRubyコードが返す抽象値は、クラス`Foo`のインスタンスで、少し紛らわしいですがこれはRBS出力の中で`Foo`と表現されます。
`42`という整数リテラルは`Integer`のインスタンス、`"str"`という文字列リテラルは`String`のインスタンスになります。

クラスオブジェクトは、クラスそのものを表す値で、たとえば定数`Integer`や`String`に入っているオブジェクトです。
このオブジェクトは厳密にはクラス`Class`のインスタンスですが、`Class`に抽象化はされません。
抽象化してしまうと、定数の参照やクラスメソッドが使えなくなるためです。

シンボルは、`:foo`のようなSymbolリテラルが返す値です。
シンボルは、キーワード引数、JSONデータのキー、`Module#attr_reader`の引数など、具体的な値が必要になることが多いので、抽象化されません。
ただし、`String#to_sym`で生成されるSymbolや、式展開を含むSymbolリテラル（`:"foo_#{ x }"`など）はクラス`Symbol`のインスタンスとして扱われます。

`untyped`は、解析の限界や制限などによって追跡ができない場合に生成される抽象値です。
`untyped`に対する演算やメソッド呼び出しは無視され、評価結果は`untyped`となります。

抽象値のユニオンは、抽象値に複数の可能性があることを表現する値です。
人工的ですが、`rand < 0.5 ? 42 : "str"`の結果は`Integer | String`という抽象値になります。

コンテナクラスのインスタンスは、ArrayやHashのように他の抽象値を要素とするオブジェクトです。
いまのところ、ArrayとEnumeratorとHashのみ対応しています。
詳細は後述します。

Procオブジェクトは、ラムダ式（`-> { ... }`）やブロック仮引数（`&blk`）で作られるクロージャです。
これらは抽象化されず、コード片と結びついた具体的な値として扱われます。
これらに渡された引数や返された値によってRBS出力されます。


## TypeProfの実行

TypeProfは型レベルのRubyインタプリタと言いましたが、その実行順はRubyのものとはまったく異なります。

### 分岐

分岐は原則として、両方が並行に実行されます。どちらが先に評価されるかは決まっていません。

次の例では、then節では変数`x`にIntegerを代入し、else節では`x`にStringを代入しています。

```ruby
if rand < 0.5
  x = 42
else
  x = "str"
end

p x #=> Integer | String
```

まず条件式が評価され、then節とelse節の両方が実行され（どちらが先かはわかりません）、分岐後は最終的に`x`に`Integer | String`が入った状態でメソッド`p`を呼び出します。

### リスタート

インスタンス変数に複数の異なる抽象値を代入する場合、さらにややこしい実行順になります。

```ruby
class Foo
  def initialize
    @x = 1
  end

  def get_x
    @x
  end

  def update_x
    @x = "str"
  end
end

foo = Foo.new

# ...

p foo.get_x #=> Integer | String

# ...

foo.update_x
```

上記の例では、`Foo#initialize`の中でインスタンス変数`@x`にIntegerを代入しています。
`Foo#get_x`は`@x`を読み出し、一旦Integerを返します。
しかし、別の箇所で`Foo#update_x`を呼ぶと、インスタンス変数`@x`の値が`Integer | String`に拡張されます。
よって`@x`の読み出しはIntegerではなく`Integer | String`を返す必要があったものとして、遡及して実行し直します。
したがって、`Foo#get_x`の呼び出しの返り値は最終的に`Integer | String`となります。


### メソッド呼び出し

TypeProfは、実行中にコールスタックを管理しません。
よって、メソッドの実行中、「現在の呼び出し元」という概念を持ちません。
メソッドがリターンするときは、そのメソッドを呼び出しているすべての箇所に返り値の抽象値を返します。

```
def fib(n)
  if n < 2
    return n
  else
    fib(n - 1) + fib(n - 2)
  end
end

p fib(10) #=> Integer
```

上記の例では、メソッド`fib`は（再帰呼び出しを含めて）3箇所から呼び出されています。
`return n`が実行されると、これらの3箇所すべてからIntegerが帰ってきたものとして解析が実行されます。
なお、Rubyでは、メソッドの呼び出しの箇所を静的に特定することはできません（レシーバの型に依存するため）。
よって、`return n`を実行する時点より後で`fib`への呼び出しを発見した場合、直ちにIntegerが返されたものとして実行します。
もしメソッドが異なる抽象値を返す場合、実行の遡及が起きる場合があります。


### スタブ実行

実行できる箇所をすべて実行したあとでも、どこからも呼ばれなかったメソッドやブロックが残る場合があります。
これらのメソッドやブロックは、`untyped`を引数として無理やり呼び出されます。

```
def foo(n)
end

def bar(n)
  foo(1)
end
```

上記のプログラムだと、メソッド`foo`と`bar`がどちらも呼び出されませんが、`bar`をスタブ実行することで、`foo`にIntegerが渡されるという情報を取り出すことができます。

ただし、この機能は解析が遅くなったり、逆にノイズとなる場合もあるので、設定で有効化・無効化できるようにする予定です。


## TypeProfの制限など

値を抽象化するために、一部のRubyの言語機能は扱うことができません。

特異メソッドのように、オブジェクトのアイデンティティが重要な言語機能については基本的に無視されます。
ただし、クラスオブジェクトは抽象化されないため、クラスメソッドの定義は正しく処理されます。
Rubyにあるクラスのクラスのような概念はなく、現状ではインスタンスメソッドとクラスメソッドの2段階のみを扱います。

メタプログラミングは、一部のみ対応しています。
`Module#attr_reader`や`Object#send`は、引数として渡されるSymbolの中身が追跡できている場合（たとえばリテラルで書かれている場合）のみ、正しく扱います。
`Kernel#instance_eval`は、ブロックが渡された場合にレシーバオブジェクトを置き換える機能のみ対応しています（文字列の中身は追跡されない）。
`Class.new`は対応されません（`untyped`を返します）。
`Kernel#require`は引数の文字列がリテラルの場合のみ特別に対応しています。


## TypeProfの機能

### RBSの手書き指定

TypeProfは理論上の制限や実装上の制限により、プログラマの意図を正しく推定できない場合があります。
そのような場合、部分的にRBSを手書きし、TypeProfに意図を伝えることができます。

たとえばTypeProfはオーバーロードを推定できないので、次のようなコードを解析すると少し雑な推定となります。

```
# プログラマの意図：(Integer) -> Integer | (String) -> String
# TypeProfの推定：  (Integer | String) -> (Integer | String)
def foo(n)
  if n.is_a?(Integer)
    42
  else
    "str"
  end
end

# オーバーロードの意図が尊重されない
p foo(42)    #=> Integer | String
p foo("str") #=> Integer | String
```

`foo(42)`の結果は`Integer`になることを期待していますが、少し広い`Integer | String`となっています。
このようなとき、RBSを手書きしてメソッド`foo`の意図を指定すれば、意図通りの推定結果となります。

``` 
# test.rbs
class Object
  def foo: (Integer) -> Integer | (String) -> String
end
``` 
 
``` 
# test.rb
def foo(n)
  # 中身に関係なく、test.rbsの記述が優先される
end

# オーバーロードの意図が尊重されるようになる
p foo(42)    #=> Integer
p foo("str") #=> String
``` 

組み込みクラス・メソッドの多くもRBSによって指定されています。
GemfileがあればそこからライブラリのRBSをまとめてロードする機能は実装予定です（未実装）。

なお、RBSのインターフェイスは未サポートで、`untyped`として扱われます。

### デバッグ機能

TypeProfの挙動・解析結果を理解するのはなかなか難しい問題です。

現状では、コード中で`Kernel#p`を呼び出すことで、引数の抽象値を観察することができます。
それ以上に解析の挙動を深く理解するには、現状では環境変数`TP_DEBUG=1`を指定してデバッグ出力を観察するしかありません。
解析結果の説明性は課題と認識していて、今後拡充していく予定です。


### flow-sensitive analysis

ユニオンの抽象値の中身を見る分岐では、可能な範囲でユニオンの分離を行います。
たとえばローカル変数`var`の抽象値が`Foo | Bar`、分岐の条件式に`var.is_a?(Foo)`などと書かれている場合、then節で変数`var`は`Foo`に、else節では`Bar`に分離されます。

ただし、レシーバがそのスコープで定義されたローカル変数である場合に限ります（`@var.is_a?(Foo)`や、ブロックの外側の変数`x`に対する`x.is_a?(Foo)`ではユニオンは分離されない）。また、現時点では`is_a?`、`respond_to?`、`case`/`when`で次のように書かれているパターンのみに限定されています。

```
def foo(x)
  if x.is_a?(Integer)
    p x #=> Integer
  else
    p x #=> String
  end
end

foo(42)
foo("str")
```

```
def foo(x)
  if x.respond_to?(:times)
    p x #=> Integer
  else
    p x #=> String
  end
end

foo(42)
foo("str")
```

```
def foo(x)
  case x
  when Integer
    p x #=> Integer
  when String
    p x #=> String
  end
end

foo(42)
foo("str")
```


### コンテナ型

いまのところ、Array型のコンテナ（ArrayとEnumerator）とHash型のコンテナ（Hash）のみ対応しています。

メソッド内ではオブジェクトのアイデンティティを保持していて（オブジェクトの生成場所で識別される）、それなりに更新などできます。
これにより、次のように配列を初期化するコードは動作します。

```
def foo
  a = []
  
  100.times {|n| a << n.to_s }
  
  p a #=> Array[String]
end

foo
```

ただし、解析のパフォーマンスの都合で、メソッドをまたがった更新の追跡は行いません。

```
def bar(a)
  a << "str"
end

def foo
  a = []
  bar(a)
  p a #=> []
end

foo
```

インスタンス変数に入っている配列に対する更新は特別に追跡するようになっています。

現在の制限として、ハッシュのキーにコンテナ型を入れる場合は`untyped`に置き換えられます。
また、入れ子の配列やハッシュは、深さ5までに制限されています。
これらは解析パフォーマンスの都合であり、設定可能にしたり、手動でRBS指定した場合は深さ制限を超えられるようにするなどの改良をする予定です。


### その他

後で書く

* Proc
* Struct
