# mill-lua
## A Lua integration for the Mill build system

This module provides Lua integration within the Mill build system, enabling
a faster development and better tooling.

## Motivation

I think Lua lacks a proper "build" system like Poetry, NPM or similar. And mantaining a
good enviroment configuration can be quite annoying. So this comes as a solution to
integrate the current tooling avaliable (Sumneko's LSP, LuaRocks) under a simpler
solution which aims to improve the development experience.

## The basic

Installation of Mill itself is better explained in the [Mill documentation](https://mill-build.com/mill/Installation_IDE_Support.html#_bootstrap_scripts).

Mill requires a `build.sc` file as the definintion for the build system. In the context of
this integration, a Lua project should look like this:

```scala

import $ivy.`com.ivmoreau::mill-lua::0.0.2`

import mill._
import mill.scalalib._
import com.ivmoreau.lua._

object example extends LuaModule {
  def luaVersion = LuaVersion.Love2D("11.5")
  override def luarocks = T{ Agg(Rock("lua-cjson", "2.1.0.10-1")) }
}
```

that corresponds to the following file structure:

```
.
├── build.sc
└── example
    └── src
        └── main.lua
```

where your `main.lua` is the entry point to your program.

All of the dependencies specified in `override def luarocks` should be already avaliable
when you run your program using mill, which is as simple as just:

```bash
mill -i example.run
```

That script will download Lua, all of the specified rocks and run your project with
your main file.

If you need LSP support, is as easy as executing the following:

```bash
mill -i example.writeLSP
```

and that's the basics!

## Support

As of rigth now, we have support for the following platforms:

- Standard Lua ~ POSIX
- Luajit ~ POSIX
- Love2D ~ macOS / Linux

## Documentation

For a more in depth explanation, check the full documentation for this module.

## License

mill-lua is licensed under MPL2. See the LICENSE file for more details.

## Contributing

Follow the [Scala Code of Conduct](https://www.scala-lang.org/conduct/), and license your work
under the [MPL 2.0](LICENSE). Contributions are always welcome, from documentation to testing to
code changes. You don't need to open an issue first, but it might be a good idea to discuss your
plans in case others (or I) are already working on it.