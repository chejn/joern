import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

name := "c2cpg"

dependsOn(
  Projects.semanticcpg,
  Projects.dataflowengineoss % "compile->compile;test->test",
  Projects.x2cpg             % "compile->compile;test->test"
)

lazy val cdtCoreDepVersion        = "8.4.0.202401242025"
lazy val cdtCoreDepNameAndVersion = s"org.eclipse.cdt.core_$cdtCoreDepVersion"
lazy val cdtCodeDepUrl =
  s"https://ci.eclipse.org/cdt/job/cdt/job/main/353/artifact/releng/org.eclipse.cdt.repo/target/repository/plugins/$cdtCoreDepNameAndVersion.jar"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
  "org.eclipse.platform"    % "org.eclipse.core.resources" % "3.20.0",
  "org.eclipse.platform"    % "org.eclipse.text"           % "3.13.100",
  "org.eclipse.platform"    % "org.eclipse.cdt.core"       % cdtCoreDepVersion from cdtCodeDepUrl,
  "org.scalatest"          %% "scalatest"                  % Versions.scalatest % Test
)

dependencyOverrides ++= Seq(
  /* tl;dr; we'll stay on 2.19.0
   * Full story: if we upgrade to 2.20.0 we run into the following osgi error:
   *   Unknown error checking OSGI environment.
   *   java.lang.reflect.InvocationTargetException
   *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
   *     at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
   *     at java.base/java.lang.reflect.Method.invoke(Method.java:568)
   *     at org.apache.logging.log4j.util.OsgiServiceLocator.checkOsgiAvailable(OsgiServiceLocator.java:39)
   *   ...
   *   Caused by: java.lang.NullPointerException: Cannot invoke "org.osgi.framework.BundleContext.getBundles()" because "context" is null
   *     at com.diffplug.spotless.extra.eclipse.base.osgi.SimpleBundle.<init>(SimpleBundle.java:57)
   *     at com.diffplug.spotless.extra.eclipse.base.osgi.SimpleBundle.<init>(SimpleBundle.java:49)
   *     at com.diffplug.spotless.extra.eclipse.base.osgi.FrameworkBundleRegistry.getBundle(FrameworkBundleRegistry.java:47)
   *     at org.osgi.framework.FrameworkUtil.lambda$5(FrameworkUtil.java:234)
   */
  "org.apache.logging.log4j" % "log4j-core"        % "2.19.0" % Optional,
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.19.0" % Optional
)

Compile / doc / scalacOptions ++= Seq("-doc-title", "semanticcpg apidocs", "-doc-version", version.value)

compile / javacOptions ++= Seq("-Xlint:all", "-Xlint:-cast", "-g")
Test / fork := true

enablePlugins(JavaAppPackaging, LauncherJarPlugin)

Universal / packageName       := name.value
Universal / topLevelDirectory := None
