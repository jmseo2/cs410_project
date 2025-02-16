package datacrawl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import org.apache.http.Header;

public class DataCrawler extends WebCrawler {
    private final static Pattern BINARY_FILES_EXTENSIONS = Pattern
            .compile(".*\\.(bmp|gif|jpe?g|png|tiff?|pdf|ico|xaml|pict|rif|pptx?|ps"
                    + "|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox"
                    + "|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv"
                    + "|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha)"
                    + "(\\?.*)?$");

    private boolean startsWithAny(String href, String[] webpageRoots) {
        for (String webpageRoot : webpageRoots) {
            if (href.startsWith(webpageRoot)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        boolean shouldVisit = !BINARY_FILES_EXTENSIONS.matcher(href).matches()
                && startsWithAny(href, DataCrawlConfigInfo.webpageRoots);
        // if (startsWithAny(href, DataCrawlConfigInfo.webpageRoots))
        // System.out.println(url + " " + shouldVisit);
        return shouldVisit;
    }

    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        System.out.println("Docid: " + docid);
        System.out.println("URL: " + url);
        System.out.println("Domain: " + domain);
        System.out.println("Sub-domain: " + subDomain);
        System.out.println("Path: " + path);
        System.out.println("Parent page: " + parentUrl);
        System.out.println("Anchor text: " + anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String title = htmlParseData.getTitle();
            String text = htmlParseData.getText().trim()
                    .replaceAll("[^\\x00-\\x7F]", " ").replaceAll(" +", " ");
            String html = htmlParseData.getHtml();
            List<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Number of outgoing links: " + links.size());

            // save parsed crawled page into the crawl storage folder
            String crawlStorageFolder = this.getMyController().getConfig()
                    .getCrawlStorageFolder();
            try {
                PrintWriter out = new PrintWriter(new FileWriter(
                        crawlStorageFolder + "/gamespot_review" + docid
                                + ".txt"));
                out.println(docid);
                out.println(url);
                out.println(title);
                out.println(text);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            System.out.println("Response headers:");
            for (Header header : responseHeaders) {
                System.out.println("\t" + header.getName() + ": "
                        + header.getValue());
            }
        }

        System.out.println("=============");
    }
}
