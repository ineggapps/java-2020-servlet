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
@MultipartConfig // Super class���� ����ߴٰ� �ؼ� ��ӹ޴� Ŭ�������� ������ �� �ִ� ���� �ƴ�
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

//		System.out.println("===" + uri + "����...\n" + info);
		if (uri.indexOf("list.do") == -1 && info == null) {
			System.out.println("�������� �ʴ� ������ ���� || �α��� ���� ����");
			resp.sendRedirect(cp + "/member/login.do");
			return;
		}

		String root = session.getServletContext().getRealPath("/");
		pathname = root + "uploads" + File.separator + "notice";

		// �����ڸ� ���� �ø� �� �ֵ��� ���� �����ϱ�
		if (!isAvailable(uri, info)) {
//			System.out.println("ERR: �����ڸ� �����ؾ� ��\n���� ���� ����: " + info);
			resp.sendRedirect(cp + "/notice/list.do");
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
		} else if (uri.indexOf("download.do") != -1) {
			// ���� �ٿ�ε�
			download(req, resp);
		} else if (uri.indexOf("deletefile.do") != -1) {
			// ���� ����
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
		// �������� ��쿡�� ������ �� ����.
		return true;
	}

	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �� ���
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
			// �˻����� ���
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

		// �Ϲ� �������� �Խñ�
		List<NoticeDTO> list;
		if (keyword.length() > 0) {
			list = dao.listNotice(offset, rows, condition, keyword);
		} else {
			list = dao.listNotice(offset, rows);
		}

		// ���� ������
//		if(current_page==1) {			
		List<NoticeDTO> listNotice = null;
		listNotice = dao.listNotice();
		for (NoticeDTO dto : listNotice) {
			dto.setCreated(dto.getCreated().substring(0, 10));
		}
//		}

		// GAP (���� ����ϱ�)
		long gap;
		Date curDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// ����Ʈ �� ��ȣ �����
		int listNum, n = 0;
		for (NoticeDTO dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);
			try {
				Date date = sdf.parse(dto.getCreated());
//				gap = (curDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24);//�Ϸ� ������ ��ȯ
				gap = (curDate.getTime() - date.getTime()) / (1000 * 60 * 60);// �ð� ������ ��ȯ
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

		// ����¡ ó��
		String listUrl = cp + "/notice/list.do";
		String articleUrl = cp + "/notice/article.do?page=" + current_page;
		if (query.length() > 0) {
			listUrl += "?" + query;
			articleUrl += "&" + query;
		}

		String paging = myUtil.paging(current_page, total_page, listUrl);

		// ������ �����ϱ�
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
		// �� ����
		SessionInfo info = getSessionInfo(req);
		MyUtil util = new MyUtil();
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto;
		// parameter ó��
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
			dao.updateHitCount(num); // ��ȸ�� �ø��� ��ȸ
			dto = dao.readNotice(num);

			if (dto == null) {
				throw new Exception("�Խù��� �������� ����");
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
		// �� ����
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
		// DB�� �� ����ϱ�
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto = new NoticeDTO();

		try {
			// ���� ���ε�
			Part p = req.getPart("upload");
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {// ������ ���ε�ǰ� �����ϴ� ��쿡��
				dto.setSaveFilename(map.get(SAVE_FILENAME));
				dto.setOriginalFilename(map.get(ORIGINAL_FILENAME));
				dto.setFileSize(p.getSize());
			}
			// DB ����
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
		// �� ����
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
				throw new Exception("�Խù��� �������� ����");
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
		// DB�� �� �������� �ݿ��ϱ�

		String cp = req.getContextPath();
		HttpSession session = req.getSession();
		SessionInfo info = (SessionInfo) session.getAttribute(SESSION_INFO);
		// DB�� �� ����ϱ�
		NoticeDAO dao = new NoticeDAO();
		NoticeDTO dto;

		try {
			int num = Integer.parseInt(req.getParameter("num"));
			dto = dao.readNotice(num);
			
			// ���� ���ε�
//			Part p = req.getPart("upload");
//			Map<String, String> map = doFileUpload(p, pathname);
//			if (map != null) {// ������ ���ε�ǰ� �����ϴ� ��쿡��
//				dto.setSaveFilename(map.get(SAVE_FILENAME));
//				dto.setOriginalFilename(map.get(ORIGINAL_FILENAME));
//				dto.setFileSize(p.getSize());
//			}
			// DB ����
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
		// �� ����
	}

	protected void download(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �ٿ�ε�
		try {
			NoticeDAO dao = new NoticeDAO();
			int num = Integer.parseInt(req.getParameter("num"));

			NoticeDTO dto = dao.readNotice(num);
			boolean b = false;

			if (dto != null) {
				b = FileManager.doFiledownload(dto.getSaveFilename(), dto.getOriginalFilename(), pathname, resp);
			}
			
			if(!b) {
				//�ٿ�ε� ������ �������� ���� ���
				resp.setContentType("text/html; charset=utf-8");
				PrintWriter out = resp.getWriter();
				out.print("<script>alert('������ �������� �ʽ��ϴ�.');history.back();</script>");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	protected void deleteFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ����

	}

	private SessionInfo getSessionInfo(HttpServletRequest req) {
		if (req == null) {
			return null;
		}
		return (SessionInfo) req.getSession().getAttribute(SESSION_INFO);
	}

}
