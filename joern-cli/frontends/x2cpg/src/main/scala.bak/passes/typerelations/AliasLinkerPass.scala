package io.joern.x2cpg.passes.typerelations

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.v2.nodes.TypeDecl
import io.shiftleft.codepropertygraph.generated.v2.{EdgeKinds, NodeKinds, NodeTypes, PropertyNames}
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language.*
import io.joern.x2cpg.utils.LinkingUtil

class AliasLinkerPass(cpg: Cpg) extends CpgPass(cpg) with LinkingUtil {

  override def run(dstGraph: DiffGraphBuilder): Unit = {
    // Create ALIAS_OF edges from TYPE_DECL nodes to TYPE
    linkToMultiple(
      cpg,
      srcKind = NodeKinds.TYPE_DECL,
      dstNodeLabel = NodeTypes.TYPE,
      edgeType = EdgeKinds.ALIAS_OF,
      dstNodeMap = typeFullNameToNode(cpg, _),
      getDstFullNames = (srcNode: TypeDecl) => {
        srcNode.aliasTypeFullName
      },
      dstFullNameKey = PropertyNames.ALIAS_TYPE_FULL_NAME,
      dstGraph
    )
  }

}