package io.shiftleft.semanticcpg.language.operatorextension.nodemethods

import io.shiftleft.codepropertygraph.generated.v2.Operators
import io.shiftleft.codepropertygraph.generated.v2.nodes.{Call, Expression}
import io.shiftleft.codepropertygraph.generated.v2.Language.*
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.language.operatorextension.{OpNodes, allArrayAccessTypes}

class TargetMethods(val expr: Expression) extends AnyVal {

  def arrayAccess: Option[OpNodes.ArrayAccess] =
    expr.ast.isCall
      .collectFirst { case x if allArrayAccessTypes.contains(x.name) => x }
      .map(new OpNodes.ArrayAccess(_))

  def pointer: Option[Expression] =
    Option(expr).collect {
      case call: Call if call.name == Operators.indirection => call.argument(1)
    }

}