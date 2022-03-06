
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.crio.warmup.stock.dto.PortfolioTrade;
import java.io.File;
class CloseComparator implements Comparator<Candle>{
  public int compare(Candle c1,Candle c2){
    return (c1.getClose()>c2.getClose())?1:-1;
  }
}

public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Task:
  //       - Read the json file provided in the argument[0], The file is available in the classpath.
  //       - Go through all of the trades in the given file,
  //       - Prepare the list of all symbols a portfolio has.
  //       - if "trades.json" has trades like
  //         [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  //         Then you should return ["MSFT", "AAPL", "GOOGL"]
  //  Hints:
  //    1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  //       Check if they are of any help to you.
  //    2. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    ObjectMapper om = new ObjectMapper();
    ClassLoader loader = PortfolioManagerApplication.class.getClassLoader();
    PortfolioTrade[] pt = om.readValue(loader.getResource(args[0]), PortfolioTrade[].class);
    List<String> l = new ArrayList<>();
    for(int idx = 0;idx < pt.length; idx++){
      l.add(pt[idx].getSymbol());
    }
    System.out.println(l);
    return l;

  }


  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.






  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/hrithikedwardsampson-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "1";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException, RestClientException, RuntimeException {
      RestTemplate restTemplate = new RestTemplate();
      ObjectMapper om = new ObjectMapper();
    ClassLoader loader = PortfolioManagerApplication.class.getClassLoader();
    PortfolioTrade[] pt = om.readValue(loader.getResource(args[0]), PortfolioTrade[].class);
    List<String> l = new ArrayList<>();
    HashMap<String,Double> mapList = new HashMap<>();
    
    for(int idx = 0;idx < pt.length; idx++){
      l.add(pt[idx].getSymbol());
      TiingoCandle[] tiingoCandles = restTemplate.getForObject("https://api.tiingo.com/tiingo/daily/"+pt[idx].getSymbol()+"/prices?endDate="+args[1]+"&startDate="+pt[idx].getPurchaseDate().toString()+"&token=b463a2ddf6e532d3e9f804c5a10c981da3a21336",
      TiingoCandle[].class);
      mapList.put(pt[idx].getSymbol(),tiingoCandles[tiingoCandles.length-1].getClose());
    }
    List<Map.Entry<String, Double>> map = new LinkedList<>(mapList.entrySet());
    class C1 implements Comparator<Map.Entry<String,Double>>{
        public int compare(Map.Entry<String,Double> m1,Map.Entry<String,Double> m2){
          return m1.getValue()>m2.getValue()?1:-1;
        }
      
    }
    Collections.sort(map,new C1());
    List<String> symboList = new ArrayList<String>();
    for(Map.Entry<String,Double> m: map){
      symboList.add(m.getKey());
    }
      
     return symboList;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    ObjectMapper om = new ObjectMapper();
    ClassLoader loader = PortfolioManagerApplication.class.getClassLoader();
    List<PortfolioTrade> pt = om.readValue(loader.getResource(filename), new TypeReference<List<PortfolioTrade>>(){
    });

     return pt;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     return "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?"+"startDate="+trade.getPurchaseDate().toString()+"&endDate="+endDate+"&token="+token;
  }










  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainReadQuotes(args));


  }
}

