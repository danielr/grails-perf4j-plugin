import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import grails.util.GrailsNameUtils as GNU
import org.grails.plugins.perf4j.SharedOptions
import org.perf4j.log4j.Log4JStopWatch
import org.apache.log4j.Logger


public class Perf4jFilters {
    // the name of the config property in controllers
    static final String PROFILED_PROPERTY = "profiled"
    // the key used to store the stopwatch object in the request
    static final String STOPWATCH_REQUEST_KEY = 'perf4jplugin.stopwatch'
    // the key used to store the includeView flag in the request
    static final String INCLUDE_VIEW_REQUEST_KEY = 'perf4jplugin.includeView'

    def log = Logger.getLogger(Perf4jFilters)

    def controllerProfiledOptionsCache
    
    
    def filters = {
        def log = Logger.getLogger(Perf4jFilters)
        
        all(controller:'*', action: '*') {
            before = {
                if(SharedOptions.profilingEnabled && SharedOptions.profilingCurrentlyEnabled) {
                    if(controllerName) {
                        def action = actionName ?: 'index'
                        def controller = GNU.getClassName(controllerName, "Controller")
                    
                        def controllerClass = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
                        if(controllerClass) {
                            def profiled = GCU.getStaticPropertyValue(controllerClass.clazz, PROFILED_PROPERTY)

                            if(profiled instanceof Boolean && profiled) {
                                log.trace "Boolean type profiled property in ${controller}"
                                createStopwatch(null, null, false, controller, action, request)
                            }
                            else if(profiled instanceof List && profiled.contains(action)) {
                                log.trace "Collection type profiled property in ${controller}"
                                createStopwatch(null, null, false, controller, action, request)
                            }
                            else if(profiled instanceof Closure) {
                                log.trace "Closure type profiled property in ${controller}"
                            
                                if(!controllerProfiledOptionsCache.hasOptionsForController(controllerName)) {
                                    log.trace "Evaluating profiled DSL in ${controller}"
                                    controllerProfiledOptionsCache.evaluateDSL(controllerName, profiled)
                                }
                                else {
                                    log.trace "Using cached profiling options for ${controller}"
                                }

                                def options = controllerProfiledOptionsCache.getOptions(controllerName, action)
                                if(options) {
                                    createStopwatch(options.tag, options.message, options.includeView as Boolean, controller, action, request)
                                }
                            }
                        }
                    }
                }
            }

            
            after = {
                if(SharedOptions.profilingEnabled && SharedOptions.profilingCurrentlyEnabled) {
                    def includeView = request[INCLUDE_VIEW_REQUEST_KEY]
            
                    if(!includeView) {
                        stopStopwatch(request)
                    }
                }
            }


            afterView = {
                if(SharedOptions.profilingEnabled && SharedOptions.profilingCurrentlyEnabled) {
                    def includeView = request[INCLUDE_VIEW_REQUEST_KEY]
            
                    if(includeView) {
                        stopStopwatch(request)
                    }
                }
            }
        }
    }
    
    
    /**
     *  Create the stop watch and store it in the request, so it can be accessed in the after/afterView interceptors.
     */
    private createStopwatch(String tag, String message, Boolean includeView, String controllerName, String actionName, request) {
        if(!tag) {
            tag = "${controllerName}.${actionName}"
        }
        
        def stopwatch = new Log4JStopWatch(tag, message)

        request[STOPWATCH_REQUEST_KEY] = stopwatch
        request[INCLUDE_VIEW_REQUEST_KEY] = includeView
        
        log.trace "Stop watch started: ${tag}"
    }
    
    
    /**
     *  Stop the stopwatch (if there is one).
     */
    private stopStopwatch(request) {
        def stopwatch = request[STOPWATCH_REQUEST_KEY]
        if(stopwatch) {
            stopwatch.stop()
            log.trace "Stop watch stopped: ${stopwatch.tag}"
        }
    }
}