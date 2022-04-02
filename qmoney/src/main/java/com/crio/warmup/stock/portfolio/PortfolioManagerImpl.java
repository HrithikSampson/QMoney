
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;



class CandleComparator implements Comparator<Candle> {
  public int compare(Candle c1, Candle c2) {
    return (c1.getDate()).isAfter(c2.getDate()) ? 1 : -1;
  }
}


public class PortfolioManagerImpl implements PortfolioManager {



  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  

  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF
  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate = new RestTemplate();
    List<String> l = new ArrayList<>();

      l.add(trade.getSymbol());
      ResponseEntity<List<TiingoCandle>> tiingoCandles = restTemplate.exchange(
          "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?endDate=" + endDate + "&startDate="
              + trade.getPurchaseDate().toString() + "&token="+token,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<TiingoCandle>>(){});
      List<Candle> c = new ArrayList<>();      
        for(TiingoCandle t:tiingoCandles.getBody()){
          c.add(t);
      }
      return c;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    Collections.sort(candles, new CandleComparator());
    return candles.get(0).getOpen();
  }

  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    Collections.sort(candles, new CandleComparator());
    return candles.get(candles.size()-1).getClose();
  }
  
  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice,
    Double sellPrice) {

    Double totalReturn = (sellPrice - buyPrice) / buyPrice;
    Double annualized_returns = Math.pow((1 + totalReturn),(1 / (double)((endDate.getYear() - trade.getPurchaseDate().getYear())+((double)endDate.getDayOfYear()-(double)trade.getPurchaseDate().getDayOfYear())/365))) - 1;
    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
        String uri = buildUri(symbol, from, to);
    List<String> l = new ArrayList<>();

      l.add(symbol);
      ResponseEntity<List<TiingoCandle>> tiingoCandles = this.restTemplate.exchange(
              uri,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<TiingoCandle>>(){});
      List<Candle> c = new ArrayList<>();      
        for(TiingoCandle t:tiingoCandles.getBody()){
          c.add(t);
      }
      return c;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https:api.tiingo.com/tiingo/daily/"+symbol+"/prices?"
            + "startDate="+startDate+"&endDate="+endDate+"&token="+PortfolioManagerImpl.getToken();
        return uriTemplate;
  }

  public static String getToken(){
    return "b463a2ddf6e532d3e9f804c5a10c981da3a21336";
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws JsonProcessingException {
    // TODO Auto-generated method stub
    String token = getToken();
    List<AnnualizedReturn> l = new ArrayList<>();
    for(PortfolioTrade trade: portfolioTrades){
      List<Candle> c = getStockQuote(trade.getSymbol(), trade.getPurchaseDate() , endDate);
      Double buyPrice = PortfolioManagerImpl.getOpeningPriceOnStartDate(c);
      Double sellPrice = PortfolioManagerImpl.getClosingPriceOnEndDate(c);
      AnnualizedReturn a = PortfolioManagerImpl.calculateAnnualizedReturns(endDate, trade, buyPrice, sellPrice);
      l.add(a);
    }
    Collections.sort(l,getComparator());
    return l;
  }
}
