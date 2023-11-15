package io.joern.x2cpg.passes.base

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.v2.{EdgeKinds, nodes}
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language._
import org.slf4j.{Logger, LoggerFactory}

/** Adds a METHOD_PARAMETER_OUT for each METHOD_PARAMETER_IN to the graph and connects those with a PARAMETER_LINK edge.
  * It also creates an AST edge from METHOD to the new METHOD_PARAMETER_OUT nodes.
  *
  * This pass has MethodStubCreator as prerequisite for language frontends which do not provide method stubs.
  */
class MethodDecoratorPass(cpg: Cpg) extends CpgPass(cpg) {
  import MethodDecoratorPass.logger

  private[this] var loggedDeprecatedWarning   = false
  private[this] var loggedMissingTypeFullName = false

  override def run(dstGraph: DiffGraphBuilder): Unit = {
    cpg.parameter.foreach { parameterIn =>
      if (!parameterIn._parameterLinkOut.hasNext) {
        val parameterOut = nodes
          .NewMethodParameterOut()
          .code(parameterIn.code)
          .order(parameterIn.order)
          .index(parameterIn.index)
          .name(parameterIn.name)
          .evaluationStrategy(parameterIn.evaluationStrategy)
          .typeFullName(parameterIn.typeFullName)
          .isVariadic(parameterIn.isVariadic)
          .lineNumber(parameterIn.lineNumber)
          .columnNumber(parameterIn.columnNumber)

        val method = parameterIn._astIn.headOption
        if (method.isEmpty) {
          logger.warn("Parameter without method encountered: " + parameterIn.toString)
        } else {
          if (parameterIn.typeFullName == null) {
            val evalType = parameterIn.typ
            dstGraph.addEdge(parameterOut, evalType, EdgeKinds.EVAL_TYPE)
            if (!loggedMissingTypeFullName) {
              logger.warn("Using deprecated CPG format with missing TYPE_FULL_NAME on METHOD_PARAMETER_IN nodes.")
              loggedMissingTypeFullName = true
            }
          }

          dstGraph.addNode(parameterOut)
          dstGraph.addEdge(method.get, parameterOut, EdgeKinds.AST)
          dstGraph.addEdge(parameterIn, parameterOut, EdgeKinds.PARAMETER_LINK)
        }
      } else if (!loggedDeprecatedWarning) {
        logger.warn("Using deprecated CPG format with PARAMETER_LINK edges")
        loggedDeprecatedWarning = true
      }
    }
  }
}

object MethodDecoratorPass {
  private val logger: Logger = LoggerFactory.getLogger(classOf[MethodDecoratorPass])
}