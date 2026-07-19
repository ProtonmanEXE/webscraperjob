package protonmanexe.com.webscraperjob.job.greenwoodnewsjob;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.TelegramBot;

import static protonmanexe.com.webscraperjob.constants.Constants.*;
import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;
import protonmanexe.com.webscraperjob.service.GreenwoodNewsService;
import protonmanexe.com.webscraperjob.service.TelegramMessagerService;
import protonmanexe.com.webscraperjob.utils.GeneralUtils;

@Component
public class GreenwoodNewJobExecutionListener implements JobExecutionListener {

    private final static Logger log = LoggerFactory.getLogger(GreenwoodNewJobExecutionListener.class);

    @Value("#{${greenwood.news.filter.keywords}}")
    private List<String> filterKeywords;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.greenwood.news.chat.id}")
    private String chatId;

    @Autowired
    private GreenwoodNewsService greenwoodNewsSvc;

    @Autowired
    private TelegramMessagerService TeleMsgSvc;

    @Autowired
    private GeneralUtils generalUtils;

    @Override
    public void beforeJob(JobExecution jobExecutionListener) {
        log.info("Starting jobexecutionlistener...");

        // 1) Initialise variables
        List<GreenwoodNewsArticle> listOfNews = new ArrayList<>();

        // 2) Use webscraper to extract all news article
        for (GreenwoodNewsArticle homeArticle : greenwoodNewsSvc.scrapeGreenwoodNewsHomePage()) {
            listOfNews.add(homeArticle);
        }
        for (GreenwoodNewsArticle crimeArticle : greenwoodNewsSvc.scrapeGreenwoodNewsCrimePage()) {
            listOfNews.add(crimeArticle);
        }

        // 2) Filter article base on keywords
        List<GreenwoodNewsArticle> filteredArticleList = 
            listOfNews.stream().filter(greenwoodNewsArticle -> {
                return filterKeywords.stream().anyMatch(
                    greenwoodNewsArticle.getHeadlines()::contains);
                })
                .collect(Collectors.toList());

        // 3) Check whether is there any relevant news and only proceed to remove duplicated if true
        if (filteredArticleList != null && !filteredArticleList.isEmpty()) {
            log.info("{} filtered articles were found", filteredArticleList.size());
            List<GreenwoodNewsArticle> finalArticleList = 
                generalUtils.checkForDuplicateNews(filteredArticleList);
            log.info("{} unique articles were found", finalArticleList.size());
            jobExecutionListener.getExecutionContext().put(
                GREENWOOD_NEWS_ARTICLE_LIST, finalArticleList);
        } else {
        // 4) Send telgram message if no relevant articles are found
            log.info("No relevant articles were found");

            TelegramBot bot = null;
            Long fullChatId = null;
            try {
                bot = new TelegramBot(botToken);
                fullChatId = -Long.valueOf(chatId);
            } catch (NumberFormatException e) {
                log.error("Error sending message, error {}", e.toString());
            }
            
            String msg = generalUtils.generateTimeInHourPmAm()
                .concat(GREENWOOD_NEW_BULLETIN)
                .concat(" - ")
                .concat(NO_GREENWOOD_NEWS);
            log.info("Msg: {}", msg);
            TeleMsgSvc.sendTelegramMessage(bot, msg, fullChatId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterJob(JobExecution jobExecutionListener) {
        // 1) Initialise variables
        TelegramBot bot = null;
        Long fullChatId = null;
        List<GreenwoodNewsArticle> nonOutdatedNews = 
            (List<GreenwoodNewsArticle>) jobExecutionListener.getExecutionContext().get(
                UPDATED_GREENWOOD_NEWS_LIST);

        try {
        // 2) Create Telegram bot and chat id
            bot = new TelegramBot(botToken);
            fullChatId = -Long.valueOf(chatId);
        } catch (NumberFormatException e) {
            log.error("Error sending message, error {}", e.toString());
        }

        // 4) Send no greenwood news msg if updated news list has no more news
        if (nonOutdatedNews == null || nonOutdatedNews.isEmpty()) {
            String msg = generalUtils.generateTimeInHourPmAm()
            .concat(GREENWOOD_NEW_BULLETIN)
            .concat(" - ")
            .concat(NO_GREENWOOD_NEWS);
            log.info("Msg: {}", msg);
            TeleMsgSvc.sendTelegramMessage(bot, msg, fullChatId);
        } else {
        // 5) Send greenwood news msg template if updated news list still has news
            String bulletinMsg = generalUtils.generateTimeInHourPmAm().toLowerCase()
                .concat(GREENWOOD_NEW_BULLETIN);
            TeleMsgSvc.sendTelegramMessage(bot, bulletinMsg, fullChatId);

            for (GreenwoodNewsArticle article : nonOutdatedNews) {
                log.info("Article: {}", article.toString());

                String msg = ("Headline: ")
                    .concat(article.getHeadlines())
                    .concat(System.lineSeparator())
                    .concat("Date: ")
                    .concat(article.getDate())
                    .concat(System.lineSeparator())
                    .concat("Link: ")
                    .concat(article.getUrl());
                TeleMsgSvc.sendTelegramMessage(bot, msg, fullChatId);
            }
        }

        log.info("Job ended with {} at {}", 
            jobExecutionListener.getStatus(), jobExecutionListener.getEndTime());
    }
}