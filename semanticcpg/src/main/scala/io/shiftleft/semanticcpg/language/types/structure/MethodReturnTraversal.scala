package io.shiftleft.semanticcpg.language.types.structure

import io.shiftleft.codepropertygraph.generated.v2.nodes.*
import io.shiftleft.codepropertygraph.generated.v2.Language.*
import io.shiftleft.semanticcpg.language.*
// TODO bring back: import overflowdb.traversal.help
// TODO bring back: import overflowdb.traversal.help.Doc

// TODO bring back: @help.Traversal(elementType = classOf[MethodReturn])
class MethodReturnTraversal(val traversal: Iterator[MethodReturn]) extends AnyVal {

  // TODO bring back: @Doc(info = "traverse to parent method")
  def method: Iterator[Method] =
    traversal._methodViaAstIn

  def returnUser(implicit callResolver: ICallResolver): Iterator[Call] =
    traversal.returnUser

  /** Traverse to last expressions in CFG. Can be multiple.
    */
  // TODO bring back: @Doc(info = "traverse to last expressions in CFG (can be multiple)")
  def cfgLast: Iterator[CfgNode] =
    traversal._cfgIn.collectAll[CfgNode]

  /** Traverse to return type
    */
  // TODO bring back: @Doc(info = "traverse to return type")
  def typ: Iterator[Type] =
    traversal._evalTypeOut.collectAll[Type]
}