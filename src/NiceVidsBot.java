import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by EA on 27.08.2016.
 */

public class NiceVidsBot extends TelegramLongPollingBot {
    private static final String BOT_USERNAME = "NiceVidsBot";
    private static final String BOT_TOKEN = "OOPPSS";

    private final String GET_VIDEO = "Give me an awesome video!";
    private final String GET_VIDEO2 = "/find";
    private final String START = "/start";
    private final String HELP = "/help";
    private final int startYear = 2012;
    private final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    private final int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
    private final int TIMEOUT = 5000;

    private int[] YEARS;

    public NiceVidsBot() {
        int size = currentYear - startYear + 1;
        int year = currentYear;
        YEARS = new int[size];
        for (int i = 0; i < size; ++i) {
            YEARS[i] = year--;
        }
    }

    public String getBotUsername() {
        return BOT_USERNAME;
    }

    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void onUpdateReceived(Update update) {
        //check if the update has a message
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.hasText()) {
                SendMessage sendMessageRequest = handleMessage(message);

                try {
                    sendMessage(sendMessageRequest); //at the end, so some magic and send the message ;)
                } catch (TelegramApiException e) {
                    //do some error handling
                }
            }
        }
    }

    private SendMessage handleMessage(Message message) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(message.getChatId().toString());
        sendMessageRequest.enableMarkdown(true);
        sendMessageRequest.setReplyMarkup(getKeyboard());

        String text = message.getText();

        switch (text) {
            case HELP:
            case START:
                sendMessageRequest.setText("Hello!\n" +
                        "This is a simple bot that finds some cool " +
                        "videos from Youtube.\nTo get a video, simply choose '" +
                        GET_VIDEO + "' or type " + GET_VIDEO2 + ".\n" + HELP +
                        " is always here.");
                break;
            case GET_VIDEO:
            case GET_VIDEO2:
                sendMessageRequest.setText("Here is a video:\n" + getVideo());
                break;
            default:
                sendMessageRequest.setReplyToMessageId(message.getMessageId());
                sendMessageRequest.setText("Sorry I can't answer that.");
                break;
        }

        return sendMessageRequest;
    }

    private String getVideo() {
        int year = YEARS[new Random().nextInt(YEARS.length)];

        int month;
        if (year == startYear)
            month = 3 + new Random().nextInt(10);
        else if (year == currentYear)
            month = new Random().nextInt(currentMonth);
        else
            month = new Random().nextInt(13);

        String archiveLink = "http://unplugthetv.com/archive/" +
                year + "/" + month;

        String videoPage;
        String resultLink = "";
        try {
            Document doc = Jsoup.connect(archiveLink).timeout(TIMEOUT)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true).get();

            if (doc != null){
                Elements posts =
                        doc.select("a[href*=http://unplugthetv.com/post/");
                Element randomElement =
                        posts.get(new Random().nextInt(posts.size()));

                videoPage = randomElement.attr("abs:href");
                doc = Jsoup.connect(videoPage).timeout(TIMEOUT)
                        .ignoreContentType(true)
                        .ignoreHttpErrors(true).get();
                if (doc != null){
                    Element srcDiv = doc.select("div[class=media]").first();
                    String src = srcDiv.toString();
                    String s1 = " src=\"";
                    String s2 = "\" ";
                    Pattern p = Pattern.compile(s1 + "(.*?)" + s2);
                    Matcher m = p.matcher(src);
                    String embedString;
                    if (m.find()){
                        embedString = m.group(1);
                        s1 = "embed/";
                        s2 = "\\?feature=";
                        p = Pattern.compile(s1 + "(.*?)" + s2);
                        m = p.matcher(embedString);
                        if (m.find())
                            resultLink = "https://www.youtube.com/watch?v=" +
                                    m.group(1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        if (resultLink.equals(""))
            resultLink = "Sorry, some error here. Could you try again?";

        return resultLink;
    }

    private ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboad(false);

        List<KeyboardRow> keyboard = new ArrayList();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(GET_VIDEO);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }
}
