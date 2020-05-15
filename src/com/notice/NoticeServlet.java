package com.notice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.member.SessionInfo;
import com.util.FileManager;
import com.util.MyUploadServlet;
import com.util.MyUtil;

@WebServlet("/notice/*")
@MultipartConfig // Super class에서 명시했다고 해서 상속받는 클래스에서 생략할 수 있는 것은 아님
public class NoticeServlet extends MyUploadServlet {

	private String pathname;

	private static final long serialVersionUID = 1L;
	private final static String VIEWS = "/WEB-INF/views";
	private final static String SESSION_INFO = "member";

	private final static String ORIGINAL_FILENAME = "originalFilename";
	private final static String SAVE_FILENAME = "saveFilename";

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String cp = req.getContextPath();
		String uri = req.getRequestURI();

		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute(SESSION_INFO);

//		System.out.println("===" + uri + "접근...\n" + info);
		if (uri.indexOf("list.do") == -1 && info == null) {
			System.out.println("존재하지 않는 페이지 접근 || 로그인 정보 없음");
			resp.sendRedirect(cp + "/member/login.do");
			return;
		}

		String root = session.getServletContext().getRealPath("/");
		pathname = root + "uploads" + File.separator + "notice";

		// 관리자만 글을 올릴 수 있도록 접근 제어하기
		if (!isAvailable(uri, info)) {
//			System.out.println("ERR: 관리자만 접근해야 함\n현재 세션 정보: " + info);
			resp.sendRedirect(cp + "/notice/list.do");
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
		} else if (uri.indexOf("download.do") != -1) {
			// 파일 다운로드
			download(req, resp);
		} else if (uri.indexOf("deletefile.do") != -1) {
			// 파일 삭제
			deleteFile(req, resp);
		}
	}

	private boolean isAvailable(String uri, SessionInfo info) {
		String[] acceptPages = { "list.do" };
		for (String page : acceptPages) {
			if (uri.indexOf(page) > -1) {
				return true;
			}
		}
		if (info == null || !info.getUserId().equals("admin")) {
			return false;
		}
		// 관리자의 경우에는 접근할 수 있음.
		return true;
	}

	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 목록
		NoticeDAO dao = new NoticeDAO();
		MyUtil myUtil = new MyUtil();
		String cp = req.getContextPath();
		String page = req.getParameter("page");
		int current_page = 1;
		if (page != null) {
			current_page = Integer.parseInt(page) > 0 ? Integer.parseInt(page) : 1;
		} else {
			page = "1";
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
		String r = req.getParameter("rows");
		int rows = r != null ? Integer.parseInt(r) : 10;
		int total_page = myUtil.pageCount(rows, dataCount);
		if (current_page > total_page) {
			current_page = total_page;
		}

		int offset = (current_page - 1) * rows;
		if (offset < 0) {
			offset = 0;
		}

		// 일반 공지사항 게시글
		List<NoticeDTO> list;
		if (keyword.length() > 0) {
			list = dao.listNotice(offset, rows, condition, keyword);
		} else {
			list = dao.listNotice(offset, rows);
		}

		// 공지 박제글
//		if(current_page==1) {			
		List<NoticeDTO> listNotice = null;
		listNotice = dao.listNotice();
		for (NoticeDTO dto : listNotice) {
			dto.setCreated(dto.getCreated().substring(0, 10));
		}
//		}

		// GAP (시차 계산하기)
		long gap;
		Date curDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 리스트 글 번호 만들기
		int listNum, n = 0;
		for (NoticeDTO dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);
			try {
				Date date = sdf.parse(dto.getCreated());
//				gap = (curDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24);//하루 단위로 변환
				gap = (curDate.getTime() - date.getTime()) / (1000 * 60 * 60);// 시간 단위로 변환
				dto.setGap(gap);
			} catch (Exception e) {
				e.printStackTrace();
			}

			dto.setCreated(dto.getCreated().substring(0, 10));
			n++;
		}

		String query = "";
		if (keyword.length() > 0) {
			query = "condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
		}
		query += "&rows=" + rows;

		// 페이징 처리
		String listUrl = cp + "/notice/list.do";
		String articleUrl = cp + "/notice/article.do?page=" + current_page;
		if (query.length() > 0) {
			listUrl += "?" + query;
			articleUrl += "&" + query;
		}

		String paging = myUtil.paging(current_page, total_page, listUrl);

		// 데이터 전달하기
		req.setAttribute("list", list);
		req.setAttribute("listNotice", listNotice);
		req.setAttribute("paging", paging);
		req.setAttribute("page", page);
		req.setAttribute("current_page", current_page);
		req.setAttribute("total_page", total_page);
		req.setAttribute("dataCount", dataCount);
		req.setAttribute("articleUrl", articleUrl);
		req.setAttribute("condition", condition);
		req.setAttribute("keyword", keyword);
		req.setAttribute("rows", rows);

		forward(req, resp, VIEWS + "/notice/list.jsp");
	}

	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 보기
		SessionInfo info = getSessionInfo(req);
		MyUtil util = new MyUtil();
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto;
		// parameter 처리
		String cp = req.getContextPath();
		String page = req.getParameter("page");
		int current_page = 1;
		if (page != null) {
			current_page = Integer.parseInt(page) > 0 ? Integer.parseInt(page) : 1;
		} else {
			page = "1";
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

		try {
			String rows = req.getParameter("rows");
			int num = Integer.parseInt(req.getParameter("num"));
			dao.updateHitCount(num); // 조회수 올리고 조회
			dto = dao.readNotice(num);

			if (dto == null) {
				throw new Exception("게시물이 존재하지 않음");
			}

			dto.setContent(util.htmlSymbols(dto.getContent()));

			NoticeDTO preReadNoticeDTO = dao.preReadNotice(num, condition, keyword);
			NoticeDTO nextReadNoticeDTO = dao.nextReadNotice(num, condition, keyword);

			String query = "?page=" + current_page + "&rows=" + rows;
			if (keyword != null && keyword.length() > 0) {
				query += "&condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
			}
			String articleUrl = cp + "/notice/article.do" + query;

			req.setAttribute("dto", dto);
			req.setAttribute("preReadNoticeDTO", preReadNoticeDTO);
			req.setAttribute("nextReadNoticeDTO", nextReadNoticeDTO);
			req.setAttribute("query", query);
			req.setAttribute("articleUrl", articleUrl);
			req.setAttribute(SESSION_INFO, info);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(cp + "/notice/list.do");
			return;
		}

		forward(req, resp, VIEWS + "/notice/article.jsp");
	}

	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 쓰기
