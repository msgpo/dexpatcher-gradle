/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.gradle.extensions

import groovy.transform.CompileStatic

import lanchon.dexpatcher.gradle.FileHelper
import lanchon.dexpatcher.gradle.NewProperty
import lanchon.dexpatcher.gradle.tasks.DexpatcherTask.Verbosity

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternFilterable

import static lanchon.dexpatcher.gradle.Constants.*

@CompileStatic
class DexpatcherExtension extends AbstractToolExtension {

    static final def QUIET = Verbosity.QUIET
    static final def NORMAL = Verbosity.NORMAL
    static final def VERBOSE = Verbosity.VERBOSE
    static final def DEBUG = Verbosity.DEBUG

    final Property<Integer> apiLevel = NewProperty.from(project, 0)
    final Property<Boolean> multiDex = NewProperty.from(project, false)
    final Property<Boolean> multiDexThreaded = NewProperty.from(project, false)
    final Property<Integer> multiDexJobs = NewProperty.from(project, 0)
    final Property<Integer> maxDexPoolSize = NewProperty.from(project, 0)
    final Property<String> annotationPackage = project.objects.property(String)
    final Property<Boolean> constructorAutoIgnore = NewProperty.from(project, true)
    final Property<Verbosity> verbosity = project.objects.property(Verbosity)
    final Property<Boolean> logSourcePath = NewProperty.from(project, false)
    final Property<String> logSourcePathRoot = project.objects.property(String)
    final Property<Boolean> logStats = NewProperty.from(project, false)

    final Provider<RegularFile> bundledAnnotationFile
    final Provider<RegularFile> configuredAnnotationFile
    final Provider<RegularFile> resolvedAnnotationFile

    DexpatcherExtension(Project project, DexpatcherConfigExtension dexpatcherConfig, Configuration dexpatcherCfg,
            Configuration dexpatcherAnnotationCfg) {
        super(project, dexpatcherConfig)
        classpath.from { dexpatcherCfg.singleFile }
        bundledAnnotationFile = project.<RegularFile>provider {
            def file = dexpatcherCfg.singleFile
            def files = file.isDirectory() ? project.fileTree(file) : project.zipTree(file)
            def filteredFiles = files.matching { PatternFilterable filter ->
                filter.include FileNames.DEXPATCHER_ANNOTATION
            }
            if (filteredFiles.empty) throw new RuntimeException("Bundled DexPatcher annotations not found")
            return FileHelper.getRegularFile(project, filteredFiles.singleFile)
        }
        configuredAnnotationFile = project.<RegularFile>provider {
            dexpatcherAnnotationCfg.empty ? null :
                    FileHelper.getRegularFile(project, dexpatcherAnnotationCfg.singleFile)
        }
        resolvedAnnotationFile = project.<RegularFile>provider {
            configuredAnnotationFile.orNull ?: bundledAnnotationFile.get()
        }
    }

}
