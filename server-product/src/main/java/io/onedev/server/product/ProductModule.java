package io.onedev.server.product;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.AbstractPluginModule;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.jetty.ServerConfigurator;
import io.onedev.server.jetty.ServletConfigurator;
import io.onedev.server.persistence.HibernateConfig;
import io.onedev.server.util.ProjectNameReservation;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.hibernate.cfg.AvailableSettings.*;

public class ProductModule extends AbstractPluginModule {

	private static final String[] HIBERNATE_PROPS = new String[] {
			DIALECT, DRIVER, URL, USER, PASS, "hibernate.hikari.leakDetectionThreshold", 
			"hibernate.hikari.maxLifetime", "hibernate.hikari.connectionTimeout", 
			"hibernate.hikari.maximumPoolSize", "hibernate.hikari.validationTimeout",
			"hibernate.show_sql"
	};
	
    @Override
	protected void configure() {
		super.configure();
		
		File file = new File(Bootstrap.installDir, "conf/hibernate.properties"); 
		HibernateConfig hibernateConfig = new HibernateConfig(FileUtils.loadProperties(file));
		String url = hibernateConfig.getProperty(URL);
		hibernateConfig.setProperty(URL, 
				StringUtils.replace(url, "${installDir}", Bootstrap.installDir.getAbsolutePath()));
		
		for (String prop: HIBERNATE_PROPS) {
			String env = System.getenv(prop.replace('.', '_'));
			if (env != null)
				hibernateConfig.setProperty(prop, env);
		}
		
		bind(HibernateConfig.class).toInstance(hibernateConfig);
		
		file = new File(Bootstrap.installDir, "conf/server.properties");
		ServerProperties serverProps = new ServerProperties(FileUtils.loadProperties(file)); 
		bind(ServerProperties.class).toInstance(serverProps);
		
		bind(ServerConfig.class).to(DefaultServerConfig.class);

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
		
		contribute(ProjectNameReservation.class, () -> {
			Set<String> reserved = new HashSet<>();
			for (var file1 : OneDev.getAssetsDir().listFiles())
				reserved.add(file1.getName());
			return reserved;
		});
	}

}
