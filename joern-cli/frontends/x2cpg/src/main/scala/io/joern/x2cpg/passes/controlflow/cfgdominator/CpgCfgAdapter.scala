package io.joern.x2cpg.passes.controlflow.cfgdominator

import io.shiftleft.codepropertygraph.generated.v2.nodes.StoredNode

class CpgCfgAdapter extends CfgAdapter[StoredNode] {

  override def successors(node: StoredNode): IterableOnce[StoredNode] =
    node._cfgOut

  override def predecessors(node: StoredNode): IterableOnce[StoredNode] =
    node._cfgIn

}