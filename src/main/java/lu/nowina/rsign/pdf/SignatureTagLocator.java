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
package lu.nowina.rsign.pdf;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Allows to locate a signature tag using PDFBox.
 *
 * @author Jean Lepropre (jean.lepropre@nowina.lu)
 */
public class SignatureTagLocator extends PDFTextStripper {

	private final String signatureTag;
	private boolean signatureTagFound;
	private int pageNo;
	private float x;
	private float y;
	private boolean signatureZoomFound;
	private int signatureZoom;

	public SignatureTagLocator(final String signatureTag) throws IOException {
		this.signatureTag = signatureTag;
		this.signatureTagFound = false;
		this.pageNo = -1;
		this.x = -1;
		this.y = -1;
		this.signatureZoomFound = false;
		this.signatureZoom = -1;
	}

	@Override
	protected void writeString(final String text, final List<TextPosition> textPositions) throws IOException {
		super.writeString(text);

		final int start = text.indexOf(this.signatureTag);
		if (start != -1) {
			this.signatureTagFound = true;
			this.pageNo = this.getCurrentPageNo();
			final TextPosition pos = textPositions.get(start);
			this.x = pos.getXDirAdj();
			this.y = pos.getYDirAdj();
		}

		if (this.signatureTagFound) {
			final String regex = ".*" + this.signatureTag + "@([0-9]+)%.*";
			final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
			final Matcher matcher = pattern.matcher(text);

			if (matcher.find()) {
				this.signatureZoomFound = true;
				final String zoomString = matcher.group(1);
				this.signatureZoom = Integer.parseInt(zoomString);
			}
		}
	}

	public boolean isSignatureTagFound() {
		return this.signatureTagFound;
	}

	public boolean isSignatureZoomFound() {
		return this.signatureZoomFound;
	}

	public int getPageNo() {
		return this.pageNo;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	/**
	 * @return the signatureZoom
	 */
	public int getSignatureZoom() {
		return this.signatureZoom;
	}

}