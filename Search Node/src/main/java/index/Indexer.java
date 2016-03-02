package index;
import nl.rug.eco.lucene.EnglishLemmaAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.InputSource;


























import documenthandler.JTidyHTMLHandler;
import documenthandler.PDFBoxPDFHandler;
import documenthandler.internals.DocumentHandlerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
public class Indexer {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Usage: java " + Indexer.class.getName()
					+ " <index dir> <data dir>");
		}
		File indexDir = new File(args[0]);
		File dataDir = new File(args[1]);

		long start = new Date().getTime();
		int numIndexed = index(indexDir, dataDir, null);
		long end = new Date().getTime();

		System.out.println("Indexing " + numIndexed + " files took "
				+ (end - start) + " milliseconds");
	}

	public static int index(File index, File dataDir, String modelpath) throws Exception {

		if (!dataDir.exists() || !dataDir.isDirectory()) {
			throw new IOException(dataDir
					+ " does not exist or is not a directory");
		}
		//	Analyzer analyzer = new EnglishLemmaAnalyzer("/home/sanket/Downloads/stanford-postagger-2014-10-26/models/english-bidirectional-distsim.tagger");

		IndexWriter indexWriter;
		Directory indexDir  = FSDirectory.open(index);
		//Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_CURRENT, "English");
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_CURRENT);
		//Analyzer analyzer = new EnglishLemmaAnalyzer(modelpath);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
        config.setOpenMode(OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(indexDir, config);

		indexDirectory(indexWriter, dataDir);

		int numIndexed = indexWriter.numDocs();
		indexWriter.optimize();
		indexWriter.close();
		return numIndexed;
	}

	private static void indexDirectory(IndexWriter writer, File dir)
			throws IOException, DocumentHandlerException{

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				indexDirectory(writer, f); // recurse
			} else if (f.getName().endsWith(".html")) {
				indexFile(writer, f,"HTML");
			}else if (f.getName().endsWith(".pdf")) {
				indexFile(writer, f,"PDF");
			}else if (f.getName().endsWith("")) {
				indexFile(writer, f,"Text");
			}
		}
	}

	private static void indexFile(IndexWriter writer, File f,String type)
			throws IOException, DocumentHandlerException {

		if (f.isHidden() || !f.exists() || !f.canRead()) {
			return;
		}
		
		InputStream is =  new FileInputStream(f.getCanonicalFile());
		System.out.println("Indexing " + f.getCanonicalPath());
		
		Document doc = new Document();
		String content= getContent(f);
		if(type.equals("Text")){
			doc.add(new Field("content", content,Field.Store.YES, Index.ANALYZED_NO_NORMS));
			//System.out.println(content);
		}
		
		doc.add(new Field("filename",f.getName(), Field.Store.YES, Index.ANALYZED_NO_NORMS));
		System.out.println(f.getName());
		
		//Enamoring according to the type
				if(type=="HTML")
				{	
					JTidyHTMLHandler tidy= new JTidyHTMLHandler();
					Document tempdoc = tidy.getDocument(is);
					doc.add(new Field("title",tempdoc.get("title"), Field.Store.YES, Index.ANALYZED));
					doc.add(new Field("body",tempdoc.get("body") ,Field.Store.YES, Index.ANALYZED));
					System.out.println(tempdoc.get("title"));
					
				}
				else if(type=="PDF")
				{
					PDFBoxPDFHandler handler = new PDFBoxPDFHandler();
					Document tempdoc = handler.getDocument(is);
					 List<Fieldable> fields = tempdoc.getFields();
					for (int i=0;i<fields.size();i++)
					{
						Field param= (Field) fields.get(i);
						doc.add(new Field(param.name(), tempdoc.get(param.name()),Field.Store.YES, Index.ANALYZED));
						System.out.println(param.name());
					}
					
				}
				
		
		writer.addDocument(doc);
	}

	private static String getContent(File f) {
	
		
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader(f));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        String everything = sb.toString();
	        return everything;
	        } 
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    finally {
		    
	    }
		return null;
	}
}