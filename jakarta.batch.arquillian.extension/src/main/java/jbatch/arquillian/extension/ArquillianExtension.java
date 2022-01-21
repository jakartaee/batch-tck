package jbatch.arquillian.extension;

import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder;

public class ArquillianExtension implements LoadableExtension {

	public static final String PROPERTY_PREFIX = "arquillian.extensions.jakarta.batch.";
	
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeploymentScenarioGenerator.class, MavenTestDependenciesDeploymentPackager.class);
    }
}
