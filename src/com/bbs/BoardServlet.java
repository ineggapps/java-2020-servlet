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

//Tomcat 7.5���� ����
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

		// ���ѻ���: �α����� ����� �Խ��ǿ� ������ �� �ֵ��� �� ����. (�Խñ� ��� ��ȸ, ����ȸ, �۾��� ���!!!)

		// �α��� ���� ��������
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		if (info == null) {// �α����� �Ǿ� ���� ���� ���¸�? �α��� �������� ����.
			resp.sendRedirect(cp + "/member/login.do");
			return;
		}

		if (uri.indexOf("list.do") != -1) {
			// �Խñ� ���
			list(req, resp);
		} else if (uri.indexOf("created.do") != -1) {
			// �Խñ� ����
			createdForm(req, resp);
		} else if (uri.indexOf("created_ok.do") != -1) {
			// �Խñ� ���ó��
			createdSubmit(req, resp);
		} else if (uri.indexOf("article.do") != -1) {
			// �Խñ� ����
			article(req, resp);
		} else if (uri.indexOf("update.do") != -1) {
			// �Խñ� ����
			updateForm(req, resp);
		} else if (uri.indexOf("update_ok.do") != -1) {
			// �Խñ� ����ó��
			updateSubmit(req, resp);
		} else if (uri.indexOf("delete.do") != -1) {
			// �Խñ� �����
			delete(req, resp);
		} else {
//			resp.sendRedirect(req.getContextPath()+"/bbs/list.do");
		}
	}

	private static final String VIEWS = "/WEB-INF/views/bbs";

	// �Խñ� ���
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
			// �˻����� ���
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

		// ����Ʈ �� ��ȣ �����
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

		// ����¡ ó��
		String listUrl = cp + "/bbs/list.do";
		String articleUrl = cp + "/bbs/article.do?page=" + current_page;
		if (query.length() > 0) {
			listUrl += "?" + query;
			articleUrl += "&" + query;
		}

		String paging = myUtil.paging(current_page, total_page, listUrl);

		// ������ �����ϱ�
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

	// �Խñ� ����
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("mode", "created");
		forward(req, resp, VIEWS + "/created.jsp");
	}

	// �Խñ� ���ó��
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String cp = req.getContextPath();
		BoardDAO dao = new BoardDAO();
		// ����� ���� ��ü ����
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

	// �Խñ� ����
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

	// �Խñ� ����
	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("mode", "update");
		forward(req, resp, VIEWS + "/created.jsp");
	}

	// �Խñ� ����ó��
	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	// �Խñ� �����
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

}
