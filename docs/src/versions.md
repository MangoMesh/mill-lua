# Versions

Versions are defined in `LuaVersion.scala`, specifically `luaBinary`, which defines
the current support for the diferent Lua distributios.

The Lua version is set with `def luaVersion: LuaVersion = LuaVersion.<dist>`

In our `build.sc` we can set `def fromSourceOnly = true` to disable the usage of a
user installed Lua version, if any. [1]

## Matrix

|   OS   | macOS | Linux | Windows |
|--------|-------|-------|---------|
| luajit |   x   |   x   |    ?    |
| lua    |   x   |   x   |    ?    |
| love2d |   x   |   x   |         |

## Notes

- [1] This currently only works when using Luajit.
