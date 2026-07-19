package protonmanexe.com.webscraperjob.job.greenwoodnewsjob;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pengrad.telegrambot.TelegramBot;

import static protonmanexe.com.webscraperjob.constants.Constants.*;
import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;
import protonmanexe.com.webscraperjob.service.TelegramMessagerService;
import protonmanexe.com.webscraperjob.utils.GeneralUtils;

@Component
public class GreenwoodNewsItemWriter implements ItemWriter<GreenwoodNewsArticle>, StepExecutionListener {

    private final static Logger log = LoggerFactory.getLogger(GreenwoodNewsItemReader.class);

    private StepExecution stepExecution;

    @Value("${greenwood.news.date.threshold}")
    private int dateThreshold;

    @Autowired
    private GeneralUtils generalUtils;

    @SuppressWarnings("unchecked")
    @Override
    public void write(Chunk<? extends GreenwoodNewsArticle> items) throws Exception {

        log.info("Starting itemwriter...");

        // 1) Initialise variables
        List<GreenwoodNewsArticle> listOfNews = (List<GreenwoodNewsArticle>) items.getItems();
        List<GreenwoodNewsArticle> nonOutdatedNews = new ArrayList<>();
        List<GreenwoodNewsArticle> existingUpdatedArticleList = 
            (List<GreenwoodNewsArticle>) this.stepExecution.getJobExecution().getExecutionContext()
                .get(UPDATED_GREENWOOD_NEWS_LIST);
        if (!(existingUpdatedArticleList == null || existingUpdatedArticleList.isEmpty())) {
            log.info("Existing article list contains {} articles", 
                existingUpdatedArticleList.size());        
        } else {
            existingUpdatedArticleList = new ArrayList<>();
            log.info("Current article list contains no articles...");  
        }

        // 3) Check articles to determine whether they are outdated
        for (GreenwoodNewsArticle article : listOfNews) {
            log.info("Checking date for {}, date is {}", article.getHeadlines(), article.getDate());
            if (generalUtils.compareTimeDifferenceInDays(article.getDate(), "MMM d, yyyy", dateThreshold)) {
                nonOutdatedNews.add(article);
            } else log.info("Article {} was removed", article.getHeadlines());
        }

        if (!(nonOutdatedNews.isEmpty())) {
            int i = 0;
            for (GreenwoodNewsArticle article : nonOutdatedNews) {
                existingUpdatedArticleList.add(article);
                i = i + 1;
            }

            this.stepExecution.getJobExecution().getExecutionContext()
                .put(UPDATED_GREENWOOD_NEWS_LIST, existingUpdatedArticleList);
            log.info("Added {} article(s) to existingUpdatedArticleList", i);
        } 
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }
}