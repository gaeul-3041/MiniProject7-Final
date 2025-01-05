package com.aivle.mini7.dto;

import com.aivle.mini7.model.Board;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDto {
    private Long board_id;
    private String author_id;
    private String title;
    private String content;
    private String post_type;
    private String created_time;

    public BoardDto(Board board) {
        this.board_id = board.getBoard_id();
        this.author_id = board.getAuthor_id();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.post_type = board.getPost_type();
        this.created_time = board.getCreated_time();
    }
}
