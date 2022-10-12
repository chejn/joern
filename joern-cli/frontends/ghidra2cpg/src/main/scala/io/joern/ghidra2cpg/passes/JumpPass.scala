//package io.joern.ghidra2cpg.passes
//
//import io.shiftleft.codepropertygraph.Cpg
//import io.shiftleft.codepropertygraph.generated.EdgeTypes
//import io.shiftleft.codepropertygraph.generated.nodes.{Call, Method}
//import io.shiftleft.passes.ConcurrentWriterCpgPass
//import io.shiftleft.semanticcpg.language._
//
//import scala.util.Try
//
//class JumpPass(cpg: Cpg) extends ConcurrentWriterCpgPass[Method](cpg) {
//
//  override def generateParts(): Array[Method] =
//    cpg.method.toArray
//  override def runOnPart(diffGraph: DiffGraphBuilder, method: Method): Unit = {
//    method.ast
//      .filter(_.isCall)
//      .map(_.asInstanceOf[Call])
//      .nameExact("<operator>.goto")
//      .where(_.argument.order(1).isLiteral)
//      .foreach { sourceCall =>
//        sourceCall.argument.order(1).code.l.headOption.flatMap(parseAddress) match {
//          case Some(destinationAddress) =>
//            method.ast.filter(_.isInstanceOf[Call]).lineNumber(destinationAddress).foreach { destination =>
//              diffGraph.addEdge(sourceCall, destination, EdgeTypes.CFG)
//            }
//          case _ => // Ignore for now
//          /*
//            TODO:
//              - Ask ghidra to resolve addresses of JMPs
//           */
//        }
//      }
//  }
//
//  private def parseAddress(address: String): Option[Int] = {
//    Try(Integer.parseInt(address, 16)).toOption
//  }
//}

package io.joern.ghidra2cpg.passes

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.shiftleft.codepropertygraph.generated.nodes.{Call, Method}
import io.shiftleft.passes.ConcurrentWriterCpgPass
import io.shiftleft.semanticcpg.language._

import scala.util.Try

class JumpPass(cpg: Cpg) extends ConcurrentWriterCpgPass[Method](cpg) {

  override def generateParts(): Array[Method] =
    cpg.method.toArray
  override def runOnPart(diffGraph: DiffGraphBuilder, method: Method): Unit = {

    def x = method.ast
      .filter(_.isCall)
      .map(_.asInstanceOf[Call])
      .nameExact("<operator>.goto")
      .where(_.argument.order(1).isLiteral)
      .dedupBy(_.code).code.l
    println(x)
    method.ast
      .filter(_.isCall)
      .map(_.asInstanceOf[Call])
      .nameExact("<operator>.goto")
      .where(_.argument.order(1).isLiteral)
      .dedupBy(_.code.mkString)
      .foreach { sourceCall =>
        println("ABC "+sourceCall.argument.order(1).code.l)
        sourceCall.argument.order(1).code.l.distinct.headOption.flatMap(parseAddress) match {
          case Some(destinationAddress) =>
            println(method.ast.filter(_.isInstanceOf[Call]).lineNumber(destinationAddress).code.l)
            method.ast.filter(_.isInstanceOf[Call]).lineNumber(destinationAddress).foreach { destination =>
              if (method.name == "main") {
                println(s"${sourceCall.code} ${destination.code}")
              }
              diffGraph.addEdge(sourceCall, destination, EdgeTypes.CFG)
            }
          case _ => // Ignore for now
          /*
            TODO:
              - Ask ghidra to resolve addresses of JMPs
           */
        }
      }
  }

  private def parseAddress(address: String): Option[Int] = {
    Try(Integer.decode(address).toInt).toOption
  }
}
