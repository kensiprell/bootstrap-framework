package com.siprell.plugin.gradle.bootstrap

import java.text.SimpleDateFormat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradlePluginSpec extends Specification {

	static final bootstrapDefaultVersion = "3.3.5"
	static final bootstrapTestVersion = "3.3.4"
	static final fontAwesomeDefaultVersion = "4.3.0"
	static final fontAwesomeTestVersion = "4.2.0"
	static final grailsJsPath = "grails-app/assets/javascripts"
	static final grailsCssPath = "grails-app/assets/stylesheets"
	static final jsPath = "src/main/webapp/resources/js"
	static final cssPath = "src/main/webapp/resources/css"
	static boolean useAssetPipeline

	def setup() {
		useAssetPipeline = true
		deleteTestFiles()
	}

	def cleanupSpec() {
		deleteTestFiles()
	}

	// TODO
	//invalidVersionFails property
	// exception thrown (mrhaki.blogspot.de/2011/01/spocklight-check-for-exceptions-with.html)
	void "use invalid Bootstrap Framework version"() {
		given:
		true
	}

	// TODO
	//invalidVersionFails property
	// exception thrown (mrhaki.blogspot.de/2011/01/spocklight-check-for-exceptions-with.html)
	void "use invalid Font Awesome version"() {
		given:
		true
	}

	// TODO
	void "change Font Awesome version to #fontAwesomeTestVersion"() {
		given:
		true
	}

	void "change Bootstrap Framework version to #bootstrapTestVersion"() {
		given:
		createProject(false, false, false, false, false, false)
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
		createProject(true, false, false, false, false, false)

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
		!data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 0
		!data.fontAwesomeCss.exists()
		!data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 0
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin without asset-pipeline"() {
		given:
		useAssetPipeline = false
		createProject(true, false, false, false, false, false)

		when:
		def data = currentData

		then:
		!data.bootstrapAllJs.exists()
		data.javascriptsCount == 1
		data.bootstrapJs.exists()
		data.jsCount == 1
		!data.bootstrapAllCss.exists()
		!data.bootstrapLessLess.exists()
		data.stylesheetsCount == 1
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
		!data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 0
		!data.fontAwesomeCss.exists()
		!data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 0
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using Bootstrap Framework individual JavaScript files"() {
		given:
		createProject(true, true, false, false, false, false)

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
		!data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 0
		!data.fontAwesomeCss.exists()
		!data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 0
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using Bootstrap Framework LESS support"() {
		given:
		createProject(true, false, true, false, false, false)

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
		!data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 0
		!data.fontAwesomeCss.exists()
		!data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 0
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using Bootstrap Framework version LESS support and individual JavaScript files"() {
		given:
		createProject(true, true, true, false, false, false)

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
		!data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 0
		!data.fontAwesomeCss.exists()
		!data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 0
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using default Bootstrap Framework and Font Awesome settings"() {
		given:
		createProject(true, false, false, true, true, false)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		!data.bootstrapLessLess.exists()
		data.stylesheetsCount == 4
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
		data.fontAwesomeAllCss.exists()
		!data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 2
		data.fontAwesomeCss.exists()
		data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 6
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using Bootstrap Framework default settings and Font Awesome with LESS support"() {
		given:
		createProject(true, false, false, true, true, true)

		when:
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		!data.bootstrapLessLess.exists()
		data.stylesheetsCount == 5
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
		data.fontAwesomeAllCss.exists()
		data.fontAwesomeLessLess.exists()
		data.stylesheetsFontAwesomeCount == 3
		data.fontAwesomeCss.exists()
		data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 6
		data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 13
	}

	static deleteTestFiles() {
		new File("$filePath.root/grails-app").deleteDir()
		new File("$filePath.root/src/main/webapp").deleteDir()
	}

	static createProject(
		boolean defaultVersion,
		boolean useIndividualJs,
		boolean useLess,
		boolean useFontAwesome,
		boolean useFontAwesomeDefaultVersion,
		boolean useFontAwesomeLess
	) {
		Project project = ProjectBuilder.builder().withProjectDir(new File(filePath.root)).build()
		String version = defaultVersion ? bootstrapDefaultVersion : bootstrapTestVersion
		String faVersion = useFontAwesomeDefaultVersion ? fontAwesomeDefaultVersion : fontAwesomeTestVersion
		if (useFontAwesome) {
			project.ext.bootstrapFramework = [
				version        : version,
				cssPath        : filePath.stylesheets,
				jsPath         : filePath.javascripts,
				useIndividualJs: useIndividualJs,
				useLess        : useLess,
				fontAwesome    : [
					install: true,
					version: faVersion,
					useLess: useFontAwesomeLess
				]
			]
		} else {
			project.ext.bootstrapFramework = [
				version        : version,
				cssPath        : filePath.stylesheets,
				jsPath         : filePath.javascripts,
				useIndividualJs: useIndividualJs,
				useLess        : useLess
			]
		}
		project.pluginManager.apply "bootstrap-framework-gradle"
		project.tasks["downloadBootstrapZip"].execute()
		project.tasks["downloadFontAwesomeZip"].execute()
		project.tasks["createBootstrapJsAll"].execute()
		project.tasks["createBootstrapJs"].execute()
		project.tasks["createBootstrapCssAll"].execute()
		project.tasks["createBootstrapFonts"].execute()
		project.tasks["createBootstrapCssIndividual"].execute()
		project.tasks["createBootstrapLessAll"].execute()
		project.tasks["createBootstrapLess"].execute()
		project.tasks["createBootstrapMixins"].execute()
		project.tasks["downloadFontAwesomeZip"].execute()
		project.tasks["createFontAwesomeCssAll"].execute()
		project.tasks["createFontAwesomeCssIndividual"].execute()
		project.tasks["createFontAwesomeFonts"].execute()
		project.tasks["createFontAwesomeLessAll"].execute()
		project.tasks["createFontAwesomeLess"].execute()
	}

	static getFilePath() {
		String root = new File("").absolutePath.toString()
		String javascripts = useAssetPipeline ? grailsJsPath : jsPath
		String stylesheets = useAssetPipeline ? grailsCssPath : cssPath
		String js = "$javascripts/bootstrap"
		String css = "$stylesheets/bootstrap/css"
		String fonts = "$stylesheets/bootstrap/fonts"
		String less = "$stylesheets/bootstrap/less"
		String mixins = "$less/mixins"
		String faCss = "$stylesheets/font-awesome/css"
		String faFonts = "$stylesheets/font-awesome/fonts"
		String faLess = "$stylesheets/font-awesome/less"

		[
			root       : root,
			javascripts: javascripts,
			js         : js,
			stylesheets: stylesheets,
			css        : css,
			fonts      : fonts,
			less       : less,
			mixins     : mixins,
			faCss      : faCss,
			faFonts    : faFonts,
			faLess     : faLess
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
		// FontAwesome
		def fontAwesomeAllCss = new File("${filePath.stylesheets}/font-awesome-all.css")
		def fontAwesomeLessLess = new File("${filePath.stylesheets}/font-awesome-less.less")
		def stylesheetsFontAwesome = new File("${filePath.stylesheets}/font-awesome")
		def stylesheetsFontAwesomeCount = stylesheetsFontAwesome.exists() ? stylesheetsFontAwesome.listFiles().size() : 0
		def fontAwesomeCss = new File("${filePath.faCss}/font-awesome.css")
		def fontAwesomeFonts = new File(filePath.faFonts)
		def fontAwesomeFontsCount = fontAwesomeFonts.exists() ? fontAwesomeFonts.listFiles().size() : 0
		def fontAwesomeLess = new File(filePath.faLess)
		def fontAwesomeLessCount = fontAwesomeLess.exists() ? fontAwesomeLess.listFiles().size() : 0

		[
			bootstrapAllJs             : bootstrapAllJs,
			javascriptsCount           : javascriptsCount,
			bootstrapJs                : bootstrapJs,
			jsCount                    : jsCount,
			bootstrapAllCss            : bootstrapAllCss,
			bootstrapLessLess          : bootstrapLessLess,
			stylesheetsCount           : stylesheetsCount,
			stylesheetsBootstrapCount  : stylesheetsBootstrapCount,
			bootstrapCss               : bootstrapCss,
			bootstrapThemeCss          : bootstrapThemeCss,
			css                        : css,
			cssCount                   : cssCount,
			fonts                      : fonts,
			fontsCount                 : fontsCount,
			bootstrapLess              : bootstrapLess,
			mixinsLess                 : mixinsLess,
			less                       : less,
			lessCount                  : lessCount,
			mixins                     : mixins,
			mixinsCount                : mixinsCount,
			fontAwesomeAllCss          : fontAwesomeAllCss,
			fontAwesomeLessLess        : fontAwesomeLessLess,
			stylesheetsFontAwesomeCount: stylesheetsFontAwesomeCount,
			fontAwesomeCss             : fontAwesomeCss,
			fontAwesomeFonts           : fontAwesomeFonts,
			fontAwesomeFontsCount      : fontAwesomeFontsCount,
			fontAwesomeLess            : fontAwesomeLess,
			fontAwesomeLessCount       : fontAwesomeLessCount
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

