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
		String bootstrapJsPath = "${project.projectDir}/$jsPath/bootstrap"
		String bootstrapCssPath = "${project.projectDir}/$cssPath/bootstrap/css"
		String bootstrapFontsPath = "${project.projectDir}/$cssPath/bootstrap/fonts"
		String bootstrapLessPath = "${project.projectDir}/$cssPath/bootstrap/less"
		String bootstrapMixinsPath = "$bootstrapLessPath/mixins"
		boolean useAssetPipeline = jsPath.contains("assets")
		boolean bootstrapInvalidVersionFails = properties.invalidVersionFails ?: false
		FileTree bootstrapZipTree

		// Font Awesome properties
		def fontAwesome = properties.fontAwesome
		boolean fontAwesomeInstall = fontAwesome?.install ?: false
		String fontAwesomeVersion = fontAwesome?.version ?: FA_DEFAULT_VERSION
		boolean fontAwesomeUseLess = fontAwesome?.useLess ?: false
		boolean fontAwesomeInvalidVersionFails = fontAwesome?.invalidVersionFails ?: false
		String fontAwesomePath = "${project.projectDir}/$cssPath/font-awesome"
		String fontAwesomeCssPath = "$fontAwesomePath/css"
		String fontAwesomeFontsPath = "$fontAwesomePath/fonts"
		String fontAwesomeLessPath = "$fontAwesomePath/less"
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
				throw new InvalidUserDataException("bootstrapFramework.jsPath directory ($jsPath) does not exist.")
			}
		}

		project.task("downloadBootstrapZip") {
			String description = "Bootstrap Framework"
			String filePrefix = "bootstrap-v"
			String url = "https://github.com/twbs/bootstrap/archive/v${bootstrapVersion}.zip"
			String zipFilename = "${filePrefix}${bootstrapVersion}.zip"
			def file = zipFile.download(tmpDir, description, filePrefix, url, bootstrapVersion, zipFilename, bootstrapInvalidVersionFails)
			if (file instanceof File) {
				bootstrapZipTree = project.zipTree(file)
			} else if (file instanceof String) {
				throw new InvalidUserDataException(file)
			} else {
				throw new InvalidUserDataException("An unknown error occurred trying to download $url")
			}
		}

		project.task("downloadFontAwesomeZip") {
			if (fontAwesomeInstall) {
				String description = "Font Awesome"
				String filePrefix = "fontAwesome-v"
				String url = "https://github.com/FortAwesome/Font-Awesome/archive/v${fontAwesomeVersion}.zip"
				String zipFilename = "${filePrefix}${fontAwesomeVersion}.zip"
				def file = zipFile.download(tmpDir, description, filePrefix, url, fontAwesomeVersion, zipFilename, fontAwesomeInvalidVersionFails)
				if (file instanceof File) {
					fontAwesomeZipTree = project.zipTree(file)
				} else if (file instanceof String) {
					throw new InvalidUserDataException(file)
				} else {
					throw new InvalidUserDataException("An unknown error occurred trying to download $url")
				}
			}
		}

		project.task("manageBootstrapDirs") {
			if (!project.file(bootstrapJsPath).exists()) {
				project.mkdir(bootstrapJsPath)
			}
			if (!project.file(bootstrapCssPath).exists()) {
				project.mkdir(bootstrapCssPath)
			}
			if (!project.file(bootstrapFontsPath).exists()) {
				project.mkdir(bootstrapFontsPath)
			}
			if (useLess) {
				if (!project.file(bootstrapMixinsPath).exists()) {
					project.mkdir(bootstrapMixinsPath)
				}
			} else {
				project.delete(bootstrapLessPath)
			}
			if (fontAwesomeInstall) {
				def dirs = ["css", "fonts"]
				if (fontAwesomeUseLess) {
					dirs << "less"
				} else {
					project.delete(fontAwesomeLessPath)
				}
				dirs.each {
					project.mkdir("$fontAwesomePath/$it")
				}
			} else {
				project.delete(fontAwesomePath)
			}
		}

		project.task("createBootstrapJsAll", type: Copy, dependsOn: project.tasks.manageBootstrapDirs) {
			def path = "${project.projectDir}/$jsPath"
			def filename = "bootstrap-all.js"
			if (useAssetPipeline) {
				from template.getFile(project, "createBootstrapJsAll")
				rename ".*", filename
				into path
				onlyIf { !project.file("$path/$filename").exists() }
			} else {
				project.delete("$path/$filename")
			}
		}

		project.task("createBootstrapJs", type: Sync, dependsOn: project.tasks.createBootstrapJsAll) {
			def files = bootstrapZipTree.matching {
				include "*/dist/js/bootstrap.js"
				if (useIndividualJs) {
					include "*/js/*.js"
				}
			}.collect()
			from files
			into bootstrapJsPath
		}

		project.task("createBootstrapCssAll", type: Copy, dependsOn: project.tasks.createBootstrapJs) {
			def path = "${project.projectDir}/$cssPath"
			def filename = "bootstrap-all.css"
			if (useAssetPipeline) {
				from template.getFile(project, "createBootstrapCssAll")
				rename ".*", filename
				into path
				onlyIf { !project.file("$path/$filename").exists() }
			} else {
				project.delete("$path/$filename")
			}
		}

		project.task("createBootstrapFonts", type: Sync, dependsOn: project.tasks.createBootstrapCssAll) {
			def files = bootstrapZipTree.matching {
				include "*/fonts/*"
			}.collect()
			from files
			into bootstrapFontsPath
		}

		project.task("createBootstrapCssIndividual", type: Sync, dependsOn: project.tasks.createBootstrapFonts) {
			def files = bootstrapZipTree.matching {
				include "*/dist/css/*.css"
				exclude "*/dist/css/*.min.css"
			}.collect()
			from files
			into bootstrapCssPath
		}

		project.task("createBootstrapLessLess", type: Copy, dependsOn: project.tasks.createBootstrapCssIndividual) {
			def path = "${project.projectDir}/$cssPath"
			def filename = "bootstrap-less.less"
			if (useLess && useAssetPipeline) {
				from template.getFile(project, "createBootstrapLessLess")
				rename ".*", filename
				into path
				onlyIf { !project.file("$path/$filename").exists() }
			}
		}

		project.task("createBootstrapLess", type: Sync, dependsOn: project.tasks.createBootstrapLessLess) {
			def files = []
			if (useLess) {
				files = bootstrapZipTree.matching {
					include "*/less/*.less"
				}.collect()
			}
			from files
			into bootstrapLessPath
		}

		project.task("createBootstrapMixins", type: Sync, dependsOn: project.tasks.createBootstrapLess) {
			def files = []
			if (useLess) {
				files = bootstrapZipTree.matching {
					include "*/less/mixins/*.less"
				}.collect()
			}
			from files
			into bootstrapMixinsPath
		}

		project.task("createFontAwesomeCssAll", type: Copy, dependsOn: project.tasks.createBootstrapMixins) {
			def path = "${project.projectDir}/$cssPath"
			def filename = "font-awesome-all.css"
			if (fontAwesomeInstall && useAssetPipeline) {
				from template.getFile(project, "createFontAwesomeCssAll")
				rename ".*", filename
				into path
				onlyIf { !project.file("$path/$filename").exists() }
			} else {
				project.delete("$path/$filename")
			}
		}

		project.task("createFontAwesomeCssIndividual", type: Sync, dependsOn: project.tasks.createFontAwesomeCssAll) {
			def files = []
			if (fontAwesomeInstall) {
				files = fontAwesomeZipTree.matching {
					include "*/css/font-awesome.css"
				}.collect()
			}
			from files
			into fontAwesomeCssPath
		}

		project.task("createFontAwesomeFonts", type: Sync, dependsOn: project.tasks.createFontAwesomeCssIndividual) {
			def files = []
			if (fontAwesomeInstall) {
				files = fontAwesomeZipTree.matching {
					include "*/fonts/*"
				}.collect()
			}
			from files
			into fontAwesomeFontsPath
		}

		project.task("createFontAwesomeLessLess", type: Copy, dependsOn: project.tasks.createFontAwesomeFonts) {
			def path = "${project.projectDir}/$cssPath"
			def filename = "font-awesome-less.less"
			if (fontAwesomeInstall && fontAwesomeUseLess) {
				if (cssPath.contains("assets")) {
					from template.getFile(project, "createFontAwesomeLessLessAssets")
				} else {
					from template.getFile(project, "createFontAwesomeLessLess")
				}
				rename ".*", filename
				into path
				onlyIf { !project.file("$path/$filename").exists() }
			}
		}

		project.task("createFontAwesomeLess", type: Sync, dependsOn: project.tasks.createFontAwesomeLessLess) {
			def files = []
			if (fontAwesomeInstall && fontAwesomeUseLess) {
				files = fontAwesomeZipTree.matching {
					include "*/less/*.less"
				}.collect()
			}
			from files
			into fontAwesomeLessPath
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
		def zipFile = new File("$tmp/$zipFilename")
		if (zipFile.exists()) {
			return zipFile
		}
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection()
		def invalidVersionMessage = "Could not download $url.\n$version is an invalid $description version, or you are not connected to the Internet."
		def tmpDir = new File("$tmp")
		if (!tmpDir.exists()) {
			tmpDir.mkdir()
		}
		connection.setRequestMethod("GET")
		if (connection.getResponseCode() == 200) {
			def file = zipFile.newOutputStream()
			file << new URL(url).openStream()
			file.close()
			return zipFile
		} else {
			zipFile.delete()
			if (invalidVersionFails) {
				return invalidVersionMessage.toString()
			} else {
				println "Error: $invalidVersionMessage"
			}
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
			return "No old $description zip files found in $tmpDir.".toString()
		}
	}
}
