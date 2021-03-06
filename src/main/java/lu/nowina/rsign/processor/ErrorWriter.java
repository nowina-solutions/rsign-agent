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
import java.io.FileWriter;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.service.FileNamingStrategy;

public class ErrorWriter extends AbstractMessageHandler {

	@Autowired
	private FileNamingStrategy fileNamingStrategy;

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {

		MessagingException ex = (MessagingException) message.getPayload();

		RSignRequest payload = (RSignRequest) ex.getFailedMessage().getPayload();

		File newStatus = fileNamingStrategy.getStatusFileForFile(payload.getInputFile(), StatusFile.ERROR);

		fileNamingStrategy.getStatusCandidates(payload.getInputFile()).ifPresent(existing -> {
			logger.info("Renaming status " + existing + " to " + newStatus);
			existing.renameTo(newStatus);
		});

		logger.info("Writing content to status file " + newStatus);
		try (FileWriter writer = new FileWriter(newStatus); PrintWriter w = new PrintWriter(writer)) {
			ex.getCause().printStackTrace(w);
		}

	}

}
