include(":qmuidemo")
include(":qmui")
include(":lib")
include(":compiler")
include(":arch")
include(":arch-compiler")
include(":arch-annotation")
include(":type")
include(":compose")
include(":photo")

includeBuild("./plugin")

pluginManagement {
    repositories {
        mavenCentral()
    }
}
