import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
			//构建Document并写入IndexWriter
			File srcfile = new File("./data/data.txt");
			StringBuffer buffer = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(new FileInputStream(srcfile), "GB2312");
			BufferedReader br = new BufferedReader(isr); 
			String ss;
			ArrayList doc = new ArrayList();
			while((ss=br.readLine()) != null) {
				if (ss.equalsIgnoreCase("<REC>")) {
					if (doc.size() > 0) {
						writer.addDocument((Document)doc.get(doc.size()-1));//将Document写入Index
					}
					doc.add(new Document());  //新建一个Document
				}else {
					int flag = ss.indexOf(">=");
					String key = ss.substring(1, flag);
					String val = ss.substring(flag+2, ss.length());
					//System.out.println(key + "," + val);
					Document d = (Document) doc.get(doc.size()-1);
					d.add(new Field(key, val, Field.Store.YES, Field.Index.ANALYZED));
				}
			}
			Document dd = new Document();
			dd.add(new Field("CN", "11-3928/TN", Field.Store.YES, Field.Index.ANALYZED));
			dd.add(new Field("中文刊名", "hahaha", Field.Store.YES, Field.Index.ANALYZED));
			writer.addDocument(dd);
			writer.close();
			System.out.println("Index finish!");
		
			IndexSearcher searcher = new IndexSearcher(dir);
			Term t = new Term("CN", "11-3928/TN");
			Query q = new TermQuery(t);
			System.out.println(q);
			TopDocs td = searcher.search(q, null, 100);
			int hitnum = td.totalHits;
			System.out.println(hitnum);
			ScoreDoc[] hits = td.scoreDocs;
			for (int i = 0 ; i < hits.length; ++i) {
				Document hitdoc = searcher.doc(hits[i].doc);
				System.out.println(hitdoc.getField("中文刊名").stringValue());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}