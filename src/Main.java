import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
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
	static private File datadir;
	static private Directory dir;
	static private MySimilarity similarity;
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		System.out.println("haha");
		String input = request.getParameter("keyword");
		System.out.println(input);
	}
	public static void EstablishIndex(Analyzer analyzer) {
		try{
			IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
			//writer.setSimilarity(similarity);   //设置相关度  
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
					if (key.equals("作者")) {  //作者字段按分号分词
						String[] authors = val.split(";");
						d.add(new Field(key, val, Field.Store.YES, Field.Index.NOT_ANALYZED));  //设置val为key"name"的val值
						for (String author: authors) {
							d.add(new Field(key, author, Field.Store.YES, Field.Index.NOT_ANALYZED));
						}
					} else {
						d.add(new Field(key, val, Field.Store.YES, Field.Index.ANALYZED));
					}
				}
			}
			writer.close();
			System.out.println("Index finish!");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String []args) {
		System.out.println("Start!");
		try{
			datadir = new File("./data/index/");
			dir = FSDirectory.open(datadir);
			similarity = new MySimilarity();
			Analyzer analyzer = new IKAnalyzer();//new StandardAnalyzer(Version.LUCENE_35); //
			//EstablishIndex(analyzer);
			//多域查询
			//作者
			String author = new String("叶丽雅").trim();  
			Query q1 = new TermQuery(new Term("作者", author));
			//标题
			String title = new String("资本").trim();
			QueryParser qp2 = new QueryParser(Version.LUCENE_35, "篇名", analyzer);
			Query q2 = qp2.parse(title);
			//时间范围
			String year = new String("2008").trim();  
			QueryParser qp3 = new QueryParser(Version.LUCENE_35, "年", analyzer);
			Query q3 = qp3.parse(year);
			//摘要
			String summary = new String("民间").trim();  
			QueryParser qp4 = new QueryParser(Version.LUCENE_35, "中文摘要", analyzer);
			Query q4 = qp4.parse(title);
			//布尔查询
			BooleanQuery q = new BooleanQuery();
			q.add(q1, Occur.MUST);
			q.add(q2, Occur.MUST);
			q.add(q3, Occur.MUST);
			q.add(q4, Occur.MUST);
			//QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, new String[]{"作者", "篇名"}, new IKAnalyzer());
			//Query q = parser.parse("创业");
			System.out.println(q);
			//开始查询
			IndexSearcher searcher = new IndexSearcher(dir);
			searcher.setSimilarity(similarity); 
			TopDocs td = searcher.search(q, null, 100);
			int hitnum = td.totalHits;
			System.out.println(hitnum);
			ScoreDoc[] hits = td.scoreDocs;
			for (int i = 0 ; i < hits.length; ++i) {
				Document hitdoc = searcher.doc(hits[i].doc);
				if (hitdoc.getField("作者") != null && hitdoc.getField("篇名") != null) {
					System.out.println("作者：" + hitdoc.getField("作者").stringValue()
							+ " 篇名：" + hitdoc.getField("篇名").stringValue() 
							+ " score=" + hits[i].score 
							+ " docId=" + hits[i].doc);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}