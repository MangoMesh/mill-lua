# Dependencies

mill-lua was built with simplification in mind. It provides a declarative and simple
way of declaring dependencies avaliable as LuaRocks.

```scala
override def luarocks = T{ Agg(Rock("lua-cjson", "2.1.0.10-1"), Rock("...", "x.y.z")) }
```

these dependencies are downloaded on launch and cached. They are already avaliable for your
project without the need of setting paths or of a global instalation.

## Metaprogramming, generated files, et al.

You can override the following in your `build.sc`:

```scala
def generatedSources: T[Seq[PathRef]] = T { Seq.empty[PathRef] }
```