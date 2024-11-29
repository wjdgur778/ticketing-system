package com.example.ticketing.api.contents.dto;

import com.example.ticketing.api.contents.Contents;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ContentsRequest {
    String title;
    String description;

    public ContentsRequest(String title,String description){
        this.title =title;
        this.description =description;
    }

    //  정적 팩토리 메서드를 활용하여 Contents to Response
    public static ContentsRequest from(Contents contents){
        return new ContentsRequest(contents.getTitle(),contents.getDescription());
    }
}
