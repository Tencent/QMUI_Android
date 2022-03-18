pluginManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("./plugin")

include(":qmuidemo")
include(":qmui")
include(":lib")
include(":compiler")
include(":arch")
include(":arch-compiler")
include(":arch-annotation")
include(":type")
include(":compose-core")
include(":compose")
include(":photo")
include(":photo-coil")
include(":photo-glide")
include(":editor")


