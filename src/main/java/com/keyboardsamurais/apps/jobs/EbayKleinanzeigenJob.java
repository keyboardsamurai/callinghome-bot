package com.keyboardsamurais.apps.jobs;

import com.keyboardsamurais.apps.db.DatabaseManager;
import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Antonio Agudo  on 27.01.23
 */
@Slf4j
public class EbayKleinanzeigenJob implements Runnable {
    static final String BASE_URL = "https://www.ebay-kleinanzeigen.de";
    private final DatabaseManager databaseManager;
    private final String url;
    private FinishedJobCallback callback;
    private boolean firstRun = true;

    private List<ClassifiedItem> newItems;

    public EbayKleinanzeigenJob(final DatabaseManager databaseManager, String url, final FinishedJobCallback callback) {
        this.databaseManager = databaseManager;
        this.url = url;
        this.callback = callback;
    }

    @Override
    public void run() {
        log.info("Now running EbayKleinanzeigenJob at {} for url {} ", Instant.now(), url);
        try {
            final var articles = getArticles();

            this.newItems = new ArrayList<>();
            // iterate over all articles
            for (var article : articles) {
                final var title = Optional.ofNullable(article.select("a.ellipsis").first()).map(Element::text).orElse("title not found");
                final var content = Optional.ofNullable(article.select("p").first()).map(Element::text).orElse("content not found");
                final var link = Optional.ofNullable(article.select("a").first()).map(e -> e.attr("href")).orElse("link not found");
                final var image = Optional.ofNullable(article.select("div.imagebox").first()).map(e -> e.attr("data-imgsrc")).orElse("image not found");

                log.info("title: {}, content: {}, link: {}, image: {}", title, content, link, image);

                final var newItem = ClassifiedItem.builder().title(title).content(content).url(link).imageUrl(image).created(Instant.now()).build();
                databaseManager.findByUrl(link).ifPresentOrElse(
                        classifiedItem -> log.info("Found item '{}' that already exists", link),
                        () -> {
                            databaseManager.save(newItem);
                            newItems.add(newItem);
                            log.info("Saved item '{}'", link);
                        }
                );
            }

            if (!firstRun) {
                // links are relative on page, so we need to add the base url
                newItems.forEach(i -> i.setUrl(BASE_URL + i.getUrl()));
                callback.onFinished(newItems);
            }
            firstRun = false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // intentionally left as package private for better testability and less exposure
    Elements getArticles() throws IOException {
        final Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 403) {
                log.error("Got 403 forbidden from ebay kleinanzeigen. Maybe we are banned? {}", e.getMessage());
                // TODO: handle 403 forbidden bans by ebay kleinanzeigen somehow. Maybe try a long timeout and retry?
                throw e;
            } else {
                throw e;
            }
        }
        return doc.select("article");
    }

    // intentionally left as package private for better testability and less exposure
    void setCallback(final FinishedJobCallback callback) {
        this.callback = callback;
    }
}
