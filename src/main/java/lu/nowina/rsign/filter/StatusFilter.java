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
package lu.nowina.rsign.filter;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.filters.AbstractFileListFilter;

import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.service.FileNamingStrategy;

abstract class StatusFilter extends AbstractFileListFilter<File> {

	@Autowired
	private FileNamingStrategy fileNamingStrategy;

	@Override
	public boolean accept(File file) {

		boolean process = processPath(file);
		if (!process) {
			return false;
		}

		Optional<File> s = fileNamingStrategy.getStatusCandidates(file);
		if (!s.isPresent()) {
			return onNoStatus();
		}

		Optional<StatusFile> status = fileNamingStrategy.getStatus(s.get());
		return onStatus(status.get());
	}

	protected boolean processPath(File file) {
		return true;
	}

	protected boolean onNoStatus() {
		return true;
	}

	abstract boolean onStatus(StatusFile status);

}
