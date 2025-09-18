package io.notfound.counsel_back.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private String content;
    private String writerName; // [추가] 익명 작성자 이름
    private String password;   // [추가] 익명 댓글 수정/삭제용 비밀번호
}