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
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import lu.nowina.rsign.filter.MaybeSignedFilter;
import lu.nowina.rsign.model.RSignRequest;
import lu.nowina.rsign.model.StatusFile;
import lu.nowina.rsign.processor.FileToRSignRequest;
import lu.nowina.rsign.processor.StatusWriter;
import lu.nowina.rsign.processor.VerifyMessage;

@Configuration
class CheckUploadedFileFlow {

	@Value("${inbound.filename.regex}")
	String regex;

	@Autowired
	public File inboundReadDirectory;

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature4() {
		return IntegrationFlows.from(this.inputFileSource3(), c -> c.poller(Pollers.fixedRate(10000)))
				.transform(new FileToRSignRequest()).transform(verifyMessage()).route(router()).get();
	}

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature5() {
		return IntegrationFlows.from("fileIsSigned").transform(statusWriter()).log().get();
	}

	@Bean
	public StatusWriter statusWriter() {
		return new StatusWriter(StatusFile.SIGNED);
	}

	@Bean
	public IntegrationFlow uploadElligibleFilesForSignature6() {
		return IntegrationFlows.from("fileIsNotSigned").log().get();
	}

	@Bean
	public AbstractMessageRouter router() {
		AbstractMessageRouter router = new AbstractMessageRouter() {
			@Override
			protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
				RSignRequest req = (RSignRequest) message.getPayload();
				if (req.isSigned()) {
					return Arrays.asList(getChannelResolver().resolveDestination("fileIsSigned"));
				} else {
					return Arrays.asList(getChannelResolver().resolveDestination("fileIsNotSigned"));
				}
			}
		};
		return router;
	}

	@Bean
	public MessageSource<File> inputFileSource3() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(this.inboundReadDirectory);

		RecursiveDirectoryScanner scanner = new RecursiveDirectoryScanner();
		CompositeFileListFilter<File> filter = new CompositeFileListFilter<File>(
				Arrays.asList(new RegexPatternFileListFilter(regex), maybeSignedFilter()));
		scanner.setFilter(filter);
		source.setScanner(scanner);
		
		source.setAutoCreateDirectory(false);
		return source;
	}

	@Bean
	public MaybeSignedFilter maybeSignedFilter() {
		return new MaybeSignedFilter();
	}

	@Bean
	public VerifyMessage verifyMessage() {
		return new VerifyMessage();
	}

}