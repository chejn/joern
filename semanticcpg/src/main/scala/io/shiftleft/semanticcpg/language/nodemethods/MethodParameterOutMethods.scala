package io.shiftleft.semanticcpg.language.nodemethods

import io.shiftleft.codepropertygraph.generated.v2.nodes.{MethodParameterOut, NewLocation}
import io.shiftleft.codepropertygraph.generated.v2.Language.*
import io.shiftleft.semanticcpg.NodeExtension
import io.shiftleft.semanticcpg.language.{HasLocation, LocationCreator}

class MethodParameterOutMethods(val paramOut: MethodParameterOut) extends AnyVal with NodeExtension with HasLocation {
  override def location: NewLocation = {
    LocationCreator(paramOut, paramOut.name, paramOut.label, paramOut.lineNumber, paramOut.method)
  }
}