package com.siprell.plugin.gradle.bootstrap

import org.gradle.api.file.FileTree
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Sync

class BootstrapGradlePlugin implements Plugin<Project> {
	final BOOTSTRAP_DEFAULT_VERSION = "3.3.5"
	final FA_DEFAULT_VERSION = "4.3.0"

	void apply(Project project) {

		// Shared properties
		def template = new Template()
		def zipFile = new ZipFile()
		def properties = project.hasProperty("bootstrapFramework") ? project.bootstrapFramework : [:]
		String tmpDir = "${project.buildDir}/tmp"

		// Bootstrap Framework properties
		String bootstrapVersion = properties.version ?: BOOTSTRAP_DEFAULT_VERSION
		boolean useIndividualJs = properties.useIndividualJs ?: false
		boolean useLess = properties.useLess ?: false
		String jsPath = properties.jsPath ? properties.jsPath : "grails-app/assets/javascripts"
		String cssPath = properties.cssPath ? properties.cssPath : "grails-app/assets/stylesheets"
		boolean useAssetPipeline = jsPath.contains("assets")
		boolean bootstrapInvalidVersionFails = properties.invalidVersionFails ?: false
		FileTree bootstrapZipTree

		// Font Awesome properties
		def fontAwesome = properties.fontAwesome
		boolean fontAwesomeInstall = fontAwesome?.install ?: false
		String fontAwesomeVersion = fontAwesome?.version ?: FA_DEFAULT_VERSION
		boolean fontAwesomeUseLess = fontAwesome?.useLess ?: false
		boolean fontAwesomeInvalidVersionFails = fontAwesome?.invalidVersionFails ?: false
		FileTree fontAwesomeZipTree

		project.afterEvaluate {
			project.tasks.processResources.dependsOn("createFontAwesomeLess")
		}

		project.task("bootstrapFrameworkVersions") << {
			println "$BOOTSTRAP_DEFAULT_VERSION is the default Bootstrap Framework version."
			println "$FA_DEFAULT_VERSION is the default Font Awesome version."
		}
		
		project.task("checkDirectories") {
		    if (!project.file(cssPath).exists()) {
		        throw new InvalidUserDataException("bootstrapFramework.cssPath directory ($cssPath) does not exist.")
		    }
		    if (!project.file(jsPath).exists()) {
		        throw new InvalidUserDataException("bootstrapFramework.jsPath directory (jsPath) does not exist.")
		    }
		}

		project.task("downloadBootstrapZip") {
			String description = "Bootstrap Framework"
			String filePrefix = "bootstrap-v"
			String url = "https://github.com/twbs/bootstrap/archive/v${bootstrapVersion}.zip"
			String zipFilename = "${filePrefix}${bootstrapVersion}.zip"
			def file = zipFile.download(tmpDir, description, filePrefix, url, bootstrapVersion, zipFilename, bootstrapInvalidVersionFails)
			bootstrapZipTree = project.zipTree(file)
		}

		project.task("downloadFontAwesomeZip", dependsOn: project.tasks.downloadBootstrapZip) {
			if (fontAwesomeInstall) {
				String description = "Font Awesome"
				String filePrefix = "fontAwesome-v"
				//String url = "http://fontawesome.io/assets/font-awesome-${fontAwesomeVersion}.zip"
				String url = "https://github.com/FortAwesome/Font-Awesome/archive/v${fontAwesomeVersion}.zip"
				String zipFilename = "${filePrefix}${fontAwesomeVersion}.zip"
				def file = zipFile.download(tmpDir, description, filePrefix, url, fontAwesomeVersion, zipFilename, fontAwesomeInvalidVersionFails)
				fontAwesomeZipTree = project.zipTree(file)
			}
		}

		project.task("createBootstrapJsAll", type: Copy, dependsOn: project.tasks.downloadFontAwesomeZip) {
		    def path = "${project.projectDir}/$jsPath"
		    def filename = "bootstrap-all.js"
			if (useAssetPipeline) {
				from template.getFile(project, "createBootstrapJsAll")
				rename ".*", filename
				into path
			} else {
			    project.delete("$path/$filename")
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
		    def path = "${project.projectDir}/$cssPath"
		    def filename = "bootstrap-all.css"
			if (useAssetPipeline) {
				from template.getFile(project, "createBootstrapCssAll")
				rename ".*", filename
				into path
			} else {
			    project.delete("$path/$filename")
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

		project.task("createBootstrapLessLess", type: Copy, dependsOn: project.tasks.createBootstrapCssIndividual) {
		    def path = "${project.projectDir}/$cssPath"
		    def filename = "bootstrap-less.less"
			if (useLess && useAssetPipeline) {
    			from template.getFile(project, "createBootstrapLessLess")
    			rename ".*", filename
    			into path
			}
		}

		project.task("createBootstrapLess", type: Sync, dependsOn: project.tasks.createBootstrapLessLess) {
			def path = "${project.projectDir}/$cssPath/bootstrap/less"
			def files = []
			if (useLess) {
				files = bootstrapZipTree.matching {
					include "*/less/*.less"
				}.collect()
    			from files
    			into path
			} else {
			    project.delete(path)
			}
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
		    def path = "${project.projectDir}/$cssPath"
		    def filename = "font-awesome-all.css"
			if (fontAwesomeInstall && useAssetPipeline) {
				from template.getFile(project, "createFontAwesomeCssAll")
				rename ".*", filename
				into path
			} else {
			    project.delete("$path/$filename")
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
    			from files
    			into path
			} else {
			    project.delete("${project.projectDir}/$cssPath/font-awesome")
			}
		}

		project.task("createFontAwesomeLessLess", type: Copy, dependsOn: project.tasks.createFontAwesomeFonts) {
			if (fontAwesomeInstall && fontAwesomeUseLess) {
				def target = "font-awesome-less.less"
				if (cssPath.contains("assets")) {
					from template.getFile(project, "createFontAwesomeLessLessAssets")
				} else {
					from template.getFile(project, "createFontAwesomeLessLess")
				}
				rename ".*", "font-awesome-less.less"
				into "${project.projectDir}/$cssPath"
			}
		}

		project.task("createFontAwesomeLess", type: Sync, dependsOn: project.tasks.createFontAwesomeLessLess) {
			def path = "${project.projectDir}/$cssPath/font-awesome/less"
			def files = []
			if (fontAwesomeInstall && fontAwesomeUseLess) {
				files = fontAwesomeZipTree.matching {
					include "*/less/*.less"
				}.collect()
    			from files
    			into path
			} else {
			    project.delete(path)
			}
		}
	}
}

class Template {
	static getText(String template) {
		def text = ""
		switch (template) {
			case "createBootstrapJsAll":
				text = """/*
* Do not edit this file. It will be overwritten by the bootstrap-framework plugin.
*
*= require bootstrap/css/bootstrap.css
*= require bootstrap/css/bootstrap-theme.css
*/
"""
				break
			case "createBootstrapCssAll":
				text = """/*
* Do not edit this file. It will be overwritten by the bootstrap-framework plugin.
*
*= require bootstrap/css/bootstrap.css
*= require bootstrap/css/bootstrap-theme.css
*/
"""
				break
			case "createBootstrapLessLess":
				text = """/*
* This file is for your Bootstrap Framework less and mixin customizations.
* It was created by the bootstrap-framework plugin.
* It will not be overwritten.
*
* You can import all less and mixin files as shown below,
* or you can import them individually.
* See https://github.com/kensiprell/bootstrap-framework/blob/master/README.md#less
*/

@import "bootstrap/less/bootstrap.less";

/*
* Your customizations go below this section.
*/
"""
				break
			case "createFontAwesomeCssAll":
				text = """/*
* Font Awesome by Dave Gandy - http://fontawesome.io
*
* Do not edit this file. It will be overwritten by the bootstrap-framework plugin.
*
*= require font-awesome/css/font-awesome.css
*= require_tree font-awesome/fonts
*/
"""
				break
			case "createFontAwesomeLessLessAssets":
				text = """/*
* Font Awesome by Dave Gandy - http://fontawesome.io
*
* This file is for your Font Awesome less and mixin customizations.
* It was created by the bootstrap-framework plugin.
* It will not be overwritten.
*
* You can import all less and mixin files as shown below,
* or you can import them individually.
* See https://github.com/kensiprell/bootstrap-framework/blob/master/README.md#font-awesome-less
*/

@import "font-awesome/less/font-awesome.less";

@fa-font-path: "/assets/font-awesome/fonts";

/*
* Your customizations go below this section.
*/
"""
				break
			case "createFontAwesomeLessLess":
				text = """/*
* Font Awesome by Dave Gandy - http://fontawesome.io
*
* This file is for your Font Awesome less and mixin customizations.
* It was created by the bootstrap-framework plugin.
* It will not be overwritten.
*
* You can import all less and mixin files as shown below,
* or you can import them individually.
* See https://github.com/kensiprell/bootstrap-framework/blob/master/README.md#font-awesome-less
*/

@import "font-awesome/less/font-awesome.less";

@fa-font-path: "/font-awesome/fonts";

/*
* Your customizations go below this section.
*/
"""
				break
		}
		text.toString()
	}

	static getFile(Project project, String template) {
		project.resources.text.fromString(getText(template)).asFile()
	}
}

class ZipFile {
	String fileSuffix = ".zip"

	def download(String tmp, String description, String filePrefix, String url, String version, String zipFilename, boolean invalidVersionFails) {
	    println "TEST url: $url"
		def tmpDir = new File("$tmp")
		if (!tmpDir.exists()) {
			tmpDir.mkdir()
		}
		def zipFile = new File("$tmp/$zipFilename")
		if (zipFile.exists()) {
			return zipFile
		}
		try {
		    // TODO check status for 200, otherwise throw exceptiion
			def file = zipFile.newOutputStream()
			file << new URL(url).openStream()
			file.close()
			return zipFile
		} catch (e) {
			zipFile.delete()
			def message = "Could not download $url.\n$version is an invalid $description version, or you are not connected to the Internet."
			if (invalidVersionFails) {
				throw new InvalidUserDataException(message)
			} else {
				println "Error: $message"
			}
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
				throw new InvalidUserDataException("No old $description zip files found in $tmpDir.")
			}
		}
	}
}

