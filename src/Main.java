import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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
			doc.add(new Field("content", "Lucene1 works well", Field.Store.YES, Field.Index.ANALYZED));  
			writer.addDocument(doc);
			Document doc1 = new Document();  
			doc1.add(new Field("title", "Lucene", Field.Store.YES, Field.Index.ANALYZED));  
			doc1.add(new Field("content", "Lucene2 works well", Field.Store.YES, Field.Index.ANALYZED));  
			writer.addDocument(doc1);
			writer.close();
			System.out.println("Index finish!");
			IndexSearcher searcher = new IndexSearcher(dir);
			Term t = new Term("title", "introduction");
			Query q = new TermQuery(t);
			TopDocs td = searcher.search(q, null, 100);
			int hitnum = td.totalHits;
			ScoreDoc[] hits = td.scoreDocs;
			for (int i = 0 ; i < hits.length; ++i) {
				Document hitdoc = searcher.doc(hits[i].doc);
				System.out.println(hitdoc.getField("content").stringValue());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}