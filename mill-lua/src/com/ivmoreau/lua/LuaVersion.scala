package com.ivmoreau.lua

import mill._

private[lua]
trait LuaVersion extends mill.define.Module { self: LuaModule =>
  def luaVersion: self.LuaVersion

  private def systemOS: T[SystemOS] = T.persistent {
    System.getProperty("os.name").toUpperCase match {
      // I found these three macOS values after a quick search on GitHub.
      case x if x.contains("MAC") || x.contains("DARWIN") || x.contains("OS X") => SystemOS.MacOS()
      case x if x.contains("LINUX") => SystemOS.Linux()
      case _ => SystemOS.Unknown()
    }
  }

  /** The path were the Lua binaries are downloaded, built or installed. */
  def luaDest: T[os.Path] = T { T.dest / "lua" }
  /** The path were the Lua source code is downloaded. */
  def luaSrcDest: T[os.Path] = T { luaDest() / "src" }
  /** The path were the Lua binaries are installed. */
  def luaBinDest: T[os.Path] = T { luaDest() / "bin" }
  /** Enables or disables the use of a system provided Lua (like when using Nix). */
  def fromSourceOnly: T[Boolean] = T { false }

  /** The subroutine that does one of these things:
    * 1. downloads, builds or installs the Lua binaries. 
    * 2. uses the system provided Lua
    * and returns the path to the Lua binary.
    */
  def luaBinary: T[os.Path] = T.persistent {
    val hasLua: String = os.proc("lua", "-v").call(check = false).out.text()
    luaVersion match {
      // If the user has LuaJIT installed, we use it.
      case LuaVersion.LuaJit if hasLua.contains("LuaJIT") && !fromSourceOnly() =>
        T.log.info("Using system LuaJIT")
        os.Path(os.proc("which", "luajit").call().out.lines().head)
      // Or download and build LuaJIT (any OS).
      case LuaVersion.LuaJit =>
        T.log.info("Building LuaJIT")
        os.makeDir.all(luaDest())
        os.proc("git", "clone", "https://github.com/LuaJIT/LuaJIT", "src").call(cwd = luaDest())
        os.proc("make", "PREFIX=" + luaBinDest()).call(
          cwd = luaSrcDest(),
          // This is required to build LuaJIT on macOS. I don't think it will hurt on Linux to have it set.
          env = Map("MACOSX_DEPLOYMENT_TARGET" -> System.getProperty("os.version"))
        )
        os.proc("make", "install", "PREFIX=" + luaBinDest()).call(cwd = luaSrcDest())
        luaBinDest() / "bin" / "luajit"
      // Or download Love2D (macOS only).
      case LuaVersion.Love2D(version) if systemOS() == SystemOS.MacOS() =>
        T.log.info("Downloading Love2D")
        os.makeDir.all(luaDest())
        os.proc("wget", "https://github.com/love2d/love/releases/download/11.5/love-11.5-macos.zip").call(cwd = luaDest())
        os.proc("unzip", "love-11.5-macos.zip").call(cwd = luaDest())
        luaDest() / "love.app" / "Contents" / "MacOS" / "love"
      // Or download Love2D (Linux only).
      case LuaVersion.Love2D(version) if systemOS() == SystemOS.Linux() =>
        T.log.info("Downloading Love2D")
        os.makeDir.all(luaDest())
        os.proc("wget", "https://github.com/love2d/love/releases/download/11.5/love-11.5-x86_64.AppImage").call(cwd = luaDest())
        os.proc("chmod", "+x", "love-11.5-x86_64.AppImage").call(cwd = luaDest())
        luaDest() / "love-11.5-x86_64.AppImage"
      // Or download and build Lua (any OS).
      case LuaVersion.Lua(major, minor) =>
        T.log.info(s"Building Lua 5.$major.$minor")
        os.makeDir.all(luaDest())
        os.proc("wget", s"https://www.lua.org/ftp/lua-5.$major.$minor.tar.gz").call(cwd = luaDest())
        os.proc("tar", "xf", s"lua-5.$major.$minor.tar.gz").call(cwd = luaDest())
        os.proc("make").call(cwd = luaSrcDest() / s"lua-5.$major.$minor")
        os.proc("make", "install", "INSTALL_TOP=" + luaBinDest()).call(cwd = luaSrcDest() / s"lua-5.$major.$minor")
        luaBinDest() / "bin" / "lua"
    }
  }

  /** LuaVersion is a specific distribution of Lua with a specific version. */
  trait LuaVersion {
    /** The version of Lua that this distribution is based on. */
    def version: String
  }

  object LuaVersion {
    case object LuaJit extends LuaVersion {
      // LuaJIT is a distribution of Lua 5.1.
      def version = "5.1"
    }
    case class Love2D(love2DVersion: String) extends LuaVersion {
      // Love2D is based on LuaJIT.
      def version = "5.1"
    }
    
    /** For any Lua 5.x.y, we set major to x and minor to y. */
    case class Lua(major: Int, minor: Int) extends LuaVersion {
      def version = s"5.$major"
    }
  }

  sealed trait SystemOS
  object SystemOS {
    case class MacOS() extends SystemOS
    case class Linux() extends SystemOS
    case class Unknown() extends SystemOS // So we can have a safe pattern match.
    // TODO: The Windows case is left as an exercise for the reader.

    // upicklers for mill
    import upickle.default.{macroRW, ReadWriter => RW}
    def rwMacoOS: RW[MacOS] = macroRW
    def rwLinux: RW[Linux] = macroRW
    def rwUnknown: RW[Unknown] = macroRW
    implicit def rwSystemOS: RW[SystemOS] = RW.merge(rwMacoOS, rwLinux, rwUnknown)
  }
}