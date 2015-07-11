package com.siprell.plugin.gradle.bootstrap

import java.text.SimpleDateFormat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradlePluginSpec extends Specification {

	static final bootstrapDefaultVersion = "3.3.5"
	static final bootstrapTestVersion = "3.3.4"

	def setup() {
		deleteTestFiles()
	}

	// TODO
	void "use invalid bootstrapFrameworkVersion"(){
		given:
		true
	}

	void "change bootstrapFrameworkVersion to #bootstrapTestVersion"() {
		given:
		createProject(false, false, false)
		def prefix = "* Bootstrap v"
		def suffix = " (http://getbootstrap.com)"

		when:
		def cssFile = new File("$filePath.css/bootstrap.css")
		def jsFile = new File("$filePath.js/bootstrap.js")
		def cssFileVersion = cssFile.readLines().get(1).trim() - prefix - suffix
		def jsFilesVersion = jsFile.readLines().get(1).trim() - prefix - suffix

		then:
		bootstrapTestVersion == cssFileVersion
		bootstrapTestVersion == jsFilesVersion
	}

	void "apply plugin using default settings"() {
		given:
		createProject(true, false, false)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		!data.bootstrapLessLess.exists()
		data.stylesheetsCount == 2
		data.stylesheetsBootstrapCount == 2
		data.bootstrapCss.exists()
		data.bootstrapThemeCss.exists()
		data.css.exists()
		data.cssCount == 2
		data.fonts.exists()
		data.fontsCount == 5
		!data.bootstrapLess.exists()
		!data.mixinsLess.exists()
		!data.less.exists()
		data.lessCount == 0
		!data.mixins.exists()
		data.mixinsCount == 0
	}

	void "apply plugin using individual JavaScript files"() {
		given:
		createProject(true, true, false)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 13
		data.bootstrapAllCss.exists()
		!data.bootstrapLessLess.exists()
		data.stylesheetsCount == 2
		data.stylesheetsBootstrapCount == 2
		data.bootstrapCss.exists()
		data.bootstrapThemeCss.exists()
		data.css.exists()
		data.cssCount == 2
		data.fonts.exists()
		data.fontsCount == 5
		!data.bootstrapLess.exists()
		!data.mixinsLess.exists()
		!data.less.exists()
		data.lessCount == 0
		!data.mixins.exists()
		data.mixinsCount == 0
	}

	void "apply plugin using LESS support"() {
		given:
		createProject(true, false, true)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		data.bootstrapLessLess.exists()
		data.stylesheetsCount == 3
		data.stylesheetsBootstrapCount == 3
		data.bootstrapCss.exists()
		data.bootstrapThemeCss.exists()
		data.css.exists()
		data.cssCount == 2
		data.fonts.exists()
		data.fontsCount == 5
		data.bootstrapLess.exists()
		data.mixinsLess.exists()
		data.less.exists()
		data.lessCount == 42
		data.mixins.exists()
		data.mixinsCount == 30
	}

	void "apply plugin using LESS support and individual JavaScript files"() {
		given:
		createProject(true, true, true)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 13
		data.bootstrapAllCss.exists()
		data.bootstrapLessLess.exists()
		data.stylesheetsCount == 3
		data.stylesheetsBootstrapCount == 3
		data.bootstrapCss.exists()
		data.bootstrapThemeCss.exists()
		data.css.exists()
		data.cssCount == 2
		data.fonts.exists()
		data.fontsCount == 5
		data.bootstrapLess.exists()
		data.mixinsLess.exists()
		data.less.exists()
		data.lessCount == 42
		data.mixins.exists()
		data.mixinsCount == 30
	}

	static deleteTestFiles() {
	    new File("$filePath.stylesheets").listFiles().each { it.delete() }
	    new File("$filePath.javascripts").listFiles().each { it.delete() }
	}

	static createProject(boolean defaultVersion, boolean useIndividualJs, boolean useLess) {
		Project project = ProjectBuilder.builder().withProjectDir(new File(filePath.root)).build()
		String version = defaultVersion ? bootstrapDefaultVersion : bootstrapTestVersion
		project.ext.bootstrapFrameworkVersion = version
		project.ext.bootstrapFrameworkUseIndividualJs = useIndividualJs
		project.ext.bootstrapFrameworkUseLess = useLess
		project.pluginManager.apply "bootstrap-framework-gradle"
		project.tasks["createBootstrapJsAll"].execute()
		project.tasks["createBootstrapJs"].execute()
		project.tasks["createBootstrapCssAll"].execute()
		project.tasks["createBootstrapFonts"].execute()
		project.tasks["createBootstrapCssIndividual"].execute()
		project.tasks["createBootstrapLessAll"].execute()
		project.tasks["createBootstrapLess"].execute()
		project.tasks["createBootstrapMixins"].execute()
	}

	static getFilePath() {
		String root = new File("").absolutePath.toString()
		String javascripts = "$root/grails-app/assets/javascripts"
		String js = "$javascripts/bootstrap"
		String stylesheets = "$root/grails-app/assets/stylesheets"
		String css = "$stylesheets/bootstrap/css"
		String fonts = "$stylesheets/bootstrap/fonts"
		String less = "$stylesheets/bootstrap/less"
		String mixins = "$less/mixins"
		[
			root       : root,
			javascripts: javascripts,
			js         : js,
			stylesheets: stylesheets,
			css        : css,
			fonts      : fonts,
			less       : less,
			mixins     : mixins
		]
	}

	static getCurrentData() {
		def bootstrapAllJs = new File("${filePath.javascripts}/bootstrap-all.js")
		def javascriptsCount = new File(filePath.javascripts).listFiles().size()
		def bootstrapJs = new File("${filePath.js}/bootstrap.js")
		def jsCount = new File(filePath.js).listFiles().size()
		def bootstrapAllCss = new File("${filePath.stylesheets}/bootstrap-all.css")
		def bootstrapLessLess = new File("${filePath.stylesheets}/bootstrap-less.less")
		def stylesheetsCount = new File(filePath.stylesheets).listFiles().size()
		def stylesheetsBootstrapCount = new File("${filePath.stylesheets}/bootstrap").listFiles().size()
		def bootstrapCss = new File("${filePath.css}/bootstrap.css")
		def bootstrapThemeCss = new File("${filePath.css}/bootstrap-theme.css")
		def css = new File(filePath.css)
		def cssCount = css.exists() ? css.listFiles().size() : 0
		def fonts = new File(filePath.fonts)
		def fontsCount = fonts.exists() ? fonts.listFiles().size() : 0
		def bootstrapLess = new File("${filePath.less}/bootstrap.less")
		def mixinsLess = new File("${filePath.less}/mixins.less")
		def less = new File(filePath.less)
		def lessCount = less.exists() ? less.listFiles().size() : 0
		def mixins = new File(filePath.mixins)
		def mixinsCount = mixins.exists() ? mixins.listFiles().size() : 0
		[
			bootstrapAllJs   : bootstrapAllJs,
			javascriptsCount : javascriptsCount,
			bootstrapJs      : bootstrapJs,
			jsCount          : jsCount,
			bootstrapAllCss  : bootstrapAllCss,
			bootstrapLessLess: bootstrapLessLess,
			stylesheetsCount : stylesheetsCount,
			stylesheetsBootstrapCount: stylesheetsBootstrapCount,
			bootstrapCss     : bootstrapCss,
			bootstrapThemeCss: bootstrapThemeCss,
			css              : css,
			cssCount         : cssCount,
			fonts            : fonts,
			fontsCount       : fontsCount,
			bootstrapLess    : bootstrapLess,
			mixinsLess       : mixinsLess,
			less             : less,
			lessCount        : lessCount,
			mixins           : mixins,
			mixinsCount      : mixinsCount
		]
	}

	static getPrettyDate(Long millis) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S yyyy.MM.dd")
		sdf.format(new Date(millis))
	}

	static getPrettyDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S yyyy.MM.dd")
		sdf.format(date)
	}
}

