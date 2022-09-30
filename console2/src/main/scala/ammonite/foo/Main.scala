package ammonite.foo

import ammonite.foo.Util.{newLine, CodeSource}
import dotty.tools.MainGenericCompiler.classpathSeparator
import dotty.tools.dotc.Run
import dotty.tools.dotc.core.Comments.{ContextDoc, ContextDocstrings}
import dotty.tools.dotc.core.Contexts.{Context, ContextBase, ContextState, FreshContext, NoContext, ctx}
import dotty.tools.dotc.ast.{Positioned, tpd, untpd}
import dotty.tools.dotc.classpath.{AggregateClassPath, ClassPathFactory}
import dotty.tools.dotc.config.{Feature, JavaPlatform, Platform}
import dotty.tools.dotc.core.{Contexts, MacroClassLoader, Mode, TyperState}
import dotty.tools.io.{AbstractFile, ClassPath, ClassRepresentation}
import dotty.tools.repl.{AbstractFileClassLoader, CollectTopLevelImports, Newline, ParseResult, Parsed, Quit, State}

import java.io.PrintStream
import org.jline.reader.*

import java.net.URL
import java.nio.charset.StandardCharsets
import javax.naming.InitialContext
import scala.annotation.tailrec
import scala.collection.mutable
import scala.jdk.CollectionConverters.*

object Main {
  def main(args: Array[String]): Unit = {
    val printer = Printer(
      outStream = System.out,
      errStream = System.err,
      resultStream = System.out,
      warning = msg => println(s"Xwarn: $msg"),
      error = msg => println(s"Xerror: $msg"),
      info = msg => println(s"Xinfo: $msg"))
    val versionSortJar = "/home/mp/.cache/coursier/v1/https/repo1.maven.org/maven2/com/michaelpollmeier/versionsort/1.0.7/versionsort-1.0.7.jar"
    val versionSortJarUrl = new URL(s"file:$versionSortJar")

    val initialClassLoader = getClass.getClassLoader
    val initialClassPath = Classpath.classpath(initialClassLoader, None)

    var compiler = new Compiler(
      dynamicClassPath = dotty.tools.io.AbstractFile.getDirectory("."),
      initialClassPath = initialClassPath,
      classPath = initialClassPath,
      whiteList = Set.empty
    )
    var cmdIdx = 0
    given Context = compiler.initialCtx // TODO always get latest context for completions...

    val prompt = "joern> "
    val terminal = new JLineTerminal(prompt)

    // TODO implement completer
    val completer: Completer = { (_, line, candidates) =>
      println("XXX1 in completer")
      //      val comps = completions(line.cursor, line.line, state)
      //      candidates.addAll(comps.asJava)
    }

    val imports = new StringBuffer
    val wd: os.Path = os.pwd

    def preprocess(code: String): String = {
      val wrapperName = Name("cmd" + code)

      val codeSource = CodeSource(
        wrapperName,
        Seq(),
        Seq(Name("ammonite"), Name("$sess")),
        Some(wd/"(console)")
      )
      val (hookStmts, importTrees) = parser.parseImportHooks(codeSource, stmts)
      ???
//      val codeWithPrefixedImports =
//        s"""$imports
//           |$code
//           |""".stripMargin
//      codeWithPrefixedImports
    }

    def readLine(): Unit = {
      val code = terminal.readLine(completer)
      if (code.startsWith("//> using")) {
        // TODO allow to add any maven dependency and/or jar
        compiler = new Compiler(
          compiler.dynamicClassPath,
          compiler.initialClassPath,
          compiler.classPath :+ versionSortJarUrl,
          compiler.whiteList)
      } else {
        val compileResult = compiler.compile(
          src = preprocess(code).getBytes(StandardCharsets.UTF_8),
          printer,
          importsLen = 0,
          userCodeNestingLevel = 0,
          fileName = s"cmd$cmdIdx.sc"
        ).get

        if (compileResult.imports.value.nonEmpty) {
          imports.append(compileResult.imports.toString())
          imports.append(newLine)
        }
      }
      cmdIdx += 1
    }

    while (true) readLine()
  }

}