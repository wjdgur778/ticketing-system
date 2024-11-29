package com.example.ticketing.api.contents;

import com.example.ticketing.api.contents.dto.ContentsRequest;
import com.example.ticketing.api.contents.dto.ContentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentsService {
    final private ContentsRepository contentsRepository;

    //redis의 캐싱을 활용하여 컨탠츠를 조회하자;
    //allContents로 고정키값 설정
    //
    //고정키값 설정할때 ' ' 를 붙여야한다는점.
    //
    @Transactional
    @Cacheable(
            value = "contentsCache",
            key = "'allContents'"
    )
    public List<ContentsResponse> getContents() {
        return contentsRepository.findAll().stream()
                .map(contents -> ContentsResponse.from(contents))
                .collect(Collectors.toList());
    }


}

