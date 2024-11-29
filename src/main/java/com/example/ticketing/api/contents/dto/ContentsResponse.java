package com.example.ticketing.api.contents.dto;

import com.example.ticketing.api.contents.Contents;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ContentsResponse {
    long id;
    String title;
    String description;

    public ContentsResponse(long id,String title,String description){
        this.id =id;
        this.title =title;
        this.description =description;
    }

//  정적 팩토리 메서드를 활용하여 Contents to Response
    public static ContentsResponse from(Contents contents){
        return new ContentsResponse(contents.getId(),contents.getTitle(),contents.getDescription());
    }
}
