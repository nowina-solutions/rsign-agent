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
package lu.nowina.rsign.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.service.FileNamingStrategy;

public class StatusWriter extends AgentMessageProcessor {

	private static final Logger logger = LoggerFactory.getLogger(StatusWriter.class);

	final StatusFile status;

	@Autowired
	private FileNamingStrategy fileNamingStrategy;

	public StatusWriter(StatusFile status) {
		this.status = status;
	}

	@Override
	RSignRequest process(RSignRequest message) throws Exception {

		File newStatus = fileNamingStrategy.getStatusFileForFile(message.getInputFile(), status);

		fileNamingStrategy.getStatusCandidates(message.getInputFile()).ifPresent(existing -> {
			logger.info("Renaming status " + existing + " to " + newStatus);
			existing.renameTo(newStatus);
		});

		String externalId = message.getExternalId();
		if (externalId != null) {
			logger.info("Writing content to status file " + newStatus);
			try (FileOutputStream out = new FileOutputStream(newStatus)) {
				out.write(externalId.getBytes(Charset.forName("UTF-8")));
			}
		} else {
			logger.info("Creating status file " + newStatus);
			newStatus.createNewFile();
		}

		return message;
	}

}
