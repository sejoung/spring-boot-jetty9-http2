/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.jetty9.ssl;

import javax.servlet.Filter;

import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NegotiatingServerConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlets.PushCacheFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class SampleJetty9SslApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SampleJetty9SslApplication.class, args);
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
		factory.addServerCustomizers(new JettyServerCustomizer() {
			@Override
			public void customize(Server server) {
				
				/*
				MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
				server.addBean(mbContainer);
				
				ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
				context.addFilter(PushSessionCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
				context.addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
				*/
				// HTTP Configuration
				HttpConfiguration http_config = new HttpConfiguration();
				http_config.setSecureScheme("https");
				http_config.setSecurePort(8443);
				http_config.setSendXPoweredBy(true);
				http_config.setSendServerVersion(true);

				// HTTP Connector
				ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config),
						new HTTP2CServerConnectionFactory(http_config));
				http.setPort(8080);
				// server.addConnector(http);

				// SSL Context Factory for HTTPS and HTTP/2
				SslContextFactory sslContextFactory = new SslContextFactory();
				sslContextFactory.setKeyStorePath("src/main/resources/sample.jks");
				sslContextFactory.setKeyStorePassword("secret");
				sslContextFactory.setKeyManagerPassword("password");
				sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

				// HTTPS Configuration
				HttpConfiguration https_config = new HttpConfiguration(http_config);
				https_config.addCustomizer(new SecureRequestCustomizer());

				// HTTP/2 Connection Factory
				HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(https_config);

				NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
				ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
				alpn.setDefaultProtocol(http.getDefaultProtocol());

				// SSL Connection Factory
				SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

				// HTTP/2 Connector
				ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2,
						new HttpConnectionFactory(https_config));
				http2Connector.setPort(8443);
				server.setConnectors(new Connector[] { http, http2Connector });

			}
		});
		return factory;
	}
	
	@Bean
    public FilterRegistrationBean someFilterRegistration() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(PushCacheFilter());
        registration.addUrlPatterns("/*");
        registration.addInitParameter("paramName", "paramValue");
        registration.setName("pushCacheFilter");
        return registration;
    }

    @Bean(name = "pushCacheFilter")
    public Filter PushCacheFilter() {
        return new PushCacheFilter();
    }
}
