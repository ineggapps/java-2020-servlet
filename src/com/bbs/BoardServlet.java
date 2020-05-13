package com.bbs;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;

public class BoardServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	protected void forward(HttpServletRequest req, HttpServletResponse resp, String path)
			throws ServletException, IOException {
		RequestDispatcher dispatcher = req.getRequestDispatcher(path);
		dispatcher.forward(req, resp);
	}

	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String cp = req.getContextPath();
		String uri = req.getRequestURI();

		// 제한사항: 로그인한 사람만 게시판에 접근할 수 있도록 할 것임. (게시글 목록 조회, 글조회, 글쓰기 모두!!!)

		// 로그인 정보 가져오기
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		if (info == null) {// 로그인이 되어 있지 않은 상태면? 로그인 페이지로 간다.
			resp.sendRedirect(cp + "/member/login.do");
			return;
		}

		if (uri.indexOf("list.do") != -1) {
			// 게시글 목록
			list(req, resp);
		} else if (uri.indexOf("created.do") != -1) {
			// 게시글 쓰기
			createdForm(req, resp);
		} else if (uri.indexOf("created_ok.do") != -1) {
			// 게시글 등록처리
			createdSubmit(req, resp);
		} else if (uri.indexOf("article.do") != -1) {
			// 게시글 보기
			article(req, resp);
		} else if (uri.indexOf("update.do") != -1) {
			// 게시글 수정
			updateForm(req, resp);
		} else if (uri.indexOf("update_ok.do") != -1) {
			// 게시글 수정처리
			updateSubmit(req, resp);
		} else if (uri.indexOf("delete.do") != -1) {
			// 게시글 지우기
			delete(req, resp);
		}
	}

	// 게시글 목록
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 쓰기
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 등록처리
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}

	// 게시글 보기
	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 수정
	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 수정처리
	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 지우기
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

}
