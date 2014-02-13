package org.grails.plugins.perf4j

/**
 * Share values between Perf4jGrailsPlugin, Perf4jFilters and maybe other classes
 * TODO isn't there risk with multithreading ?
 */
class SharedOptions {
    // indicates whether profiling is enabled AT ALL (according to config option or implicitly by environment)
    static Boolean profilingEnabled = false
    // indicates whether profiling is CURRENTLY enabled (as set via "profilingEnabled" property during runtime)
    static Boolean profilingCurrentlyEnabled = true

}
