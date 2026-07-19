package protonmanexe.com.webscraperjob.service;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static protonmanexe.com.webscraperjob.constants.Constants.GREENWOOD_DATE_CSS_QUERY_STRING;
import static protonmanexe.com.webscraperjob.constants.Constants.GREENWOOD_NEWS_CSS_QUERY_STRING;
import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;

@Service
public class GreenwoodNewsService {

    private final static Logger log = LoggerFactory.getLogger(GreenwoodNewsService.class);

    @Value("${greenwood.news.home.page}")
    private String greenwoodNewsHomePageUrl;

    @Value("${greenwood.news.crime.page}")
    private String greenwoodNewsCrimePageUrl;

    @Autowired
    private Webscraper webscraper;

    public List<GreenwoodNewsArticle> scrapeGreenwoodNewsHomePage() {

		List<GreenwoodNewsArticle> news = new ArrayList<>(); 
        Elements newsElements = null;

        // 1) Scrap all news from website with css query string property
        if (greenwoodNewsHomePageUrl != null && !greenwoodNewsHomePageUrl.isBlank()) {
            newsElements = webscraper.scrapeNewsSite(greenwoodNewsHomePageUrl, 
                                                     GREENWOOD_NEWS_CSS_QUERY_STRING); 
        } else throw new NullPointerException("Url is missing!");

        // 2) Save each result into a pojo, then add into a list
        if (newsElements != null) {
            for (Element element : newsElements) {
                GreenwoodNewsArticle article = new GreenwoodNewsArticle();
                article.setHeadlines(element.selectFirst("a").text());
                article.setUrl(element.selectFirst("a").attr("href"));
                article.setSourceUrl(greenwoodNewsHomePageUrl);
                news.add(article);
            }
        } else log.error("No news articles from {}", greenwoodNewsHomePageUrl);

		log.info("Number of objects scraped: {}", news.size());

        return news;
    }

    public List<GreenwoodNewsArticle> scrapeGreenwoodNewsCrimePage() {

		List<GreenwoodNewsArticle> news = new ArrayList<>(); 
        Elements newsElements = null;

        // 1) Scrap all news from website with css query string property
        if (greenwoodNewsCrimePageUrl != null && !greenwoodNewsCrimePageUrl.isBlank()) {
            newsElements = webscraper.scrapeNewsSite(greenwoodNewsCrimePageUrl, 
                                                     GREENWOOD_NEWS_CSS_QUERY_STRING); 
        } else throw new NullPointerException("Url is missing!");

        // 2) Save each result into a pojo, then add into a list
        if (newsElements != null) {
            for (Element element : newsElements) {
                GreenwoodNewsArticle article = new GreenwoodNewsArticle();
                article.setHeadlines(element.selectFirst("a").text());
                article.setUrl(element.selectFirst("a").attr("href"));
                article.setSourceUrl(greenwoodNewsCrimePageUrl);
                news.add(article);
            }
        } else log.error("No news articles from {}", greenwoodNewsCrimePageUrl);

		log.info("Number of objects scraped: {}", news.size());

        return news;
    }

    public String scrapeGreenwoodNewsForDate(String url) {
        String date = null;
        Elements newsElements = webscraper.scrapeNewsSite(url, 
            GREENWOOD_DATE_CSS_QUERY_STRING);
        if (newsElements != null) {
            for (Element element : newsElements) {
                date = element.selectFirst("time").text();
            }
        }
        log.info("Date for {} is {}", url, date);
        return date;
    }

}