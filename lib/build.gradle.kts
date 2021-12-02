import com.qmuiteam.plugin.Dep

plugins {
    `java-library`
}

java {
    sourceCompatibility = Dep.javaVersion
    targetCompatibility = Dep.javaVersion
}
