package protonmanexe.com.webscraperjob.job.greenwoodnewsjob;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static protonmanexe.com.webscraperjob.constants.Constants.GREENWOOD_NEWS_ARTICLE_LIST;
import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;
import protonmanexe.com.webscraperjob.service.GreenwoodNewsService;
import protonmanexe.com.webscraperjob.utils.GeneralUtils;

@Component
public class GreenwoodNewJobExecutionListener implements JobExecutionListener {

    private final static Logger log = LoggerFactory.getLogger(GreenwoodNewJobExecutionListener.class);

    @Autowired
    private GreenwoodNewsService greenwoodNewsSvc;

    @Autowired
    private GeneralUtils generalUtils;

    @Override
    public void beforeJob(JobExecution jobExecutionListener) {
        log.info("Starting jobexecutionlistener...");

        // 1) Use webscraper to extract all news article
        List<GreenwoodNewsArticle> listOfNews = greenwoodNewsSvc.scrapeGreenwoodNewsWebsite();

        // 2) Filter article base on keywords
        List<String> keywords = List.of("job", "kill", "dies", "dead", "shooting");

        List<GreenwoodNewsArticle> filteredArticleList = 
            listOfNews.stream().filter(greenwoodNewsArticle -> {
                boolean filterFlag = keywords.stream().anyMatch(
                    greenwoodNewsArticle.getHeadlines().toLowerCase()::contains);
                log.info("{}: {}", greenwoodNewsArticle.getHeadlines(), filterFlag);
                return filterFlag;
            })
                .collect(Collectors.toList());
        
        // 3) Check whether is there any relevant news and only proceed to remove duplicated if true
        if (filteredArticleList != null && !filteredArticleList.isEmpty()) {
            List<GreenwoodNewsArticle> finalArticleList = 
                generalUtils.checkForDuplicateNews(filteredArticleList);
            log.info("{} relevant articles were found", finalArticleList.size());
            jobExecutionListener.getExecutionContext().put(GREENWOOD_NEWS_ARTICLE_LIST, finalArticleList);
        } else {
            log.info("No relevant articles were found");
            jobExecutionListener.setStatus(BatchStatus.COMPLETED);
        }
    }

    @Override
    public void afterJob(JobExecution jobExecutionListener) {
        log.info("Job ended with {} at {}", 
            jobExecutionListener.getStatus(), jobExecutionListener.getEndTime());
    }
}