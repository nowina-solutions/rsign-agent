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

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class PatternTest {

	@Test
	public void test() {

		Assert.assertTrue(Pattern.matches("aaa-([a-z0-9])+.pdf", "aaa-12.pdf"));
		Assert.assertTrue(Pattern.matches("aaa-([a-z0-9])+.pdf.(ERROR|EXPIRED)", "aaa-12.pdf.ERROR"));
		Assert.assertTrue(Pattern.matches("aaa-([a-z0-9])+.pdf.(ERROR|EXPIRED)", "aaa-test.pdf.ERROR"));

	}

}
