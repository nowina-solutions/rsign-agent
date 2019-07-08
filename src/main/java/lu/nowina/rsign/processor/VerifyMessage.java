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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.rmi.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import lu.nowina.rsign.client.RSignClient;
import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.service.FileNamingStrategy;

public class VerifyMessage extends AgentMessageProcessor {

	private static final Logger logger = LoggerFactory.getLogger(VerifyMessage.class);

	@Autowired
	private FileNamingStrategy fileNamingStrategy;

	@Autowired
	private RSignClient client;

	@Override
	public RSignRequest process(RSignRequest payload) throws Exception {

		try {
			logger.debug("Verifing file " + payload);

			File uploaded = fileNamingStrategy.getStatusFileForFile(payload.getInputFile(), StatusFile.UPLOADED);

			try (FileInputStream in = new FileInputStream(uploaded)) {

				String rsignExternalId = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
				logger.debug("Verify process " + rsignExternalId);

				client.getDocumentGroup(rsignExternalId).ifPresent(group -> {

					logger.debug("Process is " + group.getStatus());
					if ("FINISHED".equals(group.getStatus())) {

						client.getDocumentContent(group.getDocuments().get(0).getExternalId()).ifPresent(doc -> {

							fileNamingStrategy.getSignedFileForFile(payload.getInputFile()).ifPresent(signedFile -> {

								logger.info("Write signed file to " + signedFile);
								try (OutputStream out = new FileOutputStream(signedFile)) {
									out.write(doc);
								} catch (IOException e) {
									logger.error("Cannot write file [" + signedFile + "] : " + e.getMessage());
								}

								payload.setSigned(true);

							});

						});

					}

				});

			}

			return payload;
			
		} catch (UnknownHostException e) {
			/* It may happens that we are disconnected from internet */
			logger.warn("Cannot reach host " + e.getMessage());
			return payload;
		} catch (Exception e) {
			logger.error("Error", e);
			return payload;
		}
	}

}
