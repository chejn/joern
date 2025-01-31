package io.joern.rubysrc2cpg.querying

import io.joern.rubysrc2cpg.testfixtures.RubyCode2CpgFixture
import io.joern.x2cpg.Defines
import io.shiftleft.codepropertygraph.generated.{DispatchTypes, Operators}
import io.shiftleft.codepropertygraph.generated.nodes.{Block, Call, Identifier, Literal}
import io.shiftleft.semanticcpg.language.*

class CallTests extends RubyCode2CpgFixture {

  "`puts 'hello'` is represented by a CALL node" in {
    val cpg = code("""
                     |puts 'hello'
                     |""".stripMargin)

    val List(puts) = cpg.call.name("puts").l
    puts.lineNumber shouldBe Some(2)
    puts.code shouldBe "puts 'hello'"

    val List(hello) = puts.argument.l
    hello.code shouldBe "'hello'"
    hello.lineNumber shouldBe Some(2)
  }

  "`foo(1,2)` is represented by a CALL node" in {
    val cpg = code("""
        |foo(1,2)
        |""".stripMargin)

    val List(foo) = cpg.call.name("foo").l
    foo.code shouldBe "foo(1,2)"
    foo.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    foo.lineNumber shouldBe Some(2)

    val one = foo.argument(1)
    one.code shouldBe "1"

    val two = foo.argument(2)
    two.code shouldBe "2"
  }

  "`x.y(1)` is represented by a `y` CALL with argument `1` and receiver `x.y`" ignore {
    val cpg = code("""
                     |x.y(1)
                     |""".stripMargin)

    val List(fieldAccess) = cpg.fieldAccess.l

    fieldAccess.code shouldBe "x.y"
    fieldAccess.dispatchType shouldBe DispatchTypes.STATIC_DISPATCH
    fieldAccess.lineNumber shouldBe Some(2)
    fieldAccess.fieldIdentifier.code.l shouldBe List("y")

    val List(call: Call) = fieldAccess.astParent.toList: @unchecked

    call.code shouldBe "x.y(1)"
    call.name shouldBe "y"
    call.receiver.l shouldBe List(fieldAccess)
    call.lineNumber shouldBe Some(2)

    val List(one) = call.argument.l
    one.code shouldBe "1"
    one.lineNumber shouldBe Some(2)
  }

  "a method and call referencing to a keyword argument" should {

    val cpg = code("""
        |def foo(a, bar: "default")
        |  puts(bar)
        |end
        |
        |foo("hello", bar: "baz")
        |""".stripMargin)

    "contain they keyword in the argumentName property" in {
      inside(cpg.call.nameExact("foo").argument.l) {
        case (hello: Literal) :: (baz: Literal) :: Nil =>
          hello.code shouldBe "\"hello\""
          hello.argumentIndex shouldBe 1
          hello.argumentName shouldBe None

          baz.code shouldBe "\"baz\""
          baz.argumentIndex shouldBe 2
          baz.argumentName shouldBe Option("bar")
        case xs => fail(s"Invalid call arguments! Got [${xs.code.mkString(", ")}]")
      }
    }
  }

  "a simple object instantiation" should {

    val cpg = code("""class A
        |end
        |
        |a = A.new
        |""".stripMargin)

    "create an assignment from `a` to an <init> invocation block" in {
      inside(cpg.method(":program").assignment.where(_.target.isIdentifier.name("a")).l) {
        case assignment :: Nil =>
          assignment.code shouldBe "a = A.new"
          inside(assignment.argument.l) {
            case (a: Identifier) :: (_: Block) :: Nil =>
              a.name shouldBe "a"
            case xs => fail(s"Expected one identifier and one call argument, got [${xs.code.mkString(",")}]")
          }
        case xs => fail(s"Expected a single assignment, got [${xs.code.mkString(",")}]")
      }
    }

    "create an assignment from a temp variable to the <init> call" in {
      inside(cpg.method(":program").assignment.where(_.target.isIdentifier.name("<tmp-0>")).l) {
        case assignment :: Nil =>
          inside(assignment.argument.l) {
            case (a: Identifier) :: (alloc: Call) :: Nil =>
              a.name shouldBe "<tmp-0>"

              alloc.name shouldBe Operators.alloc
              alloc.methodFullName shouldBe Operators.alloc
              alloc.code shouldBe "A.new"
            case xs => fail(s"Expected one identifier and one call argument, got [${xs.code.mkString(",")}]")
          }
        case xs => fail(s"Expected a single assignment, got [${xs.code.mkString(",")}]")
      }
    }

    "create a call to the object's constructor, with the temp variable receiver" in {
      inside(cpg.call.nameExact(Defines.ConstructorMethodName).l) {
        case constructor :: Nil =>
          inside(constructor.argument.l) {
            case (a: Identifier) :: Nil =>
              a.name shouldBe "<tmp-0>"
              a.typeFullName shouldBe "Test0.rb:<global>::program.A"
              a.argumentIndex shouldBe 0
            case xs => fail(s"Expected one identifier and one call argument, got [${xs.code.mkString(",")}]")
          }
        case xs => fail(s"Expected a single alloc, got [${xs.code.mkString(",")}]")
      }
    }
  }

}
