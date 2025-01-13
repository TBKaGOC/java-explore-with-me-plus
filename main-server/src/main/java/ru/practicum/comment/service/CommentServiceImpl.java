package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.dto.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto addComment(final CommentDto commentDto, Long userId, Long eventId) throws NotFoundException, ConflictException {
        commentDto.setUserId(userId);
        commentDto.setEventId(eventId);

        User user = fetchUser(userId);
        Event event = fetchEvent(eventId);
        if (!EventState.PUBLISHED.equals(event.getState())) {
            log.warn("Невозможно добавить комментарий к событию, которое не опубликовано, состояние события = {}",
                    event.getState());
            throw new ConflictException("Невозможно сохранить комментарии для неопубликованного события.");
        }
        Comment comment = CommentMapper.mapTo(commentDto, user, event);
        comment.setCreated(LocalDateTime.now());
        if (user.getId().equals(event.getInitiator().getId())) {
            comment.setInitiator(true);
        }
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(savedComment);

    }

    @Override
    @Transactional
    public void delete(final Long userId, final Long commentId) throws NotFoundException, ConflictException {
        Comment comment = fetchComment(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Пользователь может удалять только свои комментарии.");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void delete(final Long commentId) throws NotFoundException {
        Comment comment = fetchComment(commentId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto updateUserComment(final Long userId, final Long commentId,
                                        final CommentDto commentDto) throws NotFoundException, ConflictException {
        Comment comment = fetchComment(commentId);
        fetchUser(userId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new ConflictException("Пользователь может удалять только свои комментарии.");
        }

        comment.setContent(commentDto.getContent());
        Comment updated = commentRepository.save(comment);

        return CommentMapper.mapToCommentDto(updated);
    }

    @Override
    public List<CommentDto> getAllUserComments(final Long userId) throws NotFoundException {
        User user = fetchUser(userId);
        return CommentMapper.mapToCommentDto(commentRepository.findByUserId(user.getId()));
    }

    @Override
    public List<CommentDto> getAllEventComments(final GetCommentsAdminRequest param) throws NotFoundException {
        final List<Comment> comments =
                getEventComments(param.getEventId(), param.getFrom(), param.getSize());
        return CommentMapper.mapToCommentDto(comments);
    }

    @Override
    public List<CommentDto> getAllEventComments(final Long eventId, final int from, final int size) throws NotFoundException {
        return CommentMapper.mapToCommentDto(getEventComments(eventId, from, size));
    }

    private List<Comment> getEventComments(final Long eventId, final int from, final int size) throws NotFoundException {
        if (!eventRepository.existsById(eventId)) {
            log.warn("Событие с идентификатором {} не существует в базе данных.", eventId);
            throw new NotFoundException("Событие не найдено.");
        }
        final PageRequest page = PageRequest.of(from / size, size);
        return commentRepository.findAllByEventId(eventId, page).getContent();
    }

    private User fetchUser(final Long userId) throws NotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователь с идентификатором {} не найден.", userId);
                    return new NotFoundException("Пользователь не найден.");
                });
    }

    private Event fetchEvent(final Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с идентификатором {} не найдено.", eventId);
                    return new NotFoundException("Событие не найдено.");
                });
    }

    private Comment fetchComment(final Long commentId) throws NotFoundException {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий с идентификатором {} не найден.", commentId);
                    return new NotFoundException("Комментарий не найден.");
                });
    }
}
