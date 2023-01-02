package zerobase.weather.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.WeatherApplication;
import zerobase.weather.domain.DateWeather;
import zerobase.weather.domain.Diary;
import zerobase.weather.repository.DateWeatherRepository;
import zerobase.weather.repository.DiaryRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private static final String CURRENT_CITY_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=";

    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherApplication.class);

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매 새벽 1시마다 동작
    public void saveWeatherDate() {
        logger.info("오늘도 날씨 데이터 잘 가져옴.");
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi() {
        // open weather map에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱하기
        Map<String, Object> parsedWeather = parseWeather(weatherData);

        return DateWeather.builder()
            .date(LocalDate.now())
            .weather(parsedWeather.get("main").toString())
            .icon(parsedWeather.get("icon").toString())
            .temperature((double) parsedWeather.get("temp"))
            .build();
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, readOnly = false)
    public void createDiary(LocalDate date, String text) {
//        // open weather map에서 날씨 데이터 가져오기
//        String weatherData = getWeatherString();
//
//        // 받아온 날씨 json 파싱하기
//        Map<String, Object> parsedWeather = parseWeather(weatherData);

        logger.info("started to create diary");

        // 날씨 데이터 가져오기 (API에서 가져오기? or DB에서 가져오기?)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값 DB 저장
        Diary newDiary = new Diary();
//            .weather(parsedWeather.get("main").toString())
//            .icon(parsedWeather.get("icon").toString())
//            .temperature((Double) parsedWeather.get("temp"))

        newDiary.setDateWeather(dateWeather);
        newDiary.setText(text);

        this.diaryRepository.save(newDiary);
        logger.info("end to create diary");
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDb = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDb.size() == 0) {
            // 새로 api에서 날씨 정보를 가져와야 한다.
            // 정책상,,, 현재 날씨를 가져오도록 하거나, 날씨 없이 일기를 쓰도록,,,
            return getWeatherFromApi();
        }

        return dateWeatherListFromDb.get(0);
    }

    /**
     * 날짜 기반 날씨 일기 데이터 조회
     *
     * @param date
     * @return
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("read diary");
        return this.diaryRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return this.diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    @Transactional(readOnly = false)
    public void updateDiary(LocalDate date, String text) {
        logger.info("다이어리 업데이트 : " + text);
        Diary nowDiary = this.diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);

        this.diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        this.diaryRepository.deleteAllByDate(date);
    }

    private String getWeatherString() {
        String apiUrl = CURRENT_CITY_WEATHER_URL + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            BufferedReader br;

            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }

            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
            ;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherArray.get(0);

        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }
}
