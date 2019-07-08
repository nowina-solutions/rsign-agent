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

import org.springframework.beans.factory.annotation.Autowired;

import lu.nowina.rsign.client.UserInfo;
import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.service.UserInfoRetriever;

public class GetMetadata extends AgentMessageProcessor {

	@Autowired
	private UserInfoRetriever retriever;

	@Override
	public RSignRequest process(RSignRequest payload) throws Exception {

		UserInfo userInfo = retriever.getUserInfo(payload.getInputFile());
		payload.setUser(userInfo);

		return payload;
	}

}
