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

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lu.nowina.rsign.model.StatusFile;

public class SimpleFileNamingStrategy implements FileNamingStrategy {

	private static final Logger logger = LoggerFactory.getLogger(SimpleFileNamingStrategy.class);
	
	@Override
	public File getStatusFileForFile(File file, StatusFile status) {
		return new File(file.getParentFile(), file.getName() + "." + status.name());
	}

	@Override
	public Optional<File> getSignedFileForFile(File file) {

		if(file.isDirectory()) {
			return Optional.empty();
		}
		
		String fileName = file.getName();
		int lastIndexOfPoint = fileName.lastIndexOf(".");
		if(lastIndexOfPoint == -1) {
			logger.warn("Error searching for signed file matching " + file);
			return Optional.empty();
		}

		String fileNameWithoutExtension = fileName.substring(0, lastIndexOfPoint);
		return Optional.of(new File(file.getParentFile(), fileNameWithoutExtension + "-signed" + ".pdf"));
	}

	@Override
	public boolean isSignedFile(File file) {
		return file.getName().endsWith("-signed" + ".pdf");
	}

	@Override
	public Optional<StatusFile> getStatus(String statusText) {

		for (StatusFile s : StatusFile.values()) {
			if (s.name().equals(statusText)) {
				return Optional.of(s);
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<StatusFile> getStatus(File file) {

		String statusFileName = file.getName();
		int lastIndexOfPoint = statusFileName.lastIndexOf(".");
		String statusText = statusFileName.substring(lastIndexOfPoint + 1);
		return getStatus(statusText);
	}

	@Override
	public File getFileMatchStatusFile(File statusFile) {

		String statusFileName = statusFile.getName();
		int lastIndexOfPoint = statusFileName.lastIndexOf(".");
		String fileName = statusFileName.substring(0, lastIndexOfPoint);
		File file = new File(statusFile.getParentFile(), fileName);
		return file;

	}

	@Override
	public Optional<File> getStatusCandidates(File file) {
		File[] files = file.getParentFile().listFiles(new StatusFileNameFilter(file));
		if (files.length > 1) {
			throw new IllegalStateException("More than one status file for " + file);
		} else if (files.length == 0) {
			return Optional.empty();
		} else {
			return Optional.of(files[0]);
		}
	}

}
