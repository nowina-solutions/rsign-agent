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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import lu.nowina.rsign.client.RSignClient;
import lu.nowina.rsign.client.SignatureInfo;
import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.pdf.NullWriter;
import lu.nowina.rsign.pdf.SignatureTagLocator;

public class UploadMessage extends AgentMessageProcessor {

	@Autowired
	private RSignClient client;

	@Override
	public RSignRequest process(RSignRequest payload) throws Exception {

		File file = payload.getInputFile();

		Optional<SignatureInfo> info = locateVisibleSignature(file);
		
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
			String uuid = client.createDocumentGroup(payload.getUser(), file.getName(),
					StreamUtils.copyToByteArray(in), info);
			payload.setExternalId(uuid);
			return payload;
		}
	}

	public Optional<SignatureInfo> locateVisibleSignature(File file)
			throws IOException, InvalidPasswordException, FileNotFoundException {
		SignatureTagLocator locator = new SignatureTagLocator("RSignSignature");
		
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
			PDDocument load = PDDocument.load(in);
			locator.writeText(load, new NullWriter());
			load.close();
		}

		if(!locator.isSignatureTagFound()) {
			return Optional.empty();
		} else {
			SignatureInfo si = new SignatureInfo();
			si.setPage(locator.getPageNo());
			si.setX(locator.getX());
			si.setY(locator.getY());
			si.setSignatureTextColor("0;0;0;255");
			si.setSignatureText("Signed with RSign");
			si.setSignatureTextFont("Arial");
			si.setSignatureTextBackColor("255;255;255;255");

			return Optional.of(si);
		}

	}

}
