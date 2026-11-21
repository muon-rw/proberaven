import dev.kikugie.stonecutter.controller.task.StonecutterTaskMutexService

plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    id("dev.mixinmcp.decompile")
}

version = "${sc.properties["mod.version"]}+${sc.current.version}"
base.archivesName = "${sc.properties["mod.id"]}-forge"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

legacyForge {
    version = sc.properties["deps.forge_loader"]

    mods {
        register("proberaven") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }

        register("server") {
            gameDirectory = file("../../run/")
            server()
        }
    }
}

mixin {
    add(sourceSets.main.get(), "proberaven.refmap.json")
    config("proberaven.mixins.json")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
            filter { line ->
                if (line.contains("\"compatibilityLevel\"")) {
                    "$line\n  \"refmap\": \"proberaven.refmap.json\","
                } else {
                    line
                }
            }
        }

        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "*.ct", "*.classtweaker")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
        usesService(StonecutterTaskMutexService.create(gradle, name))
    }

    withType<Jar> {
        val name = sc.properties["mod.id"]
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "$it-$name" } }
    }

    jar {
        manifest {
            attributes("MixinConfigs" to "proberaven.mixins.json")
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", sc.properties["mod.version"])
        from(
            named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("reobfJar").flatMap { it.archiveFile },
            named<Jar>("sourcesJar").flatMap { it.archiveFile }
        )
        into(rootProject.layout.buildDirectory.file("libs/${sc.properties["mod.version"]}"))
    }
}
