package org.grails.plugins.perf4j

/**
 *  The builder to evaluate the DSL used if the profiled property is a Closure.
 */
class ProfiledOptionsBuilder {
    def profiledMap = [:]
    
    def methodMissing(String name, args) {
        if(args.length > 0) {
            if(!args[0] instanceof Map) {
                throw new RuntimeException("Argument for methods in profiled DSL must be of type Map")
            }
            profiledMap[name] = args[0]
        }
        else {
            profiledMap[name] = [:]
        }
    }
}
