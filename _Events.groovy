import org.codehaus.groovy.grails.test.event.GrailsTestEventConsoleReporter
import org.codehaus.groovy.grails.test.support.GrailsTestTypeSupport
import org.codehaus.groovy.grails.test.event.GrailsTestEventPublisher
import org.codehaus.groovy.grails.test.GrailsTestType

red='\033[31m'
blue="\033[34m"
cyan="\033[36m"
bold='\033[1m'
reset="\033[m"

eventTestPhasesStart = { args ->
	GrailsTestEventConsoleReporter.metaClass.doTestFailure = { String name, Object failure, boolean isError ->
	        if (++delegate.failureCount == 1) delegate.out.println()
	        delegate.out.println("                    ${name}...${bold}${red}FAILED${reset}")
	}

	GrailsTestEventConsoleReporter.metaClass.doTestCaseEnd = { String name, String out, String err ->
	        if (delegate.failureCount == 0)
			delegate.out.println("${bold}${cyan}PASSED${reset}")
	}


	delegate.runTests = { GrailsTestType type, File compiledClassesDir ->
		def testCount = type.prepare(delegate.testTargetPatterns, compiledClassesDir, delegate.binding)
    
		if (testCount) {
			try {
				delegate.event("TestSuiteStart", [type.name])
				delegate.println ""
				delegate.println "-------------------------------------------------------"
				delegate.println "Running ${testCount} $type.name test${testCount > 1 ? 's' : ''}..."

				def start = new Date()
				def result = type.run(testEventPublisher)
				def end = new Date()
            
				delegate.event("StatusUpdate", ["Tests Completed in ${end.time - start.time}ms"])

				if (result.failCount > 0) delegate.testsFailed = true
            
				delegate.println "-------------------------------------------------------"
				delegate.println "${bold}Tests passed: ${cyan}${result.passCount}${reset}"
				delegate.println "${bold}Tests failed: ${red}${result.failCount}${reset}"
				delegate.println "-------------------------------------------------------"
				delegate.event("TestSuiteEnd", [type.name])
			} catch (Exception e) {
				delegate.event("StatusFinal", ["Error running $type.name tests: ${e.toString()}"])
				GrailsUtil.deepSanitize(e)
				e.printStackTrace()
				delegate.testsFailed = true
			} finally {
				type.cleanup()
			}
		}
	}
}
