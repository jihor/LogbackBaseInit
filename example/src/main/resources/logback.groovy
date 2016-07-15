import ch.qos.logback.classic.Level
import groovy.transform.BaseScript
import ru.jihor.logging.LogbackBaseInit

enum Loggers {
    BusinessEventLogger
}

enum Appenders {
    SplunkAppender, SplunkBusinessEventAppender, FileAppender, FileBusinessEventAppender
}

def init = {
    @BaseScript LogbackBaseInit logbackBaseInitScript
    splunkAppenders(["$Appenders.SplunkAppender"             : [index: System.getProperty("logging.splunk.indexes.main", "example"), includeMDC: true],
                     "$Appenders.SplunkBusinessEventAppender": [index: System.getProperty("logging.splunk.indexes.businessevents", "businessevents")]])
    fileAppenders(["$Appenders.FileAppender"             : [filename: "example.log", pattern: "%d{MM.dd-HH:mm:ss.SSS} [%thread] %-5level %logger - %msg%n"],
                   "$Appenders.FileBusinessEventAppender": [filename: "business-events.log", pattern: "%d{MM.dd-HH:mm:ss.SSS} %msg%n"]])

    attachSplunkAppenders({
        root(logLevel, ["$Appenders.SplunkAppender"])

        // prevent deadlocking and OOM errors on levels finer or equal to DEBUG with Splunk
        logger("org.apache.http", Level.INFO, null, true)
        // if DEBUG or finer logging must be enabled on org.apache.http package, it must be either
        // done to file with additivity=false, or Splunk logging must be turned off at all

        logger("$Loggers.BusinessEventLogger", Level.INFO, ["$Appenders.SplunkBusinessEventAppender"], false)
    })

    attachFileAppenders({
        root(logLevel, ["$Appenders.FileAppender"])
        logger("$Loggers.BusinessEventLogger", Level.INFO, ["$Appenders.FileBusinessEventAppender"], false)
    })

}

init()