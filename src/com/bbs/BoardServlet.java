package com.bbs;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.member.SessionInfo;
import com.util.MyUtil;

//Tomcat 7.5부터 가능
@WebServlet("/bbs/*")
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
		} else {
//			resp.sendRedirect(req.getContextPath()+"/bbs/list.do");
		}
	}

	private static final String VIEWS = "/WEB-INF/views/bbs";

	// 게시글 목록
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		BoardDAO dao = new BoardDAO();
		MyUtil myUtil = new MyUtil();
		String cp = req.getContextPath();
		String page = req.getParameter("page");
		int current_page = 1;
		if (page != null) {
			current_page = Integer.parseInt(page)>0?Integer.parseInt(page):1;
		}

		String condition = req.getParameter("condition");
		String keyword = req.getParameter("keyword");
		if (condition == null) {
			condition = "subject";
			keyword = "";
		}
		if (req.getMethod().equalsIgnoreCase("GET")) {
			keyword = URLDecoder.decode(keyword, "UTF-8");
		}

		int dataCount;
		if (condition.length() > 0 && keyword.length() > 0) {
			// 검색했을 경우
			dataCount = dao.dataCount(condition, keyword);
		} else {
			dataCount = dao.dataCount();
		}
		int rows = 10;
		int total_page = myUtil.pageCount(rows, dataCount);
		if (current_page > total_page) {
			current_page = total_page;
		}

		int offset = (current_page - 1) * rows;
		if (offset < 0) {
			offset = 0;
		}

		List<BoardDTO> list;
		if (keyword.length() > 0) {
			list = dao.listBoard(offset, rows, condition, keyword);
		} else {
			list = dao.listBoard(offset, rows);
		}

		// 리스트 글 번호 만들기
		int listNum, n = 0;
		for (BoardDTO dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);
			n++;
		}

		String query = "";
		if (keyword.length() > 0) {
			query = "condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
		}

		// 페이징 처리
		String listUrl = cp + "/bbs/list.do";
		String articleUrl = cp + "/bbs/article.do?page=" + current_page;
		if (query.length() > 0) {
			listUrl += "?" + query;
			articleUrl += "&" + query;
		}

		String paging = myUtil.paging(current_page, total_page, listUrl);

		// 데이터 전달하기
		req.setAttribute("list", list);
		req.setAttribute("paging", paging);
		req.setAttribute("page", page);
		req.setAttribute("current_page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("condition", condition);
		req.setAttribute("keyword", keyword);

		forward(req, resp, VIEWS + "/list.jsp");
	}

	// 게시글 쓰기
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("mode", "created");
		forward(req, resp, VIEWS + "/created.jsp");
	}

	// 게시글 등록처리
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String cp = req.getContextPath();
		BoardDAO dao = new BoardDAO();
		// 등록을 위한 객체 생성
		BoardDTO dto = new BoardDTO();
		dto.setSubject(req.getParameter("subject"));
		dto.setContent(req.getParameter("content"));
		SessionInfo info = (SessionInfo) req.getSession().getAttribute("member");
		dto.setUserId(info.getUserId());
		try {
			dao.insertBoard(dto);
//			String title = dto.getSubject();
//			for (int i = 0; i < 1000; i++) {
//				dto.setSubject(title + (int) (Math.random() * 10000) + 1);
//				dao.insertBoard(dto);
//				dto.setSubject(title);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.sendRedirect(cp + "/bbs/list.do");
	}

	// 게시글 보기
	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String cp = req.getContextPath();
		BoardDAO dao = new BoardDAO();
		try {
			int num = Integer.parseInt(req.getParameter("num"));
			BoardDTO dto = dao.readBoard(num);
			System.out.println(dto);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(cp + "/bbs/list.do");
		}
		
		//dto, page, condition, keyword
		
	}

	// 게시글 수정
	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("mode", "update");
		forward(req, resp, VIEWS + "/created.jsp");
	}

	// 게시글 수정처리
	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// 게시글 지우기
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

}
