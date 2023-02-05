package com.keyboardsamurais.apps;

import com.coreoz.wisp.Scheduler;
import com.coreoz.wisp.schedule.Schedules;
import com.coreoz.wisp.schedule.cron.CronExpressionSchedule;
import com.keyboardsamurais.apps.config.EnvUtils;
import com.keyboardsamurais.apps.db.DatabaseManager;
import com.keyboardsamurais.apps.jobs.DatabaseCleanupJob;
import com.keyboardsamurais.apps.jobs.EbayKleinanzeigenJob;
import com.keyboardsamurais.apps.notification.UpdatePostedHandler;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;

@Slf4j
public class CallingHomeApp {
    private static UpdatePostedHandler updatePostedHandler;

    public static void main(String[] args) {
        log.info("Starting CallingHomeApp");

        try {
            var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            updatePostedHandler = new UpdatePostedHandler();
            telegramBotsApi.registerBot(updatePostedHandler);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        new CallingHomeApp().run();

    }

    private void run() {
        var databaseManager = new DatabaseManager();
        var scheduler = new Scheduler();

        // set up ebay kleinanzeigen job
        final var url = Objects.requireNonNull(EnvUtils.getEnv("EBAY_KLEINANZEIGEN_URL"));
        log.info("now monitoring url: {}", url);
        final Runnable ebayKleinanzeigenJob = new EbayKleinanzeigenJob(databaseManager, url, updatePostedHandler);
        ebayKleinanzeigenJob.run(); // run once at startup, then wait for the scheduler to run it again
        scheduler.schedule(ebayKleinanzeigenJob,Schedules.fixedDelaySchedule(Duration.ofMinutes(Integer.parseInt(EnvUtils.getEnv("INTERVAL")))));
        // scheduler.findJob().get().schedule().nextExecutionInMillis()

        // set up database cleanup job and run at 00:00 every day
        scheduler.schedule(new DatabaseCleanupJob(databaseManager), CronExpressionSchedule.parse("0 0 * * *"));
    }

}
