package com.siprell.plugin.gradle.bootstrap

import org.gradle.api.file.FileTree
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync

class BootstrapGradlePlugin implements Plugin<Project> {
	final BOOTSTRAP_DEFAULT_VERSION = "3.3.5"
	final FA_DEFAULT_VERSION = "4.3.0"

	void apply(Project project) {
	    
	    /*
	    TODO
	    invalidVersionFails property
	    Add to README
        Throw an exception
        Test in Spec
        */

		// Shared properties
		def downloadZipFile = new DownloadZipFile()
		def properties = project.hasProperty("bootstrapFramework") ? project.bootstrapFramework : [:]
		String templatesDir = "${project.projectDir}/src/templates"
		String tmpDir = "${project.buildDir}/tmp"

		// Bootstrap Framework properties
		String bootstrapVersion = properties.version ?: BOOTSTRAP_DEFAULT_VERSION
		boolean useIndividualJs = properties.useIndividualJs ?: false
		boolean useLess = properties.useLess ?: false
		String jsPath = properties.jsPath ? properties.jsPath : "grails-app/assets/javascripts"
		String cssPath = properties.cssPath ? properties.cssPath : "grails-app/assets/stylesheets"
		boolean useAssetPipeline = jsPath.contains("assets")
		FileTree bootstrapZipTree

		// Font Awesome properties
		def fontAwesome = properties.fontAwesome
		boolean fontAwesomeInstall = fontAwesome?.install ?: false
		String fontAwesomeVersion = fontAwesome?.version ?: FA_DEFAULT_VERSION
		boolean fontAwesomeUseLess = fontAwesome?.useLess ?: false
		FileTree fontAwesomeZipTree

		project.afterEvaluate {
			project.tasks.processResources.dependsOn("createFontAwesomeLess")
		}

		project.task("bootstrapFrameworkVersions") << {
			println "$BOOTSTRAP_DEFAULT_VERSION is the default Bootstrap Framework version."
			println "$FA_DEFAULT_VERSION is the default Font Awesome version."
		}

		project.task("downloadBootstrapZip") {
			String description = "Bootstrap Framework"
			String filePrefix = "bootstrap-v"
			String url = "https://github.com/twbs/bootstrap/archive/v${bootstrapVersion}.zip"
			String zipFilename = "${filePrefix}${bootstrapVersion}.zip"
			def zipFile = downloadZipFile.download(tmpDir, description, filePrefix, url, bootstrapVersion, zipFilename)
			bootstrapZipTree = (zipFile instanceof File) ? project.zipTree(zipFile) : null
		}

		project.task("downloadFontAwesomeZip", dependsOn: project.tasks.downloadBootstrapZip) {
			if (fontAwesomeInstall) {
				String description = "Font Awesome"
				String filePrefix = "fontAwesome-v"
				String url = "http://fontawesome.io/assets/font-awesome-${fontAwesomeVersion}.zip"
				String zipFilename = "${filePrefix}${fontAwesomeVersion}.zip"
				def zipFile = downloadZipFile.download(tmpDir, description, filePrefix, url, fontAwesomeVersion, zipFilename)
				fontAwesomeZipTree = (zipFile instanceof File) ? project.zipTree(zipFile) : null
			}
		}

		project.task("createBootstrapJsAll", type: Copy, dependsOn: project.tasks.downloadFontAwesomeZip) {
			if (useAssetPipeline) {
			    from "$templatesDir/bootstrap-all.js"
			    into "${project.projectDir}/$jsPath"
			}
		}

		project.task("createBootstrapJs", type: Sync, dependsOn: project.tasks.createBootstrapJsAll) {
			def path = "${project.projectDir}/$jsPath/bootstrap"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/dist/js/bootstrap.js"
				if (useIndividualJs) {
					include "*/js/*.js"
				}
			}.collect()
			from files
			into path
		}

		project.task("createBootstrapCssAll", type: Copy, dependsOn: project.tasks.createBootstrapJs) {
			if (useAssetPipeline) {
			    from "$templatesDir/bootstrap-all.css"
			    into "${project.projectDir}/$cssPath"
			}
		}

