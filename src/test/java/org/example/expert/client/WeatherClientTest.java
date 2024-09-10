package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class WeatherClientTest {

    private WeatherClient weatherClient;
    @Mock
    private RestTemplateBuilder restTemplateBuilder;
    @Mock
    private RestTemplate restTemplate;


    @BeforeEach
    public void setUp() {
        // WeatherClient의 생성자에서 RestTemplateBuilder가 초기화 되지 않아 RestTemplate이 null이 되어 생기는 문제를 해결하기 위해 추가한 코드
        openMocks(this); // mock에서 라이브러리 객체 초기화하는데 사용하는 것
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    public void getTodayWeather_날씨_데이터_가져오기_실패() {
        // given
        String str = "날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + HttpStatus.INTERNAL_SERVER_ERROR;
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.INTERNAL_SERVER_ERROR);

        given(restTemplate.getForEntity(
                UriComponentsBuilder
                        .fromUriString("https://f-api.github.io/f-api/weather.json")  // 절대 URI로 수정
                        .encode()
                        .build()
                        .toUri(), WeatherDto[].class))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());

        // then
        assertEquals(str , exception.getMessage());
    }

    @Test
    public void getTodayWeather_날씨_데이터_없음() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK);

        given(restTemplate.getForEntity(
                UriComponentsBuilder
                        .fromUriString("https://f-api.github.io/f-api/weather.json")  // 절대 URI로 수정
                        .encode()
                        .build()
                        .toUri(), WeatherDto[].class))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());

        // then
        assertEquals("날씨 데이터가 없습니다." , exception.getMessage());
    }

    @Test
    public void getTodayWeather_날짜_반환() {
        // given
        WeatherDto[] weatherDtoList = {
                new WeatherDto(LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd")), "날씨")
        };

        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherDtoList, HttpStatus.OK);

        given(restTemplate.getForEntity(
                UriComponentsBuilder
                        .fromUriString("https://f-api.github.io/f-api/weather.json")  // 절대 URI로 수정
                        .encode()
                        .build()
                        .toUri(), WeatherDto[].class))
                .willReturn(responseEntity);

        // when
        String str = weatherClient.getTodayWeather();

        // then
        assertEquals(weatherDtoList[0].getWeather(), str);
    }

    @Test
    public void getTodayWeather_오늘_날짜_없음() {
        // given
        WeatherDto[] weatherDtoList = {
                new WeatherDto(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("MM-dd")), "날씨")
        };

        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherDtoList, HttpStatus.OK);

        given(restTemplate.getForEntity(
                UriComponentsBuilder
                        .fromUriString("https://f-api.github.io/f-api/weather.json")  // 절대 URI로 수정
                        .encode()
                        .build()
                        .toUri(), WeatherDto[].class))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () -> weatherClient.getTodayWeather());


        // then
        assertEquals("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다." , exception.getMessage());
    }
}
