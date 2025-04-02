package itstep.learning.orm;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrencyRateNbu {
    private String txt;          // Название валюты (например, "Долар США")
    private String cc;           // Код валюты (например, "USD")
    private double rate;         // Курс (например, 27.3)
    private String exchangedate; // Дата обновления (напри

    // Геттеры и сеттеры
    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getExchangedate() {
        return exchangedate;
    }

    public void setExchangedate(String exchangedate) {
        this.exchangedate = exchangedate;
    }

    // Статический метод для парсинга JSON в объект CurrencyRateNbu
    public static CurrencyRateNbu fromJsonObject(JSONObject obj) {
        CurrencyRateNbu rate = new CurrencyRateNbu();
        rate.setTxt(obj.optString("txt"));
        rate.setCc(obj.optString("cc"));
        rate.setRate(obj.optDouble("rate"));
        rate.setExchangedate(obj.optString("exchangedate"));
        return rate;
    }
}