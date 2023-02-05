package com.keyboardsamurais.apps.notification;

import com.keyboardsamurais.apps.config.EnvUtils;
import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import com.keyboardsamurais.apps.jobs.FinishedJobCallback;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Antonio Agudo  on 27.01.23
 */
@Slf4j
public class UpdatePostedHandler extends TelegramLongPollingBot implements FinishedJobCallback {
    private final String telegramChatId;
    private final HashSet<String> denyList;

    public UpdatePostedHandler() {

        log.warn("Telegram bot name: {} starting up now at {}", EnvUtils.getEnv("TELEGRAM_BOT_NAME"), ZonedDateTime.now());
        this.telegramChatId = EnvUtils.getEnv("CHAT_ID");
        log.info("Telegram chat id: {}", telegramChatId);

        final var negatives = EnvUtils.getEnv("DENY_LIST").split(",");
        denyList = new HashSet<>(Arrays.asList(negatives));
        log.info("Deny list: {}",String.join(", ", denyList));
    }
    @Override
    public String getBotUsername() {
        return EnvUtils.getEnv("TELEGRAM_BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return EnvUtils.getEnv("TELEGRAM_BOT_TOKEN");
    }


    @Override
    public void onRegister() {
        log.info("Registered");
    }

    @Override
    public void onUpdateReceived(final Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            final var chatId = update.getMessage().getChatId();
            log.info("Sending out Telegram message to chat id:{} ", chatId);
            var message = SendMessage.builder().chatId(chatId.toString()).text(update.getMessage().getText()).build();
            message.setChatId(chatId.toString());
            message.setText(update.getMessage().getText());
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onFinished(final List<ClassifiedItem> newItems) {
        if(newItems==null || newItems.isEmpty()) {
            return;
        }

        final var relevantItems = newItems.stream()
                .filter(classifiedItem -> denyList.stream().noneMatch(s -> classifiedItem.getContent().toLowerCase().contains(s)))
                .filter(classifiedItem -> denyList.stream().noneMatch(s1 -> classifiedItem.getTitle().toLowerCase().contains(s1)))
                .toList();

        relevantItems.forEach(classifiedItem -> {
            try {
                // send a photo if there is one
                if(classifiedItem.getImageUrl() != null && !classifiedItem.getImageUrl().isBlank()) {
                    var sendImage = SendPhoto.builder().photo(new InputFile(classifiedItem.getImageUrl())).chatId(telegramChatId).build();
                    this.execute(sendImage);
                }

                // send a telegram message with the title as text and the link as url and image as image
                final var message = String.format("%s\n%s\n%s", classifiedItem.getTitle(), classifiedItem.getContent(), classifiedItem.getUrl());
                var sendMessage = SendMessage.builder().chatId(telegramChatId).text(message).build();
                this.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
