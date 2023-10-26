package io.shiftleft.semanticcpg.language.types.structure

import io.shiftleft.codepropertygraph.generated.v2.*
import io.shiftleft.codepropertygraph.generated.v2.nodes.*
import io.shiftleft.semanticcpg.language.*
import overflowdb.*
// TODO bring back: import overflowdb.traversal.help
// TODO bring back: import overflowdb.traversal.help.Doc

/** A method, function, or procedure
  */
// TODO bring back: @help.Traversal(elementType = classOf[Method])
class MethodTraversal(val traversal: Iterator[Method]) extends AnyVal {

  /** Traverse to annotations of method
    */
  def annotation: Iterator[nodes.Annotation] =
    traversal.flatMap(_._annotationViaAstOut)

  /** All control structures of this method
    */
  // TODO bring back: @Doc(info = "Control structures (source frontends only)")
  def controlStructure: Iterator[ControlStructure] =
    traversal.ast.isControlStructure

  /** Shorthand to traverse to control structures where condition matches `regex`
    */
  def controlStructure(regex: String): Iterator[ControlStructure] =
    traversal.ast.isControlStructure.code(regex)

  // TODO bring back: @Doc(info = "All try blocks (`ControlStructure` nodes)")
  def tryBlock: Iterator[ControlStructure] =
    controlStructure.isTry

  // TODO bring back: @Doc(info = "All if blocks (`ControlStructure` nodes)")
  def ifBlock: Iterator[ControlStructure] =
    controlStructure.isIf

  // TODO bring back: @Doc(info = "All else blocks (`ControlStructure` nodes)")
  def elseBlock: Iterator[ControlStructure] =
    controlStructure.isElse

  // TODO bring back: @Doc(info = "All switch blocks (`ControlStructure` nodes)")
  def switchBlock: Iterator[ControlStructure] =
    controlStructure.isSwitch

  // TODO bring back: @Doc(info = "All do blocks (`ControlStructure` nodes)")
  def doBlock: Iterator[ControlStructure] =
    controlStructure.isDo

  // TODO bring back: @Doc(info = "All for blocks (`ControlStructure` nodes)")
  def forBlock: Iterator[ControlStructure] =
    controlStructure.isFor

  // TODO bring back: @Doc(info = "All while blocks (`ControlStructure` nodes)")
  def whileBlock: Iterator[ControlStructure] =
    controlStructure.isWhile

  // TODO bring back: @Doc(info = "All gotos (`ControlStructure` nodes)")
  def goto: Iterator[ControlStructure] =
    controlStructure.isGoto

  // TODO bring back: @Doc(info = "All breaks (`ControlStructure` nodes)")
  def break: Iterator[ControlStructure] =
    controlStructure.isBreak

  // TODO bring back: @Doc(info = "All continues (`ControlStructure` nodes)")
  def continue: Iterator[ControlStructure] =
    controlStructure.isContinue

  // TODO bring back: @Doc(info = "All throws (`ControlStructure` nodes)")
  def throws: Iterator[ControlStructure] =
    controlStructure.isThrow

  /** The type declaration associated with this method, e.g., the class it is defined in.
    */
  // TODO bring back: @Doc(info = "Type this method is defined in")
  def definingTypeDecl: Iterator[TypeDecl] =
    traversal
      .repeat(_._astIn)(_.until(_.collectAll[TypeDecl]))
      .cast[TypeDecl]

  /** The type declaration associated with this method, e.g., the class it is defined in. Alias for 'definingTypeDecl'
    */
  // TODO bring back: @Doc(info = "Type this method is defined in - alias for 'definingTypeDecl'")
  def typeDecl: Iterator[TypeDecl] = definingTypeDecl

  /** The method in which this method is defined
    */
  // TODO bring back: @Doc(info = "Method this method is defined in")
  def definingMethod: Iterator[Method] =
    traversal
      .repeat(_._astIn)(_.until(_.collectAll[Method]))
      .cast[Method]

  /** Traverse only to methods that are stubs, e.g., their code is not available or the method body is empty.
    */
  def isStub: Iterator[Method] =
    traversal.where(_.not(_._cfgOut.not(_.collectAll[MethodReturn])))

  /** Traverse only to methods that are not stubs.
    */
  def isNotStub: Iterator[Method] =
    traversal.where(_._cfgOut.not(_.collectAll[MethodReturn]))

  /** Traverse only to methods that accept variadic arguments.
    */
  def isVariadic: Iterator[Method] = {
    traversal.filter(_.isVariadic)
  }

  /** Traverse to external methods, that is, methods not present but only referenced in the CPG.
    */
  // TODO bring back: @Doc(info = "External methods (called, but no body available)")
  def external: Iterator[Method] =
    traversal.isExternal(true)

  /** Traverse to internal methods, that is, methods for which code is included in this CPG.
    */
  // TODO bring back: @Doc(info = "Internal methods, i.e., a body is available")
  def internal: Iterator[Method] =
    traversal.isExternal(false)

  /** Traverse to the methods local variables
    */
  // TODO bring back: @Doc(info = "Local variables declared in the method")
  def local: Iterator[Local] =
    traversal.block.ast.isLocal

  // TODO bring back: @Doc(info = "Top level expressions (\"Statements\")")
  def topLevelExpressions: Iterator[Expression] =
    traversal._astOut
      .collectAll[Block]
      ._astOut
      .not(_.collectAll[Local])
      .cast[Expression]

  // TODO bring back: @Doc(info = "Control flow graph nodes")
  def cfgNode: Iterator[CfgNode] =
    traversal.flatMap(_.cfgNode)

  /** Traverse to last expression in CFG.
    */
  // TODO bring back: @Doc(info = "Last control flow graph node")
  def cfgLast: Iterator[CfgNode] =
    traversal.methodReturn.cfgLast

  /** Traverse to method body (alias for `block`) */
  // TODO bring back: @Doc(info = "Alias for `block`")
  def body: Iterator[Block] =
    traversal.block

  /** Traverse to namespace */
  // TODO bring back: @Doc(info = "Namespace this method is declared in")
  def namespace: Iterator[Namespace] = {
    traversal.namespaceBlock.namespace
  }

  /** Traverse to namespace block */
  // TODO bring back: @Doc(info = "Namespace block this method is declared in")
  def namespaceBlock: Iterator[NamespaceBlock] = {
    traversal.flatMap { m =>
      m.astIn.headOption match {
        // some language frontends don't have a TYPE_DECL for a METHOD
        case Some(namespaceBlock: NamespaceBlock) => namespaceBlock.start
        // other language frontends always embed their method in a TYPE_DECL
        case _ => m.definingTypeDecl.namespaceBlock
      }
    }
  }

  def numberOfLines: Iterator[Int] = traversal.map(_.numberOfLines)

}
