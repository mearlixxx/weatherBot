package mearlixxxbot.weatherBot.service;


import mearlixxxbot.weatherBot.config.BotConfig;
import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatLocation;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startMessageRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;
                default:
                    btnPress(messageText, chatId);
                    break;

            }
        }
    }

    private void startMessageRecieved(long chatId, String firstName) {
        String answer = firstName + ", здравствуйте! Введите название города, в котором хотите узнать погоду.";

        sendMessage(chatId, answer);
    }

    private String getUrlContent(String urlAddress, long chatId) {
        StringBuffer content = new StringBuffer();

        try {
            URL url = new URL(urlAddress);
            URLConnection urlConnection = url.openConnection();

            BufferedReader bf = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            while ((line = bf.readLine()) != null) {
                content.append(line + "\n");
            }
            bf.close();
        } catch (Exception e) {
            //noCityFound(chatId);
        }
        return content.toString();
    }

    /*@Bean
    public ReplyKeyboardMarkup replyKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        //следующие три строчки могут менять значение аргументов взависимости от ваших задач
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        //добавляем "клавиатуру"
        replyKeyboardMarkup.setKeyboard(keyboardRows());

        return replyKeyboardMarkup;
    }

    @Bean
    public List<KeyboardRow> keyboardRows() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(keyboardButtons()));
        //создаем список рядов кнопок из списка кнопок

        return rows;
    }

    @Bean
    public List<KeyboardButton> keyboardButtons() {
        List<KeyboardButton> buttons = new ArrayList<>();
        buttons.add(new KeyboardButton("Москва"));
        //создаем и заполняем список кнопок
        return buttons;
    }*/

    private void btnPress(String userCity, long chatId) {
        String getUserCity = userCity;
        String output = getUrlContent("http://api.openweathermap.org/data/2.5/weather?q=" + getUserCity + "&appid=9c6beb1f25ad48983d68ea7f2808a25d&units=metric", chatId);

        String temp_info = null;
        String temp_feels = null;
        String temp_max = null;
        String temp_min = null;
        String pressure = null;

        if (!output.isEmpty()) { // Нет ошибки и такой город есть
            JSONObject obj = new JSONObject(output);
            // Обрабатываем JSON и устанавливаем данные в текстовые надписи
            temp_info = "Температура: " + obj.getJSONObject("main").getInt("temp") + " градусов";
            temp_feels = "Ощущается: " + obj.getJSONObject("main").getInt("feels_like") + " градусов";
            temp_max = "Максимум: " + obj.getJSONObject("main").getInt("temp_max") + " градусов";
            temp_min = "Минимум: " + obj.getJSONObject("main").getInt("temp_min") + " градусов";
            pressure = "Давление: " + obj.getJSONObject("main").getInt("pressure") + " мм рт. ст.";
            String answer = temp_info + "\n" + temp_feels + "\n" + temp_max + "\n" + temp_min + "\n" + pressure + "\n" + "Если хотите узнать, какая погода в другом городе, введите название этого города. ";
            sendMessage(chatId, answer);
        } else {
            noCityFound(chatId);
        }
    }

    private void noCityFound(long chatId) {
        String message = "Город не был найден!";
        sendMessage(chatId, message);
    }

    private void sendMessage(long chatId, String answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(answer);

        try {
            //sendMessage.setReplyMarkup(replyKeyboardMarkup());
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
