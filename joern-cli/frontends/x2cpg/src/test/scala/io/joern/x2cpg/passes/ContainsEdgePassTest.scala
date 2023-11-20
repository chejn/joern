package io.joern.x2cpg.passes

import io.joern.x2cpg.passes.base.ContainsEdgePass
import io.shiftleft.codepropertygraph.generated.v2.{Cpg, EdgeTypes, NodeTypes}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.codepropertygraph.generated.v2.nodes.{NewCall, NewFile, NewMethod, NewTypeDecl}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.jdk.CollectionConverters.*

class ContainsEdgePassTest extends AnyWordSpec with Matchers {

  import ContainsEdgePassTest.Fixture

  "Files " can {
    "contain Methods" in Fixture { fixture =>
      fixture.methodVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.fileVertex)
    }
    "contain Classes" in Fixture { fixture =>
      fixture.typeDeclVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.fileVertex)
    }
  }

  "Classes " can {
    "contain Methods" in Fixture { fixture =>
      fixture.typeMethodVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.typeDeclVertex)
    }
  }

  "Methods " can {
    "contain Methods" in Fixture { fixture =>
      fixture.innerMethodVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.methodVertex)
    }
    "contain expressions" in Fixture { fixture =>
      fixture.expressionVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.methodVertex)
      fixture.innerExpressionVertex.in(EdgeTypes.CONTAINS).l shouldBe List(fixture.innerMethodVertex)
    }
  }

}

object ContainsEdgePassTest {
  private class Fixture {
    private val cpg = Cpg.empty
    private val graph = cpg.graph

    val fileVertex            = graph.addNode(NewFile())
    val typeDeclVertex        = graph.addNode(NewTypeDecl())
    val typeMethodVertex      = graph.addNode(NewMethod())
    val methodVertex          = graph.addNode(NewMethod())
    val innerMethodVertex     = graph.addNode(NewMethod())
    val expressionVertex      = graph.addNode(NewCall())
    val innerExpressionVertex = graph.addNode(NewCall())

    // TODO MP get arrow syntax back
//    fileVertex --- EdgeKinds.AST --> typeDeclVertex
//    typeDeclVertex --- EdgeKinds.AST --> typeMethodVertex
//
//    fileVertex --- EdgeKinds.AST --> methodVertex
//    methodVertex --- EdgeKinds.AST --> innerMethodVertex
//    methodVertex --- EdgeKinds.AST --> expressionVertex
//    innerMethodVertex --- EdgeKinds.AST --> innerExpressionVertex
    graph.applyDiff { diffGraphBuilder =>
      diffGraphBuilder.addEdge(fileVertex, typeDeclVertex, EdgeTypes.AST)
      diffGraphBuilder.addEdge(typeDeclVertex, typeMethodVertex, EdgeTypes.AST)

      diffGraphBuilder.addEdge(fileVertex, methodVertex, EdgeTypes.AST)
      diffGraphBuilder.addEdge(methodVertex, innerMethodVertex, EdgeTypes.AST)
      diffGraphBuilder.addEdge(methodVertex, expressionVertex, EdgeTypes.AST)
      diffGraphBuilder.addEdge(innerMethodVertex, innerExpressionVertex, EdgeTypes.AST)
    }

    val containsEdgeCalculator = new ContainsEdgePass(new Cpg(graph))
    containsEdgeCalculator.createAndApply()
  }

  private object Fixture {
    def apply[T](fun: Fixture => T): T = {
      val fixture = new Fixture()
      try fun(fixture)
      finally ??? // TODO MP get close back fixture.graph.close()
    }
  }
}
