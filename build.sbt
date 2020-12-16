name := "gitbucket-mirror-plugin"
organization := "io.github.gitbucket"
version := "1.3.0"
scalaVersion := "2.13.1"
gitbucketVersion := "4.35.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
libraryDependencies ++= Seq(
  "com.jcraft"                      % "jsch"                          % "0.1.55",
  "org.eclipse.jgit"                % "org.eclipse.jgit.ssh.jsch"     % "5.8.0.202006091008-r",
)

assemblyMergeStrategy in assembly := {
  case "plugin.properties"                            => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}