//		HttpSession session = req.getSession();
//		SessionInfo info = (SessionInfo) session.getAttribute(SESSION_INFO);
//		if(!info.getUserId().equals("admin")) {
//			resp.sendRedirect(req.getContextPath()+"/notice/list.do");
//			return;
//		}
		req.setAttribute("mode", "created");
		forward(req, resp, VIEWS + "/notice/created.jsp");
	}

	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String cp = req.getContextPath();
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute(SESSION_INFO);
		// DB에 글 등록하기
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto = new NoticeDTO();

		try {
			// 파일 업로드
			Part p = req.getPart("upload");
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {// 파일이 업로드되고 존재하는 경우에만
				dto.setSaveFilename(map.get(SAVE_FILENAME));
				dto.setOriginalFilename(map.get(ORIGINAL_FILENAME));
				dto.setFileSize(p.getSize());
			}
			// DB 삽입
			String notice = req.getParameter("notice");
			if (notice != null) {
				dto.setNotice(notice.equalsIgnoreCase("on")?1:0);
			}
			dto.setUserId(info.getUserId());
			dto.setSubject(req.getParameter("subject"));
			dto.setContent(req.getParameter("content"));
			dao.insertNotice(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.sendRedirect(cp + "/notice/list.do");

	}

	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 수정
		req.setAttribute("mode", "update");
		SessionInfo info = getSessionInfo(req);
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto;

		String cp = req.getContextPath();
		String page = req.getParameter("page");
		int current_page = 1;
		if (page != null) {
			current_page = Integer.parseInt(page) > 0 ? Integer.parseInt(page) : 1;
		} else {
			page = "1";
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

		try {
			int num = Integer.parseInt(req.getParameter("num"));
			String rows = req.getParameter("rows");
			dto = dao.readNotice(num);
			if (dto == null) {
				throw new Exception("게시물이 존재하지 않음");
			}

			String query = "?page=" + current_page + "&rows=" + rows;
			if (keyword != null && keyword.length() > 0) {
				query += "&condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
			}

			req.setAttribute("dto", dto);
			req.setAttribute("query", query);
			req.setAttribute(SESSION_INFO, info);

		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(cp + "/notice/list.do");
			return;
		}

		forward(req, resp, VIEWS + "/notice/created.jsp");
	}

	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// DB에 글 수정사항 반영하기

		String cp = req.getContextPath();
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute(SESSION_INFO);
		// DB에 글 등록하기
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto;

		try {
			int num = Integer.parseInt(req.getParameter("num"));
			dto = dao.readNotice(num);
			
			// 파일 업로드
//			Part p = req.getPart("upload");
//			Map<String, String> map = doFileUpload(p, pathname);
//			if (map != null) {// 파일이 업로드되고 존재하는 경우에만
//				dto.setSaveFilename(map.get(SAVE_FILENAME));
//				dto.setOriginalFilename(map.get(ORIGINAL_FILENAME));
//				dto.setFileSize(p.getSize());
//			}
			// DB 삽입
			String notice = req.getParameter("notice");
			if (notice != null) {
				dto.setNotice(notice.equalsIgnoreCase("on")?1:0);
			}
			dto.setUserId(info.getUserId());
			dto.setSubject(req.getParameter("subject"));
			dto.setContent(req.getParameter("content"));
			dao.updateNotice(dto);
		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.sendRedirect(cp + "/notice/list.do");

	}

	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 글 삭제
	}

	protected void download(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 다운로드
		try {
			NoticeDAO dao = new NoticeDAO();
			int num = Integer.parseInt(req.getParameter("num"));

			NoticeDTO dto = dao.readNotice(num);
			boolean b = false;

			if (dto != null) {
				b = FileManager.doFiledownload(dto.getSaveFilename(), dto.getOriginalFilename(), pathname, resp);
			}
			
			if(!b) {
				//다운로드 파일이 존재하지 않은 경우
				resp.setContentType("text/html; charset=utf-8");
				PrintWriter out = resp.getWriter();
				out.print("<script>alert('파일이 존재하지 않습니다.');history.back();</script>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	protected void deleteFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// 파일 삭제

	}

	private SessionInfo getSessionInfo(HttpServletRequest req) {
		if (req == null) {
			return null;
		}
		return (SessionInfo) req.getSession().getAttribute(SESSION_INFO);
	}

}
