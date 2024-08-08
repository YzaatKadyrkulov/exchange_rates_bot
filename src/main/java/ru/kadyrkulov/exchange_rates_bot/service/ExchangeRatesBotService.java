package ru.kadyrkulov.exchange_rates_bot.service;

import ru.kadyrkulov.exchange_rates_bot.exception.ServiceException;

public interface ExchangeRatesBotService {
    String getUSDExchangeRate() throws ServiceException;

    String getEURExchangeRate() throws ServiceException;
}
