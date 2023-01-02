// this does not work as expected as of now because stopping the gradle process does not kill the node process
// activate when the issue is fixed on all major platforms (https://github.com/node-gradle/gradle-node-plugin/issues/65)
/*
import com.github.gradle.node.npm.task.NpmTask


plugins {
    java
    id("com.github.node-gradle.node") version "3.5.1"
}

val devTask = tasks.register<NpmTask>("dev") {
    ignoreExitValue.set(true)
    dependsOn(tasks.npmInstall)
    args.set(listOf("run", "dev"))
}
*/
