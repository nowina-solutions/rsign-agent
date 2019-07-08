/**
 * © Nowina Solutions, 2019
 *
 * Concédée sous licence EUPL, version 1.1 ou – dès leur approbation par la Commission européenne - versions ultérieures de l’EUPL (la «Licence»).
 * Vous ne pouvez utiliser la présente œuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous la Licence est distribué «en l’état»,
 * SANS GARANTIES OU CONDITIONS QUELLES QU’ELLES SOIENT, expresses ou implicites.
 * Consultez la Licence pour les autorisations et les restrictions linguistiques spécifiques relevant de la Licence.
 */
package lu.nowina.rsign;

import java.io.File;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;
import org.springframework.util.StreamUtils;

import lu.nowina.rsign.client.RSignClient;
import lu.nowina.rsign.service.FileNamingStrategy;
import lu.nowina.rsign.service.PropertiesFileUserInfoRetriever;
import lu.nowina.rsign.service.SQLUserInfoRetriever;
import lu.nowina.rsign.service.SimpleFileNamingStrategy;
import lu.nowina.rsign.service.UserInfoRetriever;

@SpringBootApplication
public class AgentApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder().web(WebApplicationType.NONE).sources(AgentApplication.class).run(args);
	}

	@Bean
	public RSignClient rSignClient(@Value("${api.baseUrl}") String baseUrl, @Value("${api.domain}") String domain,
			@Value("${api.username}") String username, @Value("${api.privateKey}") Resource privateKey)
			throws Exception {

		final byte[] privateKeyBytes = Base64.getMimeDecoder()
				.decode(StreamUtils.copyToByteArray(privateKey.getInputStream()));

		final RSAPrivateKey pk = (RSAPrivateKey) KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

		RSignClient client = new RSignClient(baseUrl, domain, username, pk);
		return client;
	}

	@Bean(name = "inboundReadDirectory")
	public File inboundReadDirectory(@Value("${agent.directory.in}") String path) {
		return new File(path);
	}

	/**
	 * Instantiate the requested UserInfoRetriever
	 * 
	 * @param userInfoStorage
	 * @return
	 */
	@Bean
	public UserInfoRetriever userInfoRetriever(@Value("${agent.user_info}") String userInfoStorage) {
		switch (userInfoStorage) {
		case "PROPERTIES":
			return new PropertiesFileUserInfoRetriever();
		case "SQL":
			return new SQLUserInfoRetriever();
		default:
			throw new IllegalArgumentException("No storage " + userInfoStorage);
		}
	}

	/**
	 * Required when agent.user_info = PROPERTIES. A dummy DataSource is created so
	 * Spring Boot can start "normally" (to prevent having to recreate all the
	 * others elements explicitly when agent.user_info = SQL.
	 * 
	 * @return
	 */
	@Bean
	@ConditionalOnProperty(name = "agent.user_info", havingValue = "PROPERTIES", matchIfMissing = true)
	public DataSource dummyDataSource() {
		return new AbstractDriverBasedDataSource() {

			@Override
			protected Connection getConnectionFromDriver(Properties props) throws SQLException {
				throw new IllegalStateException("For SQL access, set property agent.user_info = SQL");
			}
		};
	}

	@Bean
	public FileNamingStrategy fileNamingStrategy() {
		return new SimpleFileNamingStrategy();
	}

}