		project.task("createBootstrapFonts", type: Copy, dependsOn: project.tasks.createBootstrapCssAll) {
			def path = "${project.projectDir}/$cssPath/bootstrap/fonts"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/fonts/*"
			}.collect()
			from files
			into path
		}

		project.task("createBootstrapCssIndividual", type: Copy, dependsOn: project.tasks.createBootstrapFonts) {
			def path = "${project.projectDir}/$cssPath/bootstrap/css"
			if (!project.file(path).exists()) {
				project.mkdir(path)
			}
			def files = bootstrapZipTree.matching {
				include "*/dist/css/*.css"
				exclude "*/dist/css/*.min.css"
			}.collect()
			from files
			into path
		}

		project.task("createBootstrapLessAll", type: Copy, dependsOn: project.tasks.createBootstrapCssIndividual) {
			if (useLess && useAssetPipeline) {
			    from "$templatesDir/bootstrap-less.less"
			    into "${project.projectDir}/$cssPath"
			}
		}

		project.task("createBootstrapLess", type: Sync, dependsOn: project.tasks.createBootstrapLessAll) {
			def path = "${project.projectDir}/$cssPath/bootstrap/less"
			def files = []
			if (useLess) {
			    files = bootstrapZipTree.matching {
				    include "*/less/*.less"
			    }.collect()
			}
			from files
			into path
		}

		project.task("createBootstrapMixins", type: Sync, dependsOn: project.tasks.createBootstrapLess) {
			def lessPath = "${project.projectDir}/$cssPath/bootstrap/less"
			def path = "$lessPath/mixins"
			if (useLess && !project.file(path).exists()) {
				project.mkdir(path)
			}
			if (!useLess && project.file(lessPath).exists()) {
			    project.file(lessPath).delete()
			}
			def files = []
			if (useLess) {
			    files = bootstrapZipTree.matching {
				    include "*/less/mixins/*.less"
		    	}.collect()
			}
			from files
			into path
		}

		project.task("createFontAwesomeCssAll", type: Copy, dependsOn: project.tasks.createBootstrapMixins) {
			if (fontAwesomeInstall && useAssetPipeline) {
			    from "$templatesDir/font-awesome-all.css"
			    into "${project.projectDir}/$cssPath"
			}
		}

		project.task("createFontAwesomeCssIndividual", type: Copy, dependsOn: project.tasks.createFontAwesomeCssAll) {
			def path = "${project.projectDir}/$cssPath/font-awesome/css"
			def files = []
			if (fontAwesomeInstall) {
				if (!project.file(path).exists()) {
					project.mkdir(path)
				}
				files = fontAwesomeZipTree.matching {
					include "*/css/font-awesome.css"
				}.collect()
			}
			from files
			into path
		}

		project.task("createFontAwesomeFonts", type: Sync, dependsOn: project.tasks.createFontAwesomeCssIndividual) {
			def path = "${project.projectDir}/$cssPath/font-awesome/fonts"
			def files = []
			if (fontAwesomeInstall) {
				if (!project.file(path).exists()) {
					project.mkdir(path)
				}
				files = fontAwesomeZipTree.matching {
					include "*/fonts/*"
				}.collect()
			}
			from files
			into path
		}

		project.task("createFontAwesomeLessAll", type: Copy, dependsOn: project.tasks.createFontAwesomeFonts) {
			if (fontAwesomeInstall && fontAwesomeUseLess) {
			    def target = "font-awesome-less.less"
			    if (cssPath.contains("assets")) {
			        def source = "font-awesome-less.less.assets"
			        from "$templatesDir/$source"
		            rename { String fileName ->
                        fileName.replace(source, target)
                    }
			    } else {
			        from "$templatesDir/$target"
			    }
			    into "${project.projectDir}/$cssPath"
			}
		}

		project.task("createFontAwesomeLess", type: Sync, dependsOn: project.tasks.createFontAwesomeLessAll) {
			def path = "${project.projectDir}/$cssPath/font-awesome/less"
			def files = []
			if (fontAwesomeInstall && fontAwesomeUseLess) {
				files = fontAwesomeZipTree.matching {
					include "*/less/*.less"
				}.collect()
			}
			from files
			into path
		}
	}
}

class DownloadZipFile {
	String fileSuffix = ".zip"

	def download(String tmp, String description, String filePrefix, String url, String version, String zipFilename) {
		def tmpDir = new File("$tmp")
		if (!tmpDir.exists()) {
			tmpDir.mkdir()
		}
		def zipFile = new File("$tmp/$zipFilename")
		if (zipFile.exists()) {
			return zipFile
		}
		try {
			def file = zipFile.newOutputStream()
			file << new URL(url).openStream()
			file.close()
			return zipFile
		} catch (e) {
			zipFile.delete()
			println "Error: Could not download $url.\n$version is an invalid $description version, or you are not connected to the Internet."
			List<File> zipFiles = []
			tmpDir.listFiles().each {
				if (it.name.startsWith(filePrefix)) {
					zipFiles << it
				}
			}
			if (zipFiles.size() > 0) {
				File zipFileOld
				if (zipFiles.size() == 1) {
					zipFileOld = zipFiles[0]
				} else {
					zipFileOld = zipFiles.sort(false) { a, b ->
						def tokens = [a.name.minus(filePrefix).minus(fileSuffix), b.name.minus(filePrefix).minus(fileSuffix)]
						tokens*.tokenize('.')*.collect { it as int }.with { u, v ->
							[u, v].transpose().findResult { x, y -> x <=> y ?: null } ?: u.size() <=> v.size()
						}
					}[-1]
				}
				String newVersion = zipFileOld.name.minus(filePrefix).minus(fileSuffix)
				println "Using $description version $newVersion instead of $version."
				return zipFileOld
			} else {
				// TODO stop tasks execution?
				println "FATAL ERROR: No old $description zip files found in $tmpDir."
			}
		}
	}
}

