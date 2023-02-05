package com.keyboardsamurais.apps.jobs;

import com.keyboardsamurais.apps.db.DatabaseManager;
import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import com.keyboardsamurais.apps.notification.UpdatePostedHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * @author Antonio Agudo  on 27.01.23
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public
class EbayKleinanzeigenJobTest {
    @Mock
    private DatabaseManager databaseManager;
    @Spy
    private UpdatePostedHandler willFailWhenCalled;
    @Spy
    private UpdatePostedHandler expectsOneItemWhenCalled;
    private EbayKleinanzeigenJob classUnderTest;
    private final String content = "test-content";
    private final String title = "test-title";
    private final String itemUrl = "test-link";
    private final String imageUrl = "test-image";

    @Test
    public void testMatching() throws Exception {
        final var article = createArticle(content, title, itemUrl, null);
        classUnderTest = Mockito.spy(new EbayKleinanzeigenJob(databaseManager, "", newItems -> {
            assertEquals(1, newItems.size());
            assertEquals(content, newItems.get(0).getContent());
            assertEquals(title, newItems.get(0).getTitle());
            assertEquals(itemUrl, newItems.get(0).getUrl());
            assertEquals(imageUrl, newItems.get(0).getImageUrl());
        }));
        doReturn(new Elements(article)).when(classUnderTest).getArticles();
        classUnderTest.run();
    }

    @Test
    public void testNoNotificationOnFirstRun() throws Exception {
        final var item = ClassifiedItem.builder().content(content).title(title).url(itemUrl).imageUrl(imageUrl).build();
        doReturn(Optional.of(item)).when(databaseManager).findByUrl(anyString());
        classUnderTest = Mockito.spy(new EbayKleinanzeigenJob(databaseManager, "", newItems -> fail("should not have been be called")));
        final var article = createArticle(content, title, itemUrl, imageUrl);
        doReturn(new Elements(article)).when(classUnderTest).getArticles();

        classUnderTest.run();
    }

    @Test
    public void testDoNotifyRun() throws Exception {
        final var itemUrl2 = "test-link2";
        final var content2 = "test-content2";
        final var title2 = "test-title2";
        final var imageUrl2 = "test-image2";


        doReturn(null).when(expectsOneItemWhenCalled).execute(any(SendMessage.class));
        doReturn(null).when(expectsOneItemWhenCalled).execute(any(SendPhoto.class));

        // prepare databasemanager for test
        final var item = ClassifiedItem.builder().content(content).title(title).url(itemUrl).imageUrl(imageUrl).build();
        final var item2 = ClassifiedItem.builder().content(content2).title(title2).url(itemUrl2).imageUrl(imageUrl2).build();
        doReturn(Optional.of(item)).when(databaseManager).findByUrl(eq(itemUrl));

        doReturn(Optional.empty()) // on first try item is not in database
                .doReturn(Optional.of(item2)) // on second try item2 is in database
                .when(databaseManager).findByUrl(eq(itemUrl2));
        doNothing().when(databaseManager).save(any(ClassifiedItem.class));

        // prepare article fetcher+parser for test
        classUnderTest = Mockito.spy(new EbayKleinanzeigenJob(databaseManager, "", willFailWhenCalled));
        final var article = createArticle(content, title, itemUrl, imageUrl);
        final var article2 = createArticle(content2, title2, itemUrl2, imageUrl2);

        doReturn(new Elements(article)) // first run only 1 item
                .doReturn(new Elements(article, article2)) // second run 2 items -> notification is expected
                .when(classUnderTest).getArticles();
        // first run - we expect no notification
        classUnderTest.run();
        verify(willFailWhenCalled,times(0)).execute(any(SendMessage.class));
        verify(willFailWhenCalled,times(0)).execute(any(SendPhoto.class));


        // second run - we expect a notification
        classUnderTest.setCallback(expectsOneItemWhenCalled);
        classUnderTest.run();
        verify(expectsOneItemWhenCalled,times(1)).execute(any(SendMessage.class));
        verify(expectsOneItemWhenCalled,times(1)).execute(any(SendPhoto.class));

    }

    public static Element createArticle(String content, String title, final String link, String image) {
        final var html = TEMPLATE_ARTICLE_HTML.replace("###CONTENT###", String.valueOf(content))
                .replace("###TITLE###", String.valueOf(title))
                .replace("###LINK###", String.valueOf(link))
                .replace("###IMAGE###", String.valueOf(image));
        System.out.println(html);
        final var articleDoc = Jsoup.parse(html);
        return articleDoc.firstElementSibling().select("article").first();
    }

    private final static String TEMPLATE_ARTICLE_HTML = """
            <article class="aditem" data-adid="2342632528" data-href="/s-anzeige/exklusive-vollstaendig-renovierte-3-zimmer-wohnung-balkon-in-koeln/2342632528-203-26204" style="cursor: pointer;">
            <div class="aditem-image">
                <a href="###LINK###">
                                    <div class="imagebox srpimagebox" data-imgsrc="###IMAGE###" data-imgsrcretina="https://img.ebay-kleinanzeigen.de/api/v1/prod-ads/images/6b/6ba397d7-a36d-4422-8294-b66940aaa8fe?rule=$_35.JPG 2x" data-imgtitle="Exklusive, vollständig renovierte 3-Zimmer-Wohnung Balkon in Köln Köln - Longerich Vorschau">
                                        <img src="https://img.ebay-kleinanzeigen.de/api/v1/prod-ads/images/6b/6ba397d7-a36d-4422-8294-b66940aaa8fe?rule=$_2.JPG" srcset="https://img.ebay-kleinanzeigen.de/api/v1/prod-ads/images/6b/6ba397d7-a36d-4422-8294-b66940aaa8fe?rule=$_35.JPG 2x" alt="Exklusive, vollständig renovierte 3-Zimmer-Wohnung Balkon in Köln Köln - Longerich Vorschau"></div>
                                </a>
                            </div>
            <div class="aditem-main">
                <div class="aditem-main--top">
                    <div class="aditem-main--top--left">
                        <i class="icon icon-small icon-pin"></i> 50737 Longerich</div>
                    <div class="aditem-main--top--right">
                        <i class="icon icon-small icon-calendar-open"></i>
                            Gestern, 21:53</div>
                </div>
                <div class="aditem-main--middle">
                    <h2 class="text-module-begin">
                        <a class="ellipsis" href="/s-anzeige/exklusive-vollstaendig-renovierte-3-zimmer-wohnung-balkon-in-koeln/2342632528-203-26204">###TITLE###</a>
                    </h2>
                    <p class="aditem-main--middle--description">###CONTENT###</p>
                    <div class="aditem-main--middle--price-shipping">
                        <p class="aditem-main--middle--price-shipping--price">
                            900 €</p>
                        </div>
                <div class="aditem-main--bottom">
                    <p class="text-module-end">
                        <span class="simpletag tag-small">69 m²</span>
                        <span class="simpletag tag-small">3 Zimmer</span>
                        </p>
                    </div>
            </div>
            </div></article>
            """;
}
