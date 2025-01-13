package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/comments")
@Slf4j
@Validated
public class UserCommentController {

    private final CommentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComments(
            @PathVariable Long userId,
            @RequestParam @Positive Long eventId,
            @RequestBody @Validated CommentDto commentDto) throws ConflictException, NotFoundException {
        return service.addComment(commentDto, userId, eventId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @NonNull Long commentId,
            @PathVariable @NonNull Long userId) throws ConflictException, NotFoundException {
        service.delete(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentDto commentDto) throws ConflictException, NotFoundException {
        return service.updateUserComment(userId, commentId, commentDto);
    }

    @GetMapping
    public Collection<CommentDto> getByUserComment(@PathVariable Long userId) throws NotFoundException {
        return service.getAllUserComments(userId);
    }
}