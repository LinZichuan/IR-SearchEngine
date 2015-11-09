import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Main extends HttpServlet{
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("haha");
		String input = request.getParameter("keyword");
		System.out.println(input);
	}
	public static void main(String []args) {
		System.out.println("Start!");
		try{
			File datadir = new File("./data/index/");
			Directory dir = FSDirectory.open(datadir);
			IndexWriter writer = new IndexWriter(dir, new IKAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
			//构建Document并写入IndexWriter
			File srcfile = new File("./data/CNKI_journal.txt");
			StringBuffer buffer = new StringBuffer();
			InputStreamReader isr = new InputStreamReader(new FileInputStream(srcfile), "GB2312");
			BufferedReader br = new BufferedReader(isr); 
			String ss;
			ArrayList doc = new ArrayList();
			int linenum = 0, block = 1;
			while((ss=br.readLine()) != null) {
				if (ss.equals("")) continue;
				linenum ++;
				if (linenum % 100000 == 0) {
					block ++;
					System.out.println(block);
				}
				if (ss.equalsIgnoreCase("<REC>")) {
					if (doc.size() > 0) {
						writer.addDocument((Document)doc.get(doc.size()-1));//将Document写入Index
					}
					doc.add(new Document());  //新建一个Document
				}else {
					int flag = ss.indexOf(">=");
					if (flag <= 1) {
						Document d = (Document) doc.get(doc.size()-1);
						d.add(new Field("基金", ss, Field.Store.YES, Field.Index.ANALYZED));
						continue;
					}
					String key = ss.substring(1, flag);
					String val = ss.substring(flag+2, ss.length());
					Document d = (Document) doc.get(doc.size()-1);
					d.add(new Field(key, val, Field.Store.YES, Field.Index.ANALYZED));
				}
			}
			writer.close();
			System.out.println("Index finish!");
			//构建query
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, new String[]{"作者", "篇名"}, new IKAnalyzer());
			Query q = parser.parse("创业");
			System.out.println(q);
			//开始查询
			IndexSearcher searcher = new IndexSearcher(dir);
			TopDocs td = searcher.search(q, null, 100);
			int hitnum = td.totalHits;
			System.out.println(hitnum);
			ScoreDoc[] hits = td.scoreDocs;
			for (int i = 0 ; i < hits.length; ++i) {
				Document hitdoc = searcher.doc(hits[i].doc);
				if (hitdoc.getField("篇名") != null)
					System.out.println(hitdoc.getField("篇名").stringValue());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}