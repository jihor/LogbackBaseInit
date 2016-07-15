package ru.jihor.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import com.splunk.logging.HttpEventCollectorLogbackAppender

import java.nio.charset.Charset

import static ru.jihor.logging.SystemPropertyNames.LOGGING_FILE_DIRECTORY
import static ru.jihor.logging.SystemPropertyNames.LOGGING_FILE_ENABLED
import static ru.jihor.logging.SystemPropertyNames.LOGGING_LEVEL
import static ru.jihor.logging.SystemPropertyNames.LOGGING_SPLUNK_ENABLED
import static ru.jihor.logging.SystemPropertyNames.LOGGING_SPLUNK_HOST
import static ru.jihor.logging.SystemPropertyNames.LOGGING_SPLUNK_PORT
import static ru.jihor.logging.SystemPropertyNames.LOGGING_SPLUNK_PROTOCOL
import static ru.jihor.logging.SystemPropertyNames.LOGGING_SPLUNK_TOKEN

/**
 *
 *
 * @author jihor (jihor@ya.ru)
 * Created on 2016-07-15
 */

abstract class LogbackBaseInit extends Script {

    def allowedProtocols = ["http", "https"]

    def splunkAppenders = []

    def splunkAppenders(map) { this.splunkAppenders = map }

    def attachSplunkAppenders = {}

    def attachSplunkAppenders(closure) { this.attachSplunkAppenders = closure }

    def fileAppenders = []

    def fileAppenders(map) { this.fileAppenders = map }

    def attachFileAppenders = {}

    def attachFileAppenders(closure) { this.attachFileAppenders = closure }

    def run() {

        initPropertiesCommon()
        initPropertiesSplunk()
        initPropertiesFile()

        initPropertiesAdditional()

        println "\n===== Logging properies =====\n"
        binding.getVariables().sort().each { println "$it.key = [$it.value]" }
        println "\n===== End of logging properies =====\n"

        setupSplunk()
        setupFile()
        setupJMX()
    }

    Object setupJMX() {
        jmxConfigurator('ru.jihor.logging:type=LoggerManager')
    }

    def initPropertiesCommon() {
        setProperty("logLevel", Level.valueOf(System.getProperty(LOGGING_LEVEL, "INFO")))
    }

    abstract def initPropertiesAdditional() // Body of closure annotated with @BaseScript will be executed as the implementation of this method

    def initPropertiesSplunk() {
        setProperty("enableLogToSplunk", System.getProperty(LOGGING_SPLUNK_ENABLED, "true").toBoolean())
        if (enableLogToSplunk.toBoolean()) {
            def splunkProtocol = System.getProperty(LOGGING_SPLUNK_PROTOCOL)
            if (!(splunkProtocol in allowedProtocols)) {
                println "Splunk protocol was not not in allowed list of protocols(actual value was [$splunkProtocol], " +
                        "allowed protocols are $allowedProtocols, falling back to default value..."
                splunkProtocol = "http"
            }
            setProperty("splunkProtocol", splunkProtocol)
            setProperty("splunkHost", System.getProperty(LOGGING_SPLUNK_HOST, "127.0.0.1"))
            def splunkPort = System.getProperty(LOGGING_SPLUNK_PORT)
            if (splunkPort != null && splunkPort.isInteger()) {
                splunkPort = splunkPort.toInteger()
            } else {
                println "Splunk port was not defined or couldn't be parsed as integer value (actual value was [$splunkPort]), falling back to default value..."
                splunkPort = 8088
            }
            setProperty("splunkPort", splunkPort)
            setProperty("splunkUrl", "$splunkProtocol://$splunkHost:$splunkPort")
            setProperty("splunkToken", System.getProperty(LOGGING_SPLUNK_TOKEN, "no-token"))
        }
    }

    def initPropertiesFile() {
        setProperty("enableLogToFile", System.getProperty(LOGGING_FILE_ENABLED, "true").toBoolean())
        setProperty("logPath", System.getProperty(LOGGING_FILE_DIRECTORY, "logs"))
    }

    def setupFile() {
        if (enableLogToFile.toBoolean()) {
            fileAppenders.each { fileAppender(it.key, it.value['filename'], it.value['pattern']) }
            attachFileAppenders()
        }
    }

    def setupSplunk() {
        if (enableLogToSplunk.toBoolean()) {
            splunkAppenders.each { splunkAppender(it.key, it.value['index'], it.value['includeMDC']?: false) }
            attachSplunkAppenders()
        }
    }

    def fileAppender = { name, filename, _pattern ->
        // binding is not propagated to inner scope so we must copy the variables
        def _logPath = logPath
        println "Creating File Appender with name = $name, filename = $filename, " +
                "pattern = $_pattern, logPath = $_logPath"
        appender(name, RollingFileAppender) {
            file = "$_logPath/$filename"
            rollingPolicy(FixedWindowRollingPolicy) {
                fileNamePattern = "$_logPath/$filename.%i.zip"
                minIndex = 1
                maxIndex = 5
            }
            triggeringPolicy(SizeBasedTriggeringPolicy) {
                maxFileSize = "64MB"
            }
            encoder(PatternLayoutEncoder) {
                charset = Charset.forName("UTF-8")
                pattern = _pattern
            }
        }
    }

    def splunkAppender = { name, _index, _includeMDC ->
        // binding is not propagated to inner scope so we must copy the variables
        def _splunkUrl = splunkUrl
        def _splunkToken = splunkToken
        println "Creating Splunk Appender with name = $name, index = $_index, splunkUrl = $_splunkUrl, splunkToken = $_splunkToken"
        appender(name, HttpEventCollectorLogbackAppender) {
            url = _splunkUrl
            token = _splunkToken
            sourcetype = "json-escaped"
            index = _index
            layout(JsonLayout) {
                includeMDC = _includeMDC
            }
        }
    }
}