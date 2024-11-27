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

import static protonmanexe.com.webscraperjob.constants.Constants.*;
import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;

@Service
public class GreenwoodNewsService {

    private final static Logger log = LoggerFactory.getLogger(GreenwoodNewsService.class);

    @Value("${greenwood.news.home.page}")
    private String greenwoodNewsUrl;

    @Autowired
    private Webscraper webscraper;

    public List<GreenwoodNewsArticle> scrapeGreenwoodNewsWebsite() {

		List<GreenwoodNewsArticle> news = new ArrayList<>(); 
        Elements newsElements = null;

        // 1) Scrap all news from website with css query string property
        if (greenwoodNewsUrl != null && !greenwoodNewsUrl.isBlank()) {
            newsElements = webscraper.scrapeNewsSite(greenwoodNewsUrl, 
                                                     GREENWOOD_NEWS_CSS_QUERY_STRING); 
        } else throw new NullPointerException("Url is missing!");

        // 2) Save each result into a pojo, then add into a list
        if (newsElements != null) {
            for (Element element : newsElements) {
                GreenwoodNewsArticle article = new GreenwoodNewsArticle();
                article.setHeadlines(element.selectFirst("a").text());
                article.setUrl(element.selectFirst("a").attr("href"));
                news.add(article);
            }
        } else log.error("No result from {}", greenwoodNewsUrl);

		log.info("Number of objects scraped: {}", news.size());

        return news;
    }

}