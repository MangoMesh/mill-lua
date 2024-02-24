package com.ivmoreau.lua

import mill._

private[lua]
trait LSP extends mill.define.Module { self: LuaModule =>

  def generateLSP: T[String] = T {
    val runtimeVersion: String = s"Lua ${luaVersion.version}"
    val json = ujson.Obj(
      "$schema" -> "https://raw.githubusercontent.com/LuaLS/vscode-lua/master/setting/schema.json",
      "runtime.version" -> runtimeVersion,
      "runtime.path" -> allSources().map { pathRef =>
        pathRef.path.toString
      },
    )
    ujson.write(json, indent = 2)
  }

  def writeLSP: T[PathRef] = T {
    val dest = os.pwd / ".luarc.json"
    os.write(dest, generateLSP())
    PathRef(dest)
  }
}