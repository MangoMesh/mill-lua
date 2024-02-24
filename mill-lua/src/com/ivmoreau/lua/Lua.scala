package com.ivmoreau.lua

import mill._
import mill.api.Result


/** A dependency on a LuaRocks package */
case class Rock(name: String, version: String)
object Rock {
  import upickle.default.{macroRW, ReadWriter => RW}
  implicit def rw: RW[Rock] = macroRW
}

trait LuaModule extends mill.define.Module with LuaVersion with LSP { self =>
  /** 
   * The luarocks dependencies for this module, if any.
   *
   * An example of how to define a luarocks dependency:
   * {{{ def luarocks: T[Agg[Rock]] = T{ Agg(Rock("moonscript", "0.5.0")) } }}}
   * 
   * This will install the moonscript rock version 0.5.0
   */
  def luarocks: T[Agg[Rock]] = T{ Agg.empty[Rock] }

  /** The path were luarocks will be installed. This is analogous to
    * a node_modules folder for npm in the JavaScript world.
    */
  def luarocksDest: T[os.Path] = T{ T.dest / "luarocks" }

  def resolveLuaRocks: T[Unit] = T.persistent {
    LuaModule.downloadRocks(luarocks(), luarocksDest())
  }

  /**
   * The main file to run for this module
   */
  def main: T[String] = luaVersion match {
    case self.LuaVersion.Love2D(_) => "" // Love2D looks for a main.lua file
    case _ => "main.lua" // All other versions should explicitly specify the main file
  }

  /**
   * The folder where the source files for this module live
   */
  def sources: T[PathRef] = T.source { millSourcePath / "src" }

  /**
   * Folders containing source files that are generated rather than
   * hand-written; these files can be generated in this target itself,
   * or can refer to files generated from other targets
   */
  def generatedSources: T[Seq[PathRef]] = T { Seq.empty[PathRef] }

  /**
   * The folders containing all source files fed into the interpreter
   */
  def allSources: T[Seq[PathRef]] = T { generatedSources() :+ sources() }

  /**
   * Runs this module's code in a lua ctx and waits for it to finish
   */
  def run: mill.define.Command[Unit] = T.command {
    try Result.Success {
        resolveLuaRocks()
        os.proc(luaBinary(), sources().path.toString + "/" + main()).call(
            stderr = os.ProcessOutput.Readlines.apply(T.log.error),
            stdout = os.Inherit,
            env = Map(
              "LUA_PATH" -> (sources().path / "?.lua").toString,
              "LUA_CPATH" -> (luarocksDest() / "lib" / "lua" / luaVersion.version / "?.so").toString
            )
          )
        ()
    }
    catch {
      case e: Exception =>
        Result.Failure("subprocess failed")
    }
  }
}

object LuaModule {
  private def downloadRock(rock: Rock, dest: os.Path) = {
    val command = os.proc("luarocks", "install", rock.name, rock.version, "--tree", dest.toString)
    command.call()
  }

  private def downloadRocks(rock: Agg[Rock], dest: os.Path) = {
    rock.foreach(downloadRock(_, dest))
  }
}
