package com.keyboardsamurais.apps.db;

import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Antonio Agudo  on 28.01.23
 */
class DatabaseManagerTest {

    private DatabaseManager databaseManager;

    private final String url = "https://www.ebay-kleinanzeigen.de/s-anzeige/sony-xperia-1-ii/1491000001-0";
    private final String title = "Sony Xperia 1 II";
    private final String content = "Sony Xperia 1 II - Content";
    private final String image = "https://i.ebayimg.com/00/s/MTYwMFgxNjAw/z/8Z8AAOSw~3Zf~2ZQ/$_86.JPG";

    @BeforeEach
    void setUp() {
        this.databaseManager = new DatabaseManager();
    }

    @Test
    void save() {
        // test save

        final var item = ClassifiedItem.builder().url(url).title(title).content(content).imageUrl(image).created(Instant.now()).build();
        databaseManager.save(item);

        databaseManager.findByUrl(url).ifPresentOrElse(
                classifiedItem -> {
                    assertEquals(url, classifiedItem.getUrl());
                    assertEquals(title, classifiedItem.getTitle());
                    assertEquals(content, classifiedItem.getContent());
                    assertEquals(image, classifiedItem.getImageUrl());
                    assertNotNull(classifiedItem.getCreated());
                    assertNotNull(classifiedItem.getId());
                },
                () -> fail("Item not found")
        );
    }

    @Test
    void findByUrl() {
        assertTrue(databaseManager.findByUrl(url).isPresent());
    }
}
