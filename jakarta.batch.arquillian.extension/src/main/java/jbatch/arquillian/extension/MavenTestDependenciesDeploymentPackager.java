package jbatch.arquillian.extension;

import java.util.*;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.spi.client.deployment.*;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.*;

public class MavenTestDependenciesDeploymentPackager implements DeploymentScenarioGenerator {

    public static final String PROPERTY_KEY_INCLUDE_JOBOP_APPBEAN = ArquillianExtension.PROPERTY_PREFIX + "appbean";
    
    private Archive<?> generateDeployment() {

        Boolean includeAppBean = Boolean.getBoolean(PROPERTY_KEY_INCLUDE_JOBOP_APPBEAN);

        MavenResolvedArtifact[] resolvedArtifacts = Maven.resolver().loadPomFromFile("pom.xml")
                .importDependencies(ScopeType.COMPILE, ScopeType.TEST)
                .resolve().withTransitivity().asResolvedArtifact();
        
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "jbatch-test-package-all.war")
                .as(WebArchive.class);
        
        for (MavenResolvedArtifact artifact : resolvedArtifacts) {
            String groupId = artifact.getCoordinate().getGroupId();
            if (groupId.startsWith("org.jboss.shrinkwrap")
                    || groupId.startsWith("arquillian.extension")
                    || groupId.startsWith("org.codehaus.plexus")
                    || groupId.startsWith("org.apache.maven")) {
                continue;
            }
            
            if ("jar".equals(artifact.getExtension())) {
            	// Conditionally exclude the app-packaged JobOperator bean
            	boolean isAppBeanArtifact = "com.ibm.jbatch.tck.appbean".equals(artifact.getCoordinate().getArtifactId());
            	if (isAppBeanArtifact) {
            		if (includeAppBean) {
            		    archive = archive.addAsLibrary(artifact.asFile());
            		} 
            	} else {
            	    archive = archive.addAsLibrary(artifact.asFile());
            	}
            }
        }
        return archive;
    }

    public List<DeploymentDescription> generate(TestClass arg0) {
        return Arrays.asList(new DeploymentDescription("jbatch-test-package-all", generateDeployment()));
    }

}
