/*
 * Copyright 2014 Thomas Bouffard (redfish4ktc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.eviware.soapui.report;

import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestStepResult.TestStepStatus;
import com.eviware.soapui.support.xml.XmlUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

// TODO for multi projects, could be nice to prefix testsuite by the name of the project
// could be done in a subclass or with an option (see http://blog.infostretch.com/customizing-soapui-reports)
// TODO move in a regular package
// currently in eviware package to let the class access to the package protected failures field for instance
// warn, update integration tests
// TODO modify log level used to debug
// TODO use stringbuilder instead StringBuffer
public class StepInfoJUnitReportCollector extends JUnitSecurityReportCollector {
    private static final Logger log = Logger.getLogger(StepInfoJUnitReportCollector.class);
    
    private static final String EOL = System.getProperty("line.separator");
    
    Map<TestCase, String> pendingSuccess = new HashMap<TestCase,String>();

    @Override
    public void afterStep(TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result) {
        TestStep currentStep = result.getTestStep();
        TestCase testCase = currentStep.getTestCase();
        
        log.error("##### testcase: " + testCase.getName());
        log.error("##### currentStep: " + currentStep.getName());


        if (result.getStatus() == TestStepStatus.FAILED) {
            // TODO restore this behaviour
//            if (maxErrors > 0) {
//                Integer errors = errorCount.get(testCase);
//                if (errors == null)
//                    errors = 0;
//
//                if (errors >= maxErrors)
//                    return;
//
//                errorCount.put(testCase, errors + 1);
//            }

            StringBuffer buf = new StringBuffer();
            
            // previous failure
            if (failures.containsKey(testCase)) {
                buf.append(failures.get(testCase));
//                appendBreakLine(buf);
            }
            // no previous failure, but success
            else if (pendingSuccess.containsKey(testCase)) {
                String testCasePendingSuccess = pendingSuccess.get(testCase);
                log.error("##### existing pending success, adding: " + testCasePendingSuccess);
                buf.append(testCasePendingSuccess);
//                appendBreakLine(buf);
                
                pendingSuccess.remove(testCase);
            }
                
//            buf.append("<h3><b>").append(XmlUtils.entitize(currentStep.getName())).append(" Failed</b></h3><pre>");
            appendTestStepStatus(buf, currentStep, result);
            for (String message : result.getMessages()) {
                if (message.toLowerCase().startsWith("url:")) {
                    String url = XmlUtils.entitize(message.substring(4).trim());
                    buf.append("URL: <a target=\"new\" href=\"").append(url).append("\">").append(url).append("</a>");
                } else {
                    buf.append(message);
                }
                appendBreakLine(buf);
            }
            appendTestAssertionsOfTestStepIfAvailable(buf, currentStep);

            // use string value since constant is defined in pro.. duh..
            if (testRunner.getTestCase().getSettings().getBoolean("Complete Error Logs")) {
                log.error("##### process complete error logs!!!!");
                StringWriter stringWriter = new StringWriter();
                PrintWriter writer = new PrintWriter(stringWriter);
                result.writeTo(writer);

                buf.append(XmlUtils.entitize(stringWriter.toString()));
                // TODO break line needed?
            }

//            buf.append("</pre><hr/>");

            failures.put(testCase, buf.toString());
        }
        else {
            log.error("##### Success");
            StringBuffer buf = new StringBuffer();
            if (pendingSuccess.containsKey(testCase)) {
                buf.append(pendingSuccess.get(testCase));
//                appendBreakLine(buf);
            }
            appendTestStepStatus(buf, currentStep, result);
            appendTestAssertionsOfTestStepIfAvailable(buf, currentStep);
            
            if (failures.containsKey(testCase)) {
                // any prior test-step failed!…
                String updatedFailureMessage = failures.get(testCase) + "\n" + buf.toString();
                failures.put(testCase, updatedFailureMessage);
            } else {
                pendingSuccess.put(testCase, buf.toString());
            }
        }
    }
    
    // TODO try to add cdata around failures in the generated xml
//    @Override
//    public void afterRun(TestCaseRunner testRunner, TestCaseRunContext runContext) {
//        TestCase testCase = testRunner.getTestCase();
//        JUnitReport report = reports.get(testCase.getTestSuite().getName());
//
//        if (Status.INITIALIZED != testRunner.getStatus() && Status.RUNNING != testRunner.getStatus()) {
//            if (Status.CANCELED == testRunner.getStatus()) {
//                report.addTestCase(testCase.getName(), testRunner.getTimeTaken());
//            }
//            if (Status.FAILED == testRunner.getStatus()) {
//                String msg = "";
//                if (failures.containsKey(testCase)) {
//                    msg = failures.get(testCase).toString();
//                    // customization
//                    msg = "<![CDATA[" + msg + "]]>";
//                }
//                report.addTestCaseWithFailure(testCase.getName(), testRunner.getTimeTaken(), testRunner.getReason(),
//                        msg);
//            }
//            if (Status.FINISHED == testRunner.getStatus()) {
//                report.addTestCase(testCase.getName(), testRunner.getTimeTaken());
//            }
//
//        }
//    }
    
    private static void appendTestStepStatus(StringBuffer buf, TestStep step, TestStepResult result) {
        // TODO check if we need xml entities as we are in a CDATA block
//      buf.append("<h3><b>").append(XmlUtils.entitize(step.getName())).append(" Failed</b></h3><pre>");
        buf.append("Test Step: ").append(step.getName());
        buf.append(" / Status: ").append(result.getStatus());
        appendBreakLine(buf);
    }
    
    private static void appendTestAssertionsOfTestStepIfAvailable(StringBuffer buf, TestStep step) {
        if (step instanceof Assertable) {
            log.error("##### step is assertable");
            Assertable requestStep = (Assertable) step;
            if (requestStep.getAssertionCount() > 0) {
                buf.append("Assertion details:").append(EOL);
                List<TestAssertion> assertions = requestStep.getAssertionList();
                for (TestAssertion assertion : assertions) {
                    buf.append("  Assertion: ").append(assertion.getName());
                    buf.append(" / Status: ").append(assertion.getStatus());
                    appendBreakLine(buf);
                }
            }
        }
    }
    
    private static void appendBreakLine(StringBuffer buf) {
        buf.append(EOL);
    }
    
}
