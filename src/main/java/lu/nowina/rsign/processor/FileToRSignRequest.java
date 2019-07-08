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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

import lu.nowina.rsign.model.RSignRequest;

public class FileToRSignRequest implements GenericTransformer<Message<File>, Message<RSignRequest>> {

	private static final Logger logger = LoggerFactory.getLogger(FileToRSignRequest.class);

	@Override
	public Message<RSignRequest> transform(Message<File> source) {
		logger.debug("Processing file " + source.getPayload());
		RSignRequest payload = new RSignRequest();
		payload.setInputFile(source.getPayload());
		return MessageBuilder.withPayload(payload).build();
	}

}
