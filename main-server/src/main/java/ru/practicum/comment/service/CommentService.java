package ru.practicum.comment.service;

import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

public interface CommentService {

    CommentDto addComment(CommentDto commentDto, Long userId, Long eventId) throws NotFoundException, ConflictException;

    void delete(Long userId, Long commentId) throws NotFoundException, ConflictException;

    void delete(Long commentId) throws NotFoundException;

    CommentDto updateUserComment(Long userId, Long commentId, CommentDto commentDto) throws NotFoundException, ConflictException;

    List<CommentDto> getAllUserComments(Long userId) throws NotFoundException;

    List<CommentDto> getAllEventComments(GetCommentsAdminRequest param) throws NotFoundException;

    List<CommentDto> getAllEventComments(Long eventId, int from, int size) throws NotFoundException;

}
