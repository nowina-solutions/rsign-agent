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
package lu.nowina.rsign.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import lu.nowina.rsign.client.UserInfo;

public class PropertiesFileUserInfoRetriever implements UserInfoRetriever {

	private static final String PHONE = "phone";
	private static final String LASTNAME = "lastname";
	private static final String FIRSTNAME = "firstname";
	private static final String EMAIL = "email";
	private static final String PROPERTIES_FILE_NAME = "rsign.properties";

	@Override
	public UserInfo getUserInfo(File file) throws RuntimeException {

		File directory = file.getParentFile();

		return getProperties(directory).map((props) -> {

			UserInfo info = new UserInfo();
			info.setEmail(props.getProperty(EMAIL));
			info.setFirstName(props.getProperty(FIRSTNAME));
			info.setLastName(props.getProperty(LASTNAME));
			info.setPhoneNumber(props.getProperty(PHONE));
			return info;

		}).orElseThrow(() -> {
			return new RuntimeException("Cannot find properties for directory " + directory);
		});

	}

	public Optional<File> getFolderPropertiesFile(File directory) {

		File[] candidates = containsPropertiesFile(directory);
		if (candidates.length > 0) {
			return Optional.of(candidates[0]);
		}

		File parent = directory.getParentFile();
		if (parent == null) {
			return Optional.empty();
		}

		return getFolderPropertiesFile(parent);
	}

	public Optional<Properties> getProperties(File directory) {

		return getFolderPropertiesFile(directory).map((file) -> {
			Properties properties = new Properties();

			try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
				properties.load(in);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return properties;
		});

	}

	File[] containsPropertiesFile(File dir) {
		return dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory() && PROPERTIES_FILE_NAME.equalsIgnoreCase(pathname.getName());
			}
		});
	}

}
