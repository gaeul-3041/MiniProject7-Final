package com.aivle.mini7.repository;

import com.aivle.mini7.dto.BoardDto;
import com.aivle.mini7.model.Board;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    @Query("SELECT new com.aivle.mini7.dto.BoardDto(" +
            "b.board_id, b.author_id, b.title, b.content, b.post_type, b.created_time) " +
            "FROM Board b")
    List<BoardDto> findAllAsDto();

    @Query("SELECT new com.aivle.mini7.dto.BoardDto(" +
            "b.board_id, b.author_id, b.title, b.content, b.post_type, b.created_time) " +
            "FROM Board b WHERE b.board_id = :board_id")
    Optional<BoardDto> findByIdAsDto(@Param("board_id") Long board_id);
}
