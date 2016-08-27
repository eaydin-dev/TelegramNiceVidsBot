import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * Created by EA on 27.08.2016.
 */
public class Main {

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new NiceVidsBot());
        } catch (TelegramApiException e) {
            BotLogger.error("errorr", e);
        }
    }

}
