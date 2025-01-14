package datacrawl;

import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.fetcher.*;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlData {
    public static void main(String[] args) {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(DataCrawlConfigInfo.crawlStorageFolder);
        config.setPolitenessDelay(DataCrawlConfigInfo.politenessDelay);
        config.setMaxDepthOfCrawling(DataCrawlConfigInfo.maxDepthOfCrawling);
        config.setMaxPagesToFetch(DataCrawlConfigInfo.maxPagesToFetch);
        config.setIncludeBinaryContentInCrawling(DataCrawlConfigInfo.includeBinaryContentInFiltering);
        config.setResumableCrawling(DataCrawlConfigInfo.resumableCrawling);
        config.setIncludeHttpsPages(true);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig,
                pageFetcher);
        try {
            CrawlController controller = new CrawlController(config,
                    pageFetcher, robotstxtServer);
            for (String webpageRoot : DataCrawlConfigInfo.webpageRoots) {
                controller.addSeed(webpageRoot);
            }

            controller.start(DataCrawler.class,
                    DataCrawlConfigInfo.numberOfCrawlers);
        } catch (Exception e) {

        }
    }
}
