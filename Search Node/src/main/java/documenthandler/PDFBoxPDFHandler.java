package documenthandler;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import documenthandler.internals.DocumentHandler;
import documenthandler.internals.DocumentHandlerException;

public class PDFBoxPDFHandler implements DocumentHandler {

	public static String password = "-password";

	public Document getDocument(InputStream is) throws DocumentHandlerException {

		COSDocument cosDoc = null;
		try {
			cosDoc = parseDocument(is);
		} catch (IOException e) {
			closeCOSDocument(cosDoc);
			throw new DocumentHandlerException("Cannot parse PDF document", e);
		}

		

		// extract PDF document's textual content
		String docText = null;
		try {
			PDFTextStripper stripper = new PDFTextStripper();
			PDDocument doc =new PDDocument(cosDoc);
			
			docText = stripper.getText(doc);
		} catch (IOException e) {
			closeCOSDocument(cosDoc);
			throw new DocumentHandlerException("Cannot parse PDF document", e);
			// String errS = e.toString();
			// if (errS.toLowerCase().indexOf("font") != -1) {
			// }
		}

		Document doc = new Document();
		if (docText != null) {
			
			doc.add(new Field("body", docText, Field.Store.YES, Index.ANALYZED));
		}

		// extract PDF document's meta-data
		PDDocument pdDoc = null;
		try {
			pdDoc = new PDDocument(cosDoc);
			PDDocumentInformation docInfo = pdDoc.getDocumentInformation();
			String author = docInfo.getAuthor();
			String title = docInfo.getTitle();
			String keywords = docInfo.getKeywords();
			String summary = docInfo.getSubject();
			if ((author != null) && (!author.equals(""))) {
				
				doc.add(new Field("author", author, Field.Store.YES, Index.ANALYZED));
			}
			if ((title != null) && (!title.equals(""))) {
				doc.add(new Field("title", title, Field.Store.YES, Index.ANALYZED));
			}
			if ((keywords != null) && (!keywords.equals(""))) {
				 doc.add(new Field("keywords", keywords, Field.Store.YES, Index.ANALYZED));
			}
			if ((summary != null) && (!summary.equals(""))) {
				 doc.add(new Field("summary", summary, Field.Store.YES, Index.ANALYZED));
			}
		} catch (Exception e) {
			closeCOSDocument(cosDoc);
			closePDDocument(pdDoc);
			System.err.println("Cannot get PDF document meta-data: "
					+ e.getMessage());
		}

		return doc;
	}

	private static COSDocument parseDocument(InputStream is) throws IOException {
		PDFParser parser = new PDFParser(is);
		parser.parse();
		return parser.getDocument();
	}

	private void closeCOSDocument(COSDocument cosDoc) {
		if (cosDoc != null) {
			try {
				cosDoc.close();
			} catch (IOException e) {
				// eat it, what else can we do?
			}
		}
	}

	private void closePDDocument(PDDocument pdDoc) {
		if (pdDoc != null) {
			try {
				pdDoc.close();
			} catch (IOException e) {
				// eat it, what else can we do?
			}
		}
	}

	public static void main(String[] args) throws Exception {
		PDFBoxPDFHandler handler = new PDFBoxPDFHandler();
		Document doc = handler.getDocument(new FileInputStream(
				new File(args[0])));
		System.out.println(doc);
	}
}
