package org.grails.plugins.perf4j

/**
 *  Global cache for profiling options of controllers.
 */
class ControllerProfiledOptionsCache {
    // profiling options for each controller which has a "profiled" property of type Closure (controller name is map key)
    // this is needed to cache the options, so we don't have to execute the closure upon each request
    private profilingOptions = [:].asSynchronized()
    
    boolean hasOptionsForController(String controllerName) {
        return this.profilingOptions.containsKey(controllerName)
    }
    
    void evaluateDSL(String controllerName, Closure callable) {
        // run closure with builder as delegate
        def builder = new ProfiledOptionsBuilder()
        callable.delegate = builder
        callable.resolveStrategy = Closure.DELEGATE_ONLY
        callable.call()
        this.profilingOptions[controllerName] = builder.profiledMap
    }
    
    Map getOptions(controllerName, actionName) {
        this.profilingOptions[controllerName]?.get(actionName)
    }
}