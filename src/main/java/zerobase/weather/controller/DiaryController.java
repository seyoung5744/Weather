package zerobase.weather.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zerobase.weather.domain.Diary;
import zerobase.weather.service.DiaryService;

@RestController
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @ApiOperation(value = "일기 텍스트와 날씨를 이용해서 DB에 일기를 저장", notes = "이것은 노트.")
    @PostMapping("/create/diary")
    public void createDiary(
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date,
        @RequestBody String text
    ) {
        this.diaryService.createDiary(date, text);
    }

    @ApiOperation("선택한 날짜의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diary")
    public List<Diary> readDiary(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        return this.diaryService.readDiary(date);
    }

    @ApiOperation("선택한 기간 중의 모든 일기 데이터를 가져옵니다.")
    @GetMapping("/read/diaries")
    public List<Diary> readDiaries(
        @RequestParam @DateTimeFormat(iso = ISO.DATE) @ApiParam(value = "조회할 기간의 첫번째 날 : yyyy-MM-dd", example = "2020-01-20") LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = ISO.DATE) @ApiParam(value = "조회할 기간의 마지막 날 : yyyy-MM-dd", example = "2020-01-20") LocalDate endDate
    ) {
        return this.diaryService.readDiaries(startDate, endDate);
    }

    @PutMapping("/update/diary")
    public void updateDiary(
        @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date,
        @RequestBody String text
    ) {
        this.diaryService.updateDiary(date, text);
    }

    @DeleteMapping("/delete/diary")
    public void deleteDiary(@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date){
        this.diaryService.deleteDiary(date);
    }
}
