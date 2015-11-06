import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Main {
	public static void main(String []args) {
		System.out.println("Start!");
		try{
			File datadir = new File("./data/index/");
			Directory dir = FSDirectory.open(datadir);
			IndexWriter writer = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_35), true, IndexWriter.MaxFieldLength.LIMITED);
			Document doc = new Document();  
			doc.add(new Field("title", "Lucene introduction", Field.Store.YES, Field.Index.ANALYZED));  
			doc.add(new Field("content", "Lucene works well", Field.Store.YES, Field.Index.ANALYZED));  
			writer.addDocument(doc);
			writer.close();
			System.out.println("Index finish!");
			IndexSearcher searcher = new IndexSearcher(dir);
			Term t = new Term("title", "introduction");
			Query q = new TermQuery(t);
			TopDocs hits = searcher.search(q, null, 100);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}