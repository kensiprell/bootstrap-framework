package com.siprell.plugin.gradle.bootstrap

import java.text.SimpleDateFormat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class GradlePluginSpec extends Specification {
    
	static cssPath = "src/main/webapp/css"
    static jsPath = "src/main/webapp/js"
	static grailsCssPath = "grails-app/assets/stylesheets"
    static grailsJsPath = "grails-app/assets/javascripts"
	static boolean useAssetPipeline = true

	def setupSpec() {
	    deleteDirs()
    	[
    	    "$filePath.root/$cssPath",
            "$filePath.root/$jsPath",
    	    "$filePath.root/$grailsCssPath",
            "$filePath.root/$grailsJsPath"
        ].each {
            new File("$it").mkdirs()
        }
	}
	
	def setup() {
	    useAssetPipeline = true
	}
	
	def cleanupSpec() {
	    deleteDirs()
	}
	
	// TODO test missing directory exceptions

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

	void "change Bootstrap Framework version to #testVersion"() {
		when:
		def testVersion = "3.3.4"
		def properties = [
		version				: testVersion,
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
		def prefix = "* Bootstrap v"
		def suffix = " (http://getbootstrap.com)"
		def cssFile = new File("$filePath.css/bootstrap.css")
		def jsFile = new File("$filePath.js/bootstrap.js")
		def cssFileVersion = cssFile.readLines().get(1).trim() - prefix - suffix
		def jsFilesVersion = jsFile.readLines().get(1).trim() - prefix - suffix

		then:
		testVersion == cssFileVersion
		testVersion == jsFilesVersion
	}

	void "apply plugin using default settings"() {
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
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
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: cssPath,
		jsPath				: jsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
		useAssetPipeline = false
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
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: true,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
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
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: true,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
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
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: true,
		useLess				: true,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: false,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
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

	void "change Font Awesome version to #testVersion"() {
		when:
		def testVersion = "4.2.0"
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: true,
			version				: testVersion,
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
		def prefix = "*  Font Awesome "
		def suffix = " by @davegandy - http://fontawesome.io - @fontawesome"
		def cssFile = new File("$filePath.faCss/font-awesome.css")
		def cssFileVersion = cssFile.readLines().get(1).trim() - prefix - suffix

		then:
		testVersion == cssFileVersion
	}

	void "apply plugin using default Bootstrap Framework and Font Awesome settings"() {
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: true,
			version				: "4.3.0",
			useLess				: false,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		data.bootstrapLessLess.exists()
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
		!data.fontAwesomeLessLess.exists() 
		data.fontAwesomeCss.exists()
		data.fontAwesomeFonts.exists()
		data.fontAwesomeFontsCount == 6
		!data.fontAwesomeLess.exists()
		data.fontAwesomeLessCount == 0
	}

	void "apply plugin using Bootstrap Framework default settings and Font Awesome with LESS support"() {
		when:
		def properties = [
		version				: "3.3.5",
		cssPath				: grailsCssPath,
		jsPath				: grailsJsPath,
		useIndividualJs		: false,
		useLess				: false,
		invalidVersionFails	: false,
		fontAwesome : [
			install				: true,
			version				: "4.3.0",
			useLess				: true,
			invalidVersionFails	: false
			]
		]
		createProject(properties)
		def data = currentData

		then:
		data.bootstrapAllJs.exists()
		data.javascriptsCount == 2
		data.bootstrapJs.exists()
		data.jsCount == 1
		data.bootstrapAllCss.exists()
		data.bootstrapLessLess.exists()
		data.stylesheetsCount == 6
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

	def deleteDirs() {
		new File("$filePath.root/grails-app").deleteDir()
		new File("$filePath.root/src/main/webapp").deleteDir()
	}
	
	static createProject(properties) {
		Project project = ProjectBuilder.builder().withProjectDir(new File(filePath.root)).build()
		project.ext.bootstrapFramework = properties
		project.pluginManager.apply "bootstrap-framework"
		project.tasks["downloadBootstrapZip"].execute()
		project.tasks["downloadFontAwesomeZip"].execute()
		project.tasks["createBootstrapJsAll"].execute()
		project.tasks["createBootstrapJs"].execute()
		project.tasks["createBootstrapCssAll"].execute()
		project.tasks["createBootstrapFonts"].execute()
		project.tasks["createBootstrapCssIndividual"].execute()
		project.tasks["createBootstrapLessLess"].execute()
		project.tasks["createBootstrapLess"].execute()
		project.tasks["createBootstrapMixins"].execute()
		project.tasks["downloadFontAwesomeZip"].execute()
		project.tasks["createFontAwesomeCssAll"].execute()
		project.tasks["createFontAwesomeCssIndividual"].execute()
		project.tasks["createFontAwesomeFonts"].execute()
		project.tasks["createFontAwesomeLessLess"].execute()
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

