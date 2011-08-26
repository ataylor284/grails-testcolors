// -*- mode: Groovy; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-

import org.codehaus.groovy.grails.test.event.GrailsTestEventConsoleReporter
import org.codehaus.groovy.runtime.InvokerHelper
import org.fusesource.jansi.*

class GrailsTestEventConsoleReporterMetaClass extends DelegatingMetaClass {
    GrailsTestEventConsoleReporterMetaClass() {
        super(GrailsTestEventConsoleReporter.metaClass)
        initialize()
    }

    Object invokeConstructor(Object[] args) {
        new ColorizingGrailsTestEventConsoleReporter(args[0])
    }
}

class ColorizingGrailsTestEventConsoleReporter extends GrailsTestEventConsoleReporter {
    def colorWriter

    ColorizingGrailsTestEventConsoleReporter(PrintStream out) {
        super(out)
        colorWriter = new AnsiRenderWriter(AnsiConsole.wrapOutputStream(out), true)
    }
    
    protected doTestFailure(String name, failure, boolean isError) {
        if (++failureCount == 1) out.println()
        colorWriter.println("                    ${name}...@|red,bold FAILED|@")
    }

    protected doTestCaseEnd(String name, String out, String err) {
        if (failureCount == 0) colorWriter.println("@|cyan,bold PASSED|@")
    }
}

InvokerHelper.metaRegistry.setMetaClass(GrailsTestEventConsoleReporter.class, new GrailsTestEventConsoleReporterMetaClass())

eventTestPhasesStart = { args ->
    delegate.runTests.metaClass.println = { String str ->
        def colorWriter = new AnsiRenderWriter(AnsiConsole.out, true)
        def m = (str =~ /^Tests (\w+): (\d+)$/)
        if (m) {
            if (m[0][1] == 'passed') {
                colorWriter.println "@|bold Tests ${m[0][1]}:|@ @|cyan,bold ${m[0][2]}|@"
            } else {
                colorWriter.println "@|bold Tests ${m[0][1]}:|@ @|red,bold ${m[0][2]}|@"
            }
        } else {
            println str
        }
    }
}
