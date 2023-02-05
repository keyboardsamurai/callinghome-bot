package com.keyboardsamurais.apps.db;

import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcDataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Antonio Agudo  on 27.01.23
 */
@Slf4j
public class DatabaseManager {
    private static final Integer MAX_AGE_DAYS = 30;
    private final DBI dbi;

    public DatabaseManager() {
        log.info("DatabaseManager.init");
        var ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:file:./callinghome.db");
        dbi = new DBI(ds);

        // create table
        try(var handle = dbi.open()) {
            handle.execute("""
                CREATE TABLE IF NOT EXISTS CLASSIFIED_ITEM (
                    id IDENTITY NOT NULL PRIMARY KEY,
                    title VARCHAR(2048),
                    content VARCHAR(2048),
                    url VARCHAR(2048),
                    imageUrl VARCHAR(2048) DEFAULT NULL,
                    created BIGINT
                )
                """);
        }
    }

    public void cleanup() {
        try (var handle = dbi.open()) {
            // find out how many items are in the database
            Integer count = countEntries(handle);
            log.info("Found {} items in the database", count);
            // if more than 30, delete the oldest ones that are older than 30 days
            if (count > MAX_AGE_DAYS) {
                log.warn("Found more items in the database than the threshold of {} allows, deleting the oldest ones", MAX_AGE_DAYS);
                handle.execute("DELETE FROM CLASSIFIED_ITEM WHERE ID IN (SELECT ID FROM CLASSIFIED_ITEM ORDER BY created DESC OFFSET %d) AND created < :created".formatted(MAX_AGE_DAYS),
                        Instant.now().minus(Duration.ofDays(MAX_AGE_DAYS)).toEpochMilli());
                final Integer newCount = countEntries(handle);
                log.warn("Deleted {} items from the database. New item count is {}", count - newCount, newCount);
            }
        }
    }

    private Integer countEntries(final Handle handle) {
        return handle.createQuery("SELECT COUNT(*) FROM CLASSIFIED_ITEM")
                .mapTo(Integer.class)
                .first();
    }

    public void save(ClassifiedItem item) {
        try (var handle = dbi.open()) {
            handle.execute("INSERT INTO CLASSIFIED_ITEM (title, content, url, imageUrl, created) VALUES (:title, :content, :url, :imageUrl, :created)",
                    item.getTitle(), item.getContent(), item.getUrl(), item.getImageUrl(), item.getCreated().toEpochMilli());
        }
    }
    public Optional<ClassifiedItem> findByUrl(String url) {
        try (var handle = dbi.open()) {
            var result = handle.createQuery("SELECT * FROM CLASSIFIED_ITEM WHERE url = :url")
                    .bind("url", url)
                    .map(new ClassifiedItemMapper())
                    .first();
            return Optional.ofNullable(result);
        }
    }
}
