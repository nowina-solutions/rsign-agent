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
package lu.nowina.rsign.flow;

import java.io.File;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileReadingMessageSource.WatchEventType;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;

import lu.nowina.rsign.filter.RetryFilter;
import lu.nowina.rsign.filter.UploadableFilter;
import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.processor.FileToRSignRequest;
import lu.nowina.rsign.processor.GetMetadata;
import lu.nowina.rsign.processor.StatusWriter;
import lu.nowina.rsign.processor.UploadMessage;

@Configuration
class FilePollingIntegrationFlow {

	private static final String PROCESSING_FILE_CHANNEL = "fileChannel";

	@Autowired
	public File inboundReadDirectory;

	@Value("${inbound.filename.regex}")
	String regex;

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature3() {
		return IntegrationFlows.from(this.inputFileSource(), c -> c.poller(Pollers.fixedRate(1000)))
				.channel(PROCESSING_FILE_CHANNEL).get();
	}

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature2() {
		return IntegrationFlows.from(this.retryFileSource(), c -> c.poller(Pollers.fixedRate(1000)))
				.channel(PROCESSING_FILE_CHANNEL).get();
	}

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature() {
		return IntegrationFlows.from(PROCESSING_FILE_CHANNEL).transform(new FileToRSignRequest())
				.transform(getMetadata()).transform(uploadFiles()).transform(uploadedWriter()).log().get();
	}

	@Bean
	public StatusWriter uploadedWriter() {
		return new StatusWriter(StatusFile.UPLOADED);
	}

	@Bean
	public MessageSource<File> inputFileSource() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(this.inboundReadDirectory);

		CompositeFileListFilter<File> filter = new CompositeFileListFilter<File>(
				Arrays.asList(new RegexPatternFileListFilter(regex), uploadableFilter()));
		source.setFilter(filter);

		source.setUseWatchService(false);
		source.setAutoCreateDirectory(false);
		return source;
	}

	@Bean
	public UploadableFilter uploadableFilter() {
		return new UploadableFilter();
	}

	@Bean
	public MessageSource<File> retryFileSource() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(this.inboundReadDirectory);

		source.setWatchEvents(WatchEventType.DELETE);

		String pattern = regex + ".(" + StatusFile.ERROR.name() + "|" + StatusFile.EXPIRED.name() + ")";

		CompositeFileListFilter<File> filter = new CompositeFileListFilter<File>(
				Arrays.asList(retryFilter(), new RegexPatternFileListFilter(pattern)));
		source.setFilter(filter);

		source.setUseWatchService(true);
		source.setAutoCreateDirectory(false);
		return source;
	}

	@Bean
	public RetryFilter retryFilter() {
		return new RetryFilter();
	}

	@Bean
	public GetMetadata getMetadata() {
		return new GetMetadata();
	}

	@Bean
	public UploadMessage uploadFiles() {
		return new UploadMessage();
	}

}