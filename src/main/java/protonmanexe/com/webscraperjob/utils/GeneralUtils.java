package protonmanexe.com.webscraperjob.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import protonmanexe.com.webscraperjob.models.GreenwoodNewsArticle;

@Component
public class GeneralUtils {

    public List<GreenwoodNewsArticle> checkForDuplicateNews(List<GreenwoodNewsArticle> list) {
        List<GreenwoodNewsArticle> newList = new ArrayList<>();
        boolean duplicateFlag = false;

        for (GreenwoodNewsArticle article : list) {
            if (newList.isEmpty()) {
                newList.add(article);
            } else {
                for (GreenwoodNewsArticle addedArticle : newList) {
                    if (article.getHeadlines().equalsIgnoreCase(addedArticle.getHeadlines())) {
                        duplicateFlag = true;
                        break;
                    } else duplicateFlag = false;
                }

                if (!duplicateFlag) newList.add(article);
            }
        }
        
        return newList;
    }

    public String generateTimeInHourPmAm() {
    	DateFormat dateFormat = new SimpleDateFormat("hhaa"); // Displaying current time in 12 hour format with AM/PM
    	String dateString = dateFormat.format(new Date());

        int index = 0;
        for (int i = 0; i < dateString.length(); i++) {
            char p = dateString.charAt(i);
            if (p != '0') {
                index = i;
                break;
            }
        }
        return dateString.substring(index, dateString.length()).toLowerCase();
    }

    public boolean compareTimeDifferenceInDays(String date, String datetimeformat, int threshold) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(datetimeformat);
		LocalDate date1 = LocalDate.parse(date, dtf);
		LocalDate date2 = LocalDate.now();
		Period diff = Period.between(date1, date2);
        return diff.getDays() < threshold; // true indicates non-outdated article
    }
}