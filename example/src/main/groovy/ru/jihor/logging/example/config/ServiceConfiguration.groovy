package ru.jihor.logging.example.config

import ch.qos.logback.classic.Logger
import groovy.util.logging.Slf4j
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 *
 *
 * @author jihor (jihor@ya.ru)
 * Created on 2016-07-15
 */
@RestController
@Slf4j
class ServiceConfiguration {
    static Logger businessEventLog = (Logger) LoggerFactory.getLogger("BusinessEventLogger")

    @RequestMapping(value = "/hello/{name}", method = RequestMethod.GET)
    String hello(@PathVariable String name) {
        def number = new Random().nextInt(20)

        MDC.put "name", name
        MDC.put "threadId", String.valueOf(Thread.currentThread().getId())

        businessEventLog.info "Received 'hello' event"
        log.info "Magic number for $name is $number"

        "Hello, $name. Your magic number is $number"
    }

}
