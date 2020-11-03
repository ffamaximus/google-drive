package co.com.alianza.drive.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix="co.com.alianza.drive")
public class ParametrosConfig {
	private Resource credencialesGoogle;

	public Resource getCredencialesGoogle() {
		return credencialesGoogle;
	}

	public void setCredencialesGoogle(Resource credencialesGoogle) {
		this.credencialesGoogle = credencialesGoogle;
	}
}
