package io.notfound.counsel_back.board.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    @NotBlank(message = "작성자 이름은 필수 입력값입니다.")
    private String writerName;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
}