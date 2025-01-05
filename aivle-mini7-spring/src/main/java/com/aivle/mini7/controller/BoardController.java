package com.aivle.mini7.controller;

import com.aivle.mini7.dto.BoardDto;
import com.aivle.mini7.model.User;
import com.aivle.mini7.service.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public String getBoardList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("userType", user.getIdType());
        model.addAttribute("boardList", boardService.getBoardListWithIndex());
        return "board/list";
    }


    @GetMapping("/{id}")
    public String getBoardDetail(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/login";
        }

        // 게시글 상세 보기
        model.addAttribute("board", boardService.getBoardById(id));
        return "board/detail";
    }

    @GetMapping("/form")
    public String createForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/login";
        }

        // 사용자 유형을 전달 (관리자 여부 확인용)
        model.addAttribute("userType", user.getIdType());
        model.addAttribute("board", new BoardDto());
        return "board/form";
    }

    @PostMapping
    public String saveBoard(@ModelAttribute BoardDto boardDto, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/login";
        }


        // 공지사항 작성 권한 제한
        if ("공지사항".equals(boardDto.getPost_type()) && user.getIdType() != 1) {
            model.addAttribute("error", "관리자만 공지사항을 작성할 수 있습니다.");
            return "board/form";
        }
        // 작성자를 현재 로그인 사용자로 설정
        boardDto.setAuthor_id(user.getId());
        boardService.createBoard(boardDto);
        return "redirect:/board";
    }

    @DeleteMapping("/{id}/delete")
    public String deleteBoard(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // 권한 확인: 관리자 또는 작성자만 삭제 가능
        BoardDto board = boardService.getBoardById(id);
        if (user.getIdType() != 1 && !board.getAuthor_id().equals(user.getId())) {
            model.addAttribute("error", "삭제 권한이 없습니다.");
            return "error/unauthorized";
        }

        boardService.deleteBoard(id);

        // 삭제 후 게시글 목록으로 리다이렉트
        return "redirect:/board";
    }


    @GetMapping("/{id}/edit")
    public String editBoard(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // 권한 확인: 관리자 또는 작성자만 수정 가능
        BoardDto board = boardService.getBoardById(id);
        if (user.getIdType() != 1 && !board.getAuthor_id().equals(user.getId())) {
            model.addAttribute("error", "수정 권한이 없습니다.");
            return "error/unauthorized";
        }

        model.addAttribute("board", board);
        model.addAttribute("userType", user.getIdType());
        return "board/form"; // 수정 폼으로 이동
    }

    @PostMapping("/{id}")
    public String updateBoard(@PathVariable Long id, @ModelAttribute BoardDto boardDto, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // 권한 확인
        BoardDto existingBoard = boardService.getBoardById(id);
        if (user.getIdType() != 1 && !existingBoard.getAuthor_id().equals(user.getId())) {
            model.addAttribute("error", "수정 권한이 없습니다.");
            return "error/unauthorized";
        }

        // 업데이트 수행
        boardService.updateBoard(id, boardDto);
        return "redirect:/board";
    }


}
