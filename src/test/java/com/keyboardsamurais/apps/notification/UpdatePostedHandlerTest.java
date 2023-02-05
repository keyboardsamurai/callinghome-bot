package com.keyboardsamurais.apps.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

/**
 * @author Antonio Agudo on 01.02.23
 */
class UpdatePostedHandlerTest {
    @Mock
    HashSet<String> denyList;
    @Mock
    Logger log;
    @Mock
    ExecutorService exe;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    DefaultBotOptions options;
    @Mock
    CloseableHttpClient httpClient;
    @Mock
    RequestConfig requestConfig;
    @Mock
    TelegramFileDownloader telegramFileDownloader;
    @InjectMocks @Spy
    UpdatePostedHandler updatePostedHandler;
    private ClassifiedItem positiveItem;
    private ClassifiedItem negativeItem;

    @BeforeEach
    void setUp() throws TelegramApiException {
        MockitoAnnotations.openMocks(this);
        doReturn(null).when(updatePostedHandler).execute(any(SendMessage.class));
        positiveItem = ClassifiedItem.builder().title("aaa").content("bbb").url("http://localhost/1").created(Instant.now()).build();
        negativeItem = ClassifiedItem.builder().title("Tauschwohnung").content("bbb").url("http://localhost/1").created(Instant.now()).build();
    }

    @Test @SetEnvironmentVariable(key = "TELEGRAM_BOT_NAME", value = "MyBot")
    void testGetBotUsername() {
        String result = updatePostedHandler.getBotUsername();
        Assertions.assertEquals("MyBot", result);
    }

    @Test @SetEnvironmentVariable(key = "TELEGRAM_BOT_TOKEN", value = "MY_TELEGRAM_BOT_TOKEN")
    void testGetBotToken() {
        String result = updatePostedHandler.getBotToken();
        Assertions.assertEquals("MY_TELEGRAM_BOT_TOKEN", result);
    }

    @Test
    void testOnRegister() {
        updatePostedHandler.onRegister();
    }

    @Test
    void testOnUpdateReceived() {
    }

    @Test
    void testOnFinished_SingleMessage_Positive() throws Exception {
        updatePostedHandler.onFinished(List.of(positiveItem));
        verify(updatePostedHandler, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testOnFinished_SingleMessage_Positive_WithPhoto() throws Exception {
        doReturn(null).when(updatePostedHandler).execute(any(SendPhoto.class));
        positiveItem.setImageUrl("http://localhost/1.jpg");
        updatePostedHandler.onFinished(List.of(positiveItem));
        verify(updatePostedHandler, times(1)).execute(any(SendPhoto.class));
        verify(updatePostedHandler, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testOnFinished_SingleMessage_Negative_Title() throws Exception {
        updatePostedHandler.onFinished(List.of(negativeItem));
        verify(updatePostedHandler, times(0)).execute(any(SendMessage.class));
    }

    @Test
    void testOnFinished_SingleMessage_Negative_Content() throws Exception {
        negativeItem.setTitle("aaa");
        negativeItem.setContent("Tauschwohnung");
        updatePostedHandler.onFinished(List.of(negativeItem));
        verify(updatePostedHandler, times(0)).execute(any(SendMessage.class));
    }
    @Test
    void testOnFinished_SingleMessage_Negative_Content2() throws Exception {
        negativeItem.setTitle("Familiengerechtes Wohnen in Porz Eil");
        negativeItem.setContent("# Objektbeschreibung Die Wohnung für Jedermann! Wir bieten Ihnen hier eine praktisch geschnittene...");
        updatePostedHandler.onFinished(List.of(negativeItem));
        verify(updatePostedHandler, times(0)).execute(any(SendMessage.class));
    }

}
