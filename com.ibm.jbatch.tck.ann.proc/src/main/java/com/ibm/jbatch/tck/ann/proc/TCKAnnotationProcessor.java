/*
 * Copyright 2016 International Business Machines Corp.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.jbatch.tck.ann.proc;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.ibm.jbatch.tck.ann.*;

@SupportedAnnotationTypes("com.ibm.jbatch.tck.ann.*")
public class TCKAnnotationProcessor extends AbstractProcessor {

    /**
     * Each key in the coverageReport represents a section of the spec with tests written for it.
     * The corresponding value is a list of all the tests that reference that section.
     */
    private TreeMap<String, ArrayList<Element>> coverageReport = new TreeMap<String, ArrayList<Element>>(SectionComparator);

    /**
     * All annotated test methods that do not reference a specific section of the spec should be
     * grouped together at the bottom of the coverage report under this name.
     */
    private static final String NO_SPEC_SECTION_SPECIFIED = "No Section Specified";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotationTypes, RoundEnvironment roundEnvironment) {

        /* Note that an Element can refer to a class, method, or field, etc.
         * However, we will refer to this Set of Elements as "testMethods"
         * because the declaration for TCKTest has an @Target of ElementType.METHOD only */
        //The set of methods annotated with @TCKTest
        Set<? extends Element> testMethods = roundEnvironment.getElementsAnnotatedWith(TCKTest.class);
        for (Element testMethod : testMethods) {
            processTestMethod(testMethod);
        }

        if (roundEnvironment.processingOver()) {
            createCoverageReport();
        }

        return false;
    }

    private void processTestMethod(Element testMethod) {
        addTestToList(testMethod);
        createReportForTest(testMethod);
    }

    private void addTestToList(Element testMethod) {
        TCKTest tckAnnotation = testMethod.getAnnotation(TCKTest.class);
        SpecRef[] specRefs = tckAnnotation.specRefs();

        for (SpecRef specRef : specRefs) {
            String sectionName = NO_SPEC_SECTION_SPECIFIED;
            if (specRef.section().trim().length() > 0) {
                sectionName = specRef.section();
            }
            addTestToSection(testMethod, sectionName);
        }

    }

    private void addTestToSection(Element testMethod, String sectionName) {
        //If there aren't any tests yet for the specified section
        if (!coverageReport.containsKey(sectionName)) {
            ArrayList<Element> testMethods = new ArrayList<Element>();
            testMethods.add(testMethod);
            coverageReport.put(sectionName, testMethods);
        }
        //Otherwise, tests for the specified section have already been processed
        else {
            coverageReport.get(sectionName).add(testMethod);
        }
    }

    private void createReportForTest(Element testMethod) {
        try {
            TCKTest annotation = testMethod.getAnnotation(TCKTest.class);

            String packageName = processingEnv.getElementUtils().getPackageOf(testMethod).toString();
            String className = testMethod.getEnclosingElement().getSimpleName().toString();
            String methodName = testMethod.getSimpleName().toString();

            String dirName = "TCK_Coverage_Report.Tests." + className;
            String fileName = methodName + ".html";
            FileObject testCoverageReportFile = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, dirName, fileName);
            Writer writer = testCoverageReportFile.openWriter();

            //Begin writing HTML
            writeHTMLHeadAndCSS("TCK Coverage Report - " + methodName, writer);

            //Section for Package and Class Name of Test
            writeBeginSection(writer);
            writePackageAndClassTable(writer, packageName, className);
            writeEndSection(writer);

            //Section for Name of Test and when it was added/ updated to TCK
            writeBeginSectionWithTitle(writer, methodName, 1);
            writeTCKVersionInfo(writer, annotation);
            writeEndSection(writer);

            //Section for Assertions
            writeBeginSectionWithTitle(writer, "Test Assertion(s)", 2);
            writeOneColumnTableFromArray(writer, "Assertion(s)", annotation.assertions());
            writeEndSection(writer);

            //Section for Spec References
            writeBeginSectionWithTitle(writer, "Jakarta Batch Reference(s)", 2);
            writeSpecRefTable(writer, annotation);
            writeEndSection(writer);

            //Section for API References
            writeBeginSectionWithTitle(writer, "API Reference(s)", 2);
            writeAPIRefTable(writer, annotation);
            writeEndSection(writer);

            //Section for Issue References
            writeBeginSectionWithTitle(writer, "Issue Reference(s)", 2);
            writeIssueRefTable(writer, annotation);
            writeEndSection(writer);

            //Section for Test Strategy
            writeBeginSectionWithTitle(writer, "Test Strategy", 2);
            writeOneColumnTableFromString(writer, "Strategy", annotation.strategy());
            writeEndSection(writer);

            //Section for Additional Comments
            writeBeginSectionWithTitle(writer, "Additional Comments", 2);
            writeOneColumnTableFromArray(writer, "Comment(s)", annotation.notes());
            writeEndSection(writer);

            //End writing HTML
            writeHTMLFooter(writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createCoverageReport() {
        try {
            FileObject testCoverageReportFile = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "TCK_Coverage_Report", "Coverage_Report.html");
            Writer writer = testCoverageReportFile.openWriter();

            //Begin writing HTML
            writeHTMLHeadAndCSS("TCK Coverage Report", writer);

            //Section for TCK Coverage Report page title
            writeBeginSectionWithTitle(writer, "TCK Coverage Report", 1);
            writeEndSection(writer);

            //(HTML) Section for each specSection with test coverage
            for (String specSection : coverageReport.keySet()) {
                writeBeginSectionWithTitle(writer, specSection, 2);
                for (Element testMethod : coverageReport.get(specSection)) {
                    writeLinkForIndividualTestReport(writer, testMethod);
                }
                writeEndSection(writer);
            }

            //End writing HTML
            writeHTMLFooter(writer);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHTMLHeadAndCSS(String title, Writer writer) throws IOException {
        writer.write(
                "<!DOCTYPE html>"
                        + "\n<html>"
                        + "\n<head>"
                        + "\n	<title> " + title + " </title>"
                        + "\n	<style>"
                        + "\n		*{"
                        + "\n			background: White;"
                        + "\n			padding: 5px;"
                        + "\n			margin: 0px;"
                        + "\n			box-sizing:border-box;"
                        + "\n		}"
                        + "\n		html{"
                        + "\n			background: MidnightBlue;"
                        + "\n			padding: 5px 0px;"
                        + "\n		}"
                        + "\n		body{"
                        + "\n			max-width: 1000px;"
                        + "\n			width: 100%;"
                        + "\n			min-width: 750px;"
                        + "\n			padding: 5px 10px;"
                        + "\n			margin: 0px auto;"
                        + "\n			border: 1px solid black;"
                        + "\n		}"
                        + "\n		section{"
                        + "\n			padding: 0px 0px 20px;"
                        + "\n		}"
                        + "\n		h1{"
                        + "\n			font-size: 40px;"
                        + "\n		}"
                        + "\n		table{"
                        + "\n			padding: 0px;"
                        + "\n			border-spacing: 0px;"
                        + "\n			border-collapse: collapse;"
                        + "\n		}"
                        + "\n		td,th{"
                        + "\n			border: 1px solid black;"
                        + "\n			vertical-align: top;"
                        + "\n		}"
                        + "\n		th{"
                        + "\n			background: LightGray;"
                        + "\n		}"
                        + "\n		p{"
                        + "\n			padding: 0px 20px 5px;"
                        + "\n		}"
                        + "\n		a{"
                        + "\n			color: Blue;"
                        + "\n			text-decoration: none;"
                        + "\n		}"
                        + "\n		a:hover{"
                        + "\n			text-decoration: underline;"
                        + "\n		}"
                        + "\n		#dotted{"
                        + "\n			background: inherit;"
                        + "\n			border-bottom: 2px dotted black;"
                        + "\n			padding: 0px;"
                        + "\n		}"
                        + "\n		#dotted:hover{"
                        + "\n			cursor: pointer;"
                        + "\n		}"
                        + "\n	</style>"
                        + "\n</head>"
                        + "\n<body>\n"
        );
    }

    private void writeBeginSection(Writer writer) throws IOException {
        writer.write("\n<section>");
    }

    private void writeBeginSectionWithTitle(Writer writer, String sectionTitle, int headerLevel) throws IOException {
        writeBeginSection(writer);
        if ((headerLevel < 1) || (headerLevel > 6)) {
            headerLevel = 1;
        }
        writer.write("\n	<h" + headerLevel + "> " + sectionTitle + " </h" + headerLevel + ">");
    }

    private void writeEndSection(Writer writer) throws IOException {
        writer.write("\n</section>\n");
    }

    private void writeOneColumnTableFromString(Writer writer, String tableHeader, String s) throws IOException {
        String[] arrayWithOneString = {s};
        writeOneColumnTableFromArray(writer, tableHeader, arrayWithOneString);
    }

    private void writeOneColumnTableFromArray(Writer writer, String tableHeader, String[] array) throws IOException {
        writer.write(
                "\n	<table>"
                        + "\n		<tr>"
                        + "\n			<th> " + tableHeader + " </th>"
                        + "\n		</tr>"
        );
        for (String s : array) {
            writer.write(
                    "\n		<tr>"
                            + "\n			<td> " + s + " </td>"
                            + "\n		</tr>"
            );
        }
        writer.write(
                "\n	</table>"
        );
    }

    private void writePackageAndClassTable(Writer writer, String packageName, String className) throws IOException {
        writer.write(
                "\n	<table>"
                        + "\n		<tr>"
                        + "\n			<td> Package: </td>"
                        + "\n			<td> " + packageName + " </td>"
                        + "\n		</tr>"
                        + "\n		<tr>"
                        + "\n			<td> Class: </td>"
                        + "\n			<td> " + className + " </td>"
                        + "\n		</tr>"
                        + "\n	</table>"
        );
    }

    private void writeTCKVersionInfo(Writer writer, TCKTest annotation) throws IOException {
        for (int i = 0; i < annotation.versions().length; i++) {
            if (i == 0) {
                writer.write("\n	<p> Test introduced in TCK Version: " + annotation.versions()[i]);
            } else if (i == 1) {
                writer.write("\n	<p> Test updated in TCK Version(s): " + annotation.versions()[1]);
            } else {
                writer.write(", " + annotation.versions()[i]);
            }
        }
    }

    private void writeSpecRefTable(Writer writer, TCKTest annotation) throws IOException {
        writer.write(
                "\n 	<table>"
                        + "\n		<tr>"
                        + "\n			<th> Section </th>"
                        + "\n			<th title='Version of the spec where the tested behavior was first introduced or most recently clarified.'><span id='dotted'> Version </span></th>"
                        + "\n			<th> Citation(s) </th>"
                        + "\n			<th> Note(s) </th>"
                        + "\n		</tr>"
        );
        for (SpecRef specRef : annotation.specRefs()) {
            writer.write(
                    "\n 		<tr>"
                            + "\n			<td> " + getString(specRef.section()) + " </td>"
                            + "\n			<td> " + getString(specRef.version()) + " </td>"
                            + "\n			<td> " + getArrayAsString(specRef.citations()) + " </td>"
                            + "\n			<td> " + getArrayAsString(specRef.notes()) + " </td>"
                            + "\n		</tr>"
            );
        }
        writer.write("\n	</table>");
    }

    private void writeAPIRefTable(Writer writer, TCKTest annotation) throws IOException {
        writer.write(
                "\n 	<table>"
                        + "\n		<tr>"
                        + "\n			<th> Class Name </th>"
                        + "\n			<th> Method Names </th>"
                        + "\n			<th> Note(s) </th>"
                        + "\n		</tr>"
        );
        for (APIRef apiRef : annotation.apiRefs()) {
            writer.write(
                    "\n 		<tr>"
                            + "\n			<td> " + getString(apiRef.className()) + " </td>"
                            + "\n			<td> " + getArrayAsString(apiRef.methodNames()) + " </td>"
                            + "\n			<td> " + getArrayAsString(apiRef.notes()) + " </td>"
                            + "\n		</tr>"
            );
        }
        writer.write("\n	</table>");
    }

    private void writeIssueRefTable(Writer writer, TCKTest annotation) throws IOException {
        writer.write(
                "\n	<table>"
                        + "\n		<tr>"
                        + "\n			<th> Link(s) </th>"
                        + "\n		</tr>"
        );
        for (String issueRef : annotation.issueRefs()) {
            writer.write(
                    "\n		<tr>"
                            + "\n			<td><a href='" + issueRef + "' target='_blank'> " + issueRef + " </a></td>"
                            + "\n		</tr>"
            );
        }
        writer.write("\n	</table>");
    }

    private void writeLinkForIndividualTestReport(Writer writer, Element testMethod) throws IOException {
        String className = testMethod.getEnclosingElement().getSimpleName().toString();
        String methodName = testMethod.getSimpleName().toString();
        String link = "Tests/" + className + "/" + methodName + ".html";
        writer.write("\n	<p><a href='" + link + "' target='_blank'> " + methodName + " </a>");
    }

    private void writeHTMLFooter(Writer writer) throws IOException {
        writer.write(
                "\n</body>"
                        + "\n</html>"
        );
    }

    /**
     * Converts null strings and strings with only blanks spaces into an empty String, ""
     **/
    private String getString(String s) {
        String realString = "";
        if (s != null) {
            if (s.trim().length() > 0) {
                realString = s;
            }
        }
        return realString;
    }

    /**
     * Formats an array into a single String to be used in HTML
     */
    private String getArrayAsString(String[] array) {
        String arrayAsString = "";
        if (array != null) {
            for (String s : array) {
                if (s.trim().length() > 0) {
                    if (arrayAsString.equals("")) {
                        arrayAsString = "~ " + s;
                    } else {
                        arrayAsString += "<br>~ " + s;
                    }
                }
            }
        }
        return arrayAsString;
    }

    private static Comparator<String> SectionComparator = new Comparator<String>() {

        /*
         * The sorted order for a group of section Strings should look something like:
         * - 1.0
         * - 1.2
         * - 5.1
         * - 5.1.3
         * - 5.1.4
         * - No spec section specified
         *
         * Strategy for comparing two input Strings:
         * - if Strings are the same, return 0
         * - else:
         *   - split both Strings into an array of substrings (split at every ".")
         *     (examples: "10.4.8" --> {"10","4","8"} | "No section specified" --> {"No section specified"} )
         *   - compare corresponding substrings in the two arrays:
         *     - if subStrings are equal, move to the next substring
         *     - if both substrings can be parsed into Integers, then compare numerically (5<10)
         *     - else compare alphabetically ("10"<"No section specified")
         *   - if there are no more corresponding subStrings,
         *       then the String with fewer substrings comes first ("5.1.3"<"5.1.3.1")
         */

        @Override
        public int compare(String s1, String s2) {

            if (s1.equals(s2)) {
                return 0;
            }

            //Cannot trim() using "." as delimiter since it has a special meaning as a regular expression
            String[] a1 = s1.trim().split("\\.");
            String[] a2 = s2.trim().split("\\.");

            int maxIndex = a1.length;
            if (a2.length < maxIndex) {
                maxIndex = a2.length;
            }

            for (int i = 0; i < maxIndex; i++) {
                String ss1 = a1[i];
                String ss2 = a2[i];

                //Only compare the substrings if they are different
                if (!ss1.equals(ss2)) {
                    try {
                        //compare substrings numerically
                        int int1 = Integer.parseInt(ss1);
                        int int2 = Integer.parseInt(ss2);
                        if (int1 < int2) {
                            return -1;
                        } else if (int1 > int2) {
                            return 1;
                        }
                    } catch (NumberFormatException e) {
                        //do nothing
                    }

                    //compare substrings alphabetically
                    if (ss1.compareTo(ss2) < 0) {
                        return -1;
                    } else if (ss1.compareTo(ss2) > 0) {
                        return 1;
                    }
                }
            }

            if (a1.length < a2.length) {
                return -1;
            } else if (a1.length > a2.length) {
                return 1;
            }

            return 0;
            //I don't think it's possible to hit this case, but I was hesitant to put a blanket else statement earlier
            //in the method due to possible weird cases resulting from the trimming and splitting
        }
    };

}
