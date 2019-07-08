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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.filters.ResettableFileListFilter;

import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.service.FileNamingStrategy;

/**
 * This uses the remove feature of ResettableFileListFilter to detect the retry
 * of a file (by deleting the .ERROR status file)
 * 
 * @author david
 *
 */
public class RetryFilter implements ResettableFileListFilter<File> {

	private static final Logger logger = LoggerFactory.getLogger(RetryFilter.class);

	@Autowired
	private FileNamingStrategy fileNamingStrategy;

	private List<File> list = new ArrayList<File>();

	@Override
	public List<File> filterFiles(File[] files) {
		List<File> results = new ArrayList<File>();
		synchronized (list) {
			results.addAll(list);
			list = new ArrayList<File>();
		}
		return results;
	}

	@Override
	public boolean remove(File f) {

		/* Search for ERROR files */
		Optional<StatusFile> status = fileNamingStrategy.getStatus(f).filter(s -> { return s == StatusFile.ERROR || s == StatusFile.EXPIRED; });
		
		if (status.isPresent()) {

			File input = fileNamingStrategy.getFileMatchStatusFile(f);

			logger.info("Status file was deleted [" + f + "] matching file [" + input + "] exists : " + input.exists());
			if (input.exists()) {
				synchronized (list) {
					list.add(input);
					return true;
				}
			}

		}

		return false;

	}

}
