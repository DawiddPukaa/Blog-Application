package pl.kul.blog.infrastructure.api.comments;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kul.blog.domain.comment.Comment;
import pl.kul.blog.domain.comment.CommentPublisher;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/api/comments")
@AllArgsConstructor
public class CommentsEndpoint {
    private final CommentPublisher commentPublisher;

    @PutMapping(path = "/{commentId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    PublishCommentResponse publishComment(
        @PathVariable("commentId") UUID commentId,
        @RequestBody PublishCommentRequest request
    ) {
        Comment publishComment = commentPublisher.publishComment(request.toDomain(commentId));
//
        return PublishCommentResponse.fromDomain(publishComment);
    }
}
