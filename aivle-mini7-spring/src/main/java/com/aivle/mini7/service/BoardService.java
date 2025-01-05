package com.aivle.mini7.service;

import com.aivle.mini7.dto.BoardDto;
import com.aivle.mini7.model.Board;
import com.aivle.mini7.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardDto createBoard(BoardDto boardDto) {
        Board board = toEntity(boardDto);
        board.setCreated_time(getCurrentTime());
        return new BoardDto(boardRepository.save(board));
    }

    public List<BoardDto> getBoardList() {
        return boardRepository.findAll().stream()
                .map(BoardDto::new)
                .collect(Collectors.toList());
    }

    public BoardDto getBoardById(Long board_id) {
        return boardRepository.findById(board_id)
                .map(BoardDto::new)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }
    public List<Map<String, Object>> getBoardListWithIndex() {
        List<BoardDto> boardList = getBoardList();
        List<Map<String, Object>> boardListWithIndex = new ArrayList<>();

        for (int i = 0; i < boardList.size(); i++) {
            BoardDto board = boardList.get(i);
            Map<String, Object> boardWithIndex = new HashMap<>();
            boardWithIndex.put("index", i + 1); // 1부터 시작하는 번호
            boardWithIndex.put("board", board);
            boardListWithIndex.add(boardWithIndex);
        }

        return boardListWithIndex;
    }

    private Board toEntity(BoardDto dto) {
        return Board.builder()
                .author_id(dto.getAuthor_id())
                .title(dto.getTitle())
                .content(dto.getContent())
                .post_type(dto.getPost_type())
                .build();
    }
    public void deleteBoard(Long board_id) {
        if (!boardRepository.existsById(board_id)) {
            throw new RuntimeException("삭제하려는 게시글이 존재하지 않습니다.");
        }
        boardRepository.deleteById(board_id);
    }



    public void updateBoard(Long id, BoardDto boardDto) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 기존 데이터를 업데이트
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setPost_type(boardDto.getPost_type());
        boardRepository.save(board);
    }




    private String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
