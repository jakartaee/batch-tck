/*
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
package jbatch.arquillian.extension;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.*;

public class MavenTestDependenciesDeploymentPackager implements DeploymentScenarioGenerator {

    // Artifacts with a group matching one of these prefixes will not be added to the package. 
    // Expects a comma-separated list of prefixes.
    public static final String PROPERTY_KEY_GROUP_PREFIXES_TO_IGNORE = ArquillianExtension.PROPERTY_PREFIX + "groupPrefixesToIgnore";

    // package type, e.g. EAR, EJBJAR, WAR. Default is WAR
    public static final String PROPERTY_KEY_PACKAGE = ArquillianExtension.PROPERTY_PREFIX + "package";

    private List<String> groupPrefixesToIgnore = null;

    private DeploymentPackage deploymentPackage = DeploymentPackage.WAR;

    public MavenTestDependenciesDeploymentPackager() {
        initListOfIgnoredArtifactPrefixes();
        initDeploymentPackage();
    }

    public enum DeploymentPackage {
        WAR {
            @Override
            protected Archive<?> createDeploymentArchive(Stream<File> streamArtifactsToAdd) {
                WebArchive archive = ShrinkWrap
                        .create(WebArchive.class, "jbatch-test-package-all.war")
                        .as(WebArchive.class);
                streamArtifactsToAdd.forEach(archive::addAsLibrary);
                return archive;
            }
        }, EJBJAR {
            @Override
            protected Archive<?> createDeploymentArchive(Stream<File> streamArtifactsToAdd) {
                return createEarDeploymentArchive(streamArtifactsToAdd);
            }

        }, EAR {
            @Override
            protected Archive<?> createDeploymentArchive(Stream<File> streamArtifactsToAdd) {
                return createEarDeploymentArchive(streamArtifactsToAdd);
            }

        };

        public static DeploymentPackage fromString(String value) {
            if (value == null || value.isEmpty()) {
                return null;
            }
            String upperCaseValue = value.toUpperCase();
            // remove special chars, e.g. ejb-jar is turned into EJBJAR
            upperCaseValue = upperCaseValue.replaceAll("-|_|\\.| ", "");
            return DeploymentPackage.valueOf(upperCaseValue);
        }

        protected abstract Archive<?> createDeploymentArchive(Stream<File> streamArtifactsToAdd);

        protected Archive<?> createEarDeploymentArchive(Stream<File> streamArtifactsToAdd) {
            throw new RuntimeException("TODO - Not implemented yet!");
        }
    }

    public List<String> getGroupPrefixesToIgnore() {
        return groupPrefixesToIgnore;
    }

    public void setGroupPrefixesToIgnore(List<String> groupPrefixesToIgnore) {
        this.groupPrefixesToIgnore = groupPrefixesToIgnore;
    }

    public DeploymentPackage getDeploymentPackage() {
        return deploymentPackage;
    }

    public void setDeploymentPackage(DeploymentPackage deploymentPackage) {
        this.deploymentPackage = deploymentPackage;
    }

    private Archive<?> generateDeployment() {
        MavenResolvedArtifact[] resolvedArtifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.TEST)
                .resolve().withTransitivity().asResolvedArtifact();

        final Stream<File> streamArtifactsToAdd = Stream.of(resolvedArtifacts)
                .filter(this::artifactNotToIgnore)
                .filter(artifact -> {
                    return "jar".equals(artifact.getExtension());
                }).map(MavenResolvedArtifact::asFile);

        Archive<?> archive = deploymentPackage.createDeploymentArchive(streamArtifactsToAdd);
        return archive;
    }

    private boolean artifactNotToIgnore(MavenResolvedArtifact artifact) {
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
                "org.jboss.shrinkwrap",
                "arquillian.extension",
                "org.codehaus.plexus",
                "org.apache.maven"));
        String additionalPrefixesFromProperties = System.getProperty(PROPERTY_KEY_GROUP_PREFIXES_TO_IGNORE);
        if (additionalPrefixesFromProperties != null) {
            List<String> prefixesFromPropertiesList = Arrays.asList(additionalPrefixesFromProperties.split("\\s*,\\s*"));
            groupPrefixesToIgnore.addAll(prefixesFromPropertiesList);
        }
    }

    private void initDeploymentPackage() {
        String packageValue = System.getProperty(PROPERTY_KEY_PACKAGE);
        if (packageValue != null) {
            deploymentPackage = DeploymentPackage.fromString(packageValue);
        }
    }

}
