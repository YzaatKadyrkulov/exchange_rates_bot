package ru.kadyrkulov.exchange_rates_bot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.kadyrkulov.exchange_rates_bot.exception.ServiceException;
import ru.kadyrkulov.exchange_rates_bot.service.ExchangeRatesBotService;

import java.time.LocalDate;

@Component
public class ExchangeRatesBot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesBot.class);

    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String HELP = "/help";

    @Autowired
    private ExchangeRatesBotService exchangeRatesBotService;

    public ExchangeRatesBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();

        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case USD -> usdCommand(chatId);
            case EUR -> eurCommand(chatId);
            case HELP -> helpCommand(chatId);
            default -> unknownCommand(chatId);

        }
    }

    private void helpCommand(Long chatId) {
        var text = """
                Фон информации о боте.
                
                Используйте команды, чтобы получить текущие обменные курсы:
                /usd - курс доллара
                /eur - курс евро
                """;
        sendMessage(chatId, text);
    }

    @Override
    public String getBotUsername() {
        return "kadyrkulov_er_bot";
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s
                ___________________________________
                Автор: Кадыркулов Ызаат
                Специалист в области информационных технологий
                и программирования.
                ___________________________________

                Здесь вы можете ознакомиться с официальными курсами, установленными Центральным банком Российской Федерации на сегодняшний день.

                Для этого используйте команды:

                /usd - курс доллара
                /eur - курс евро

                Дополнительные команды:
                /help - получение помощи
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void usdCommand(Long chatId) {
        String formattedText;
        try {
            var usd = exchangeRatesBotService.getUSDExchangeRate();
            var text1 = "Курс доллара на %s составляет %s рублей. ";
            formattedText = String.format(text1, LocalDate.now(), usd);

        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса доллара.", e);
            formattedText = "Не удалось получить текущий курс доллара. Попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }

    private void eurCommand(Long chatId) {
        String formattedText;
        try {
            var eur = exchangeRatesBotService.getEURExchangeRate();
            var text = "Курс евро на %s составляет %s рублей.";
            formattedText = String.format(text, LocalDate.now(), eur);

        } catch (ServiceException e) {
            LOG.error("Ошибка получения курса евро.", e);
            formattedText = "Не удалось получить текущий курс евро. Попробуйте позже";
        }
        sendMessage(chatId, formattedText);
    }

    private void unknownCommand(Long chatId) {
        var text = "Напишите правильную команду!";
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);

        } catch (TelegramApiException e) {
            LOG.error("Произошла ошибка при отправке сообщения", e);
        }
    }
}
