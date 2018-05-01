import javax.imageio.ImageIO;
//import javax.swing.text.Document;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.Object.*;
import java.util.Random;

//import net.semanticmetadata.lire.imageanalysis.CEDD;
//import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
//import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.document.Document;

import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import org.apache.lucene.store.FSDirectory;

/**
 * Simple class showing the process of indexing
 * @author Mathias Lux, mathias@juggle.at and Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class Indexer {
    public static void main(String[] args) throws IOException {
        String dirPath = "~/Downloads/Corel";
        Indexer idx = new Indexer();
//        idx.createIndexes(dirPath);


        idx.listSimilarEntries(dirPath, 12);


    }

    public void listSimilarEntries(String dirPath, int limit) throws IOException {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        Random rd = new Random();
        int source = rd.nextInt(files.length);
        BufferedImage img = null;
        File f = files[source];


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

        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        ImageSearcher searcher = new GenericFastImageSearcher(30, CEDD.class);
        ImageSearchHits hits = searcher.search(img, ir);
        for (int i = 0; i < hits.length(); i++) {
            String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }
    }

    public void createIndexes(String dirPath) throws IOException {
        File dr = new File(dirPath);
        String images[] = dr.list();

        // Creating a CEDD document builder and indexing all files.
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
        // Creating an Lucene IndexWriter
        IndexWriter iw = LuceneUtils.createIndexWriter("index", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);

        for (String it: images) {
            String imageFilePath = it;
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
        iw.close();
        System.out.println("Finished indexing.");

    }
}
