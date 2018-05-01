import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
//import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class IndexService {

    public static void main(String[] args) throws IOException {
        String dirPath = "/home/afnan/Downloads/Corel/";
        String indexName = "index";
        IndexService idx = new IndexService();
//        idx.createIndexes(dirPath, indexName);
//        idx.listSimilarEntries(dirPath, 12, indexName);
//        idx.listAllIndexes(indexName);
//        idx.indexExists(indexName);
        idx.removeIndex(indexName);
//        idx.insertIndex("/home/afnan/Pictures/3.png", indexName);

    }
    public void insertIndex(String imageFilePath, String indexName) throws IOException {
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(false);

        globalDocumentBuilder.addExtractor(CEDD.class);
        globalDocumentBuilder.addExtractor(FCTH.class);
        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);

        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter iw = new IndexWriter(FSDirectory.open(Paths.get(indexName)), conf);
        try {
            BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
            Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
            iw.addDocument(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
        iw.close();
    }
    public void removeIndex(String indexName) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexName));
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter iw = new IndexWriter(dir, conf);
        iw.deleteAll();
        iw.close();
    }

    public void indexExists(String indexName) throws IOException {
        boolean ind = DirectoryReader.indexExists(FSDirectory.open(Paths.get(indexName)));
//        System.out.println(FSDirectory.open(Paths.).listAll());
        System.out.println("Index exists result: " + ind);

    }
    public void listAllIndexes(String indexName) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexName));
        IndexReader ir = DirectoryReader.open(dir);
        for (int i=0; i<ir.maxDoc(); i++) {
            Document doc = ir.document(i);
            System.out.println(doc);
        }
        System.out.println("Total docs: "+ ir.maxDoc());

    }
    public void listSimilarEntries(String dirPath, int limit, String indexName) throws IOException {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        Random rd = new Random();
        int source = rd.nextInt(files.length);
        BufferedImage img = null;
        File f = files[source];
        System.out.println("File randomly chosen:" + f.getName());


        if (f.exists()) {
            try {
                img = ImageIO.read(f);


            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No image exists");
            System.exit(1);
        }

        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        ImageSearcher searcher = new GenericFastImageSearcher(limit, CEDD.class);
        ImageSearchHits hits = searcher.search(img, ir);
        for (int i = 0; i < hits.length(); i++) {
            String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }
    }

    public void createIndexes(String dirPath, String indexName) throws IOException {
        File f = new File(dirPath);
        ArrayList<String> images = FileUtils.getAllImages(f, true);
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(false);

        globalDocumentBuilder.addExtractor(CEDD.class);
        globalDocumentBuilder.addExtractor(FCTH.class);
        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);

        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
        IndexWriter iw = new IndexWriter(FSDirectory.open(Paths.get(indexName)), conf);
        for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
            String imageFilePath = it.next();
            System.out.println("Indexing " + imageFilePath);
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing it.");
                e.printStackTrace();
            }
        }
        // closing the IndexWriter
        iw.close();
        System.out.println("Finished indexing.");
    }
}
