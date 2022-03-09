name := "gitbucket-mirror-plugin"
organization := "io.github.gitbucket"
version := "1.4.0"
scalaVersion := "2.13.8"
gitbucketVersion := "4.37.2"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
libraryDependencies ++= Seq(
  "com.jcraft"                      % "jsch"                          % "0.1.55",
  "org.eclipse.jgit"                % "org.eclipse.jgit.ssh.jsch"     % "5.13.0.202109080827-r",
)

assemblyMergeStrategy in assembly := {
  case "plugin.properties"                            => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}