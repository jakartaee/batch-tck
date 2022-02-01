/*
 * Copyright (c) 2021-2022 Contributors to the Eclipse Foundation
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
package ee.jakarta.tck.batch.arquillian;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.*;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.*;

public class MavenTestDependenciesDeploymentPackager implements DeploymentScenarioGenerator {

    public static final String PROPERTY_KEY_INCLUDE_JOBOP_APPBEAN = ArquillianExtension.PROPERTY_PREFIX + "appbean";

    // Artifacts with a group matching one of these prefixes will not be added to the package. 
    // Expects a comma-separated list of prefixes.
    public static final String PROPERTY_KEY_GROUP_PREFIXES_TO_IGNORE = ArquillianExtension.PROPERTY_PREFIX + "groupPrefixesToIgnore";

    // package type, e.g. EAR, EJBJAR, WAR. Default is WAR
    public static final String PROPERTY_KEY_PACKAGE = ArquillianExtension.PROPERTY_PREFIX + "package";

    private List<String> groupPrefixesToIgnore = null;

    boolean includeAppBean = false;

    private DeploymentPackageType deploymentPackageType = DeploymentPackageType.WAR;

    public MavenTestDependenciesDeploymentPackager() {
        initListOfIgnoredArtifactPrefixes();
        initDeploymentPackage();
        includeAppBean = Boolean.getBoolean(PROPERTY_KEY_INCLUDE_JOBOP_APPBEAN);
    }


    public List<String> getGroupPrefixesToIgnore() {
        return groupPrefixesToIgnore;
    }

    public void setGroupPrefixesToIgnore(List<String> groupPrefixesToIgnore) {
        this.groupPrefixesToIgnore = groupPrefixesToIgnore;
    }

    public DeploymentPackageType getDeploymentPackageType() {
        return deploymentPackageType;
    }

    public void setDeploymentPackageType(DeploymentPackageType deploymentPackageType) {
        this.deploymentPackageType = deploymentPackageType;
    }

    private Archive<?> generateDeployment() {
        /**
         * As coded, doesn't use profile to resolve even if profile is active in
         * top-level execution.
         *
         * During staging, we need to resolve against artifacts not published to
         * Maven Central, so this next line would need to look something like:
         *
         * Maven.resolver().loadPomFromFile("pom.xml", "staging") ...
         *
         * See javadoc:
         * https://repository.jboss.org/nexus/content/repositories/unzip/org/jboss/shrinkwrap/resolver/shrinkwrap-resolver-api-maven/3.1.4/shrinkwrap-resolver-api-maven-3.1.4-javadoc.jar-unzip/index.html
         */
        MavenResolvedArtifact[] resolvedArtifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.TEST)
                .resolve().withTransitivity().asResolvedArtifact();

        DeploymentPackageType.PackageBuilder packageBuilder = deploymentPackageType.getPackageBuilder();

        Stream.of(resolvedArtifacts)
                .filter(this::artifactShouldntBeIgnored)
                .filter(this::notAppBeanArtifactToIgnore)
                .filter(artifact -> {
                    return "jar".equals(artifact.getExtension());
                })
                .map(MavenResolvedArtifact::asFile)
                .forEach(packageBuilder::addArtifact);

        return packageBuilder.build();
    }

    private boolean notAppBeanArtifactToIgnore(MavenResolvedArtifact artifact) {
        if (!includeAppBean) {
            boolean isAppBeanArtifact = "com.ibm.jbatch.tck.appbean".equals(artifact.getCoordinate().getArtifactId());
            return !isAppBeanArtifact;
        } else {
            return true;
        }
    }

    private boolean artifactShouldntBeIgnored(MavenResolvedArtifact artifact) {
        String groupId = artifact.getCoordinate().getGroupId();
        final boolean groupMatchesAPrefix = groupPrefixesToIgnore.stream()
                .anyMatch(prefix -> groupId.startsWith(prefix));
        return !groupMatchesAPrefix;
    }

    @Override
    public List<DeploymentDescription> generate(TestClass testClass) {
        final DeploymentDescription defaultDeployment = new DeploymentDescription("jbatch-test-package-all",
                generateDeployment());
        return Collections.singletonList(defaultDeployment);
    }

    private void initListOfIgnoredArtifactPrefixes() {
        groupPrefixesToIgnore = new ArrayList<>(Arrays.asList(
                "org.jboss.shrinkwrap", // ShrinkWrap - creates a deployment, not needed in the deployment itself
                "jbatch.arquillian.extension", // This extension - not needed in the deployment itself
                "org.codehaus.plexus",  // Maven classes - not needed in the deployment itself
                "org.apache.maven"));   // Maven classes - not needed in the deployment itself
        String additionalPrefixesFromProperties = System.getProperty(PROPERTY_KEY_GROUP_PREFIXES_TO_IGNORE);
        if (additionalPrefixesFromProperties != null) {
            List<String> prefixesFromPropertiesList = Arrays.asList(additionalPrefixesFromProperties.split("\\s*,\\s*"));
            groupPrefixesToIgnore.addAll(prefixesFromPropertiesList);
        }
    }

    private void initDeploymentPackage() {
        String packageValue = System.getProperty(PROPERTY_KEY_PACKAGE);
        if (packageValue != null) {
            deploymentPackageType = DeploymentPackageType.fromString(packageValue);
        }
    }

}
