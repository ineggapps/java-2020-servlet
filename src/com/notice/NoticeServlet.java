package com.notice;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.bbs.BoardDTO;
import com.member.SessionInfo;
import com.util.MyUploadServlet;
import com.util.MyUtil;

@WebServlet("/notice/*")
@MultipartConfig // Super class���� ����ߴٰ� �ؼ� ��ӹ޴� Ŭ�������� ������ �� �ִ� ���� �ƴ�
public class NoticeServlet extends MyUploadServlet {

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

		System.out.println("===" + uri + "����...\n" + info);
		if (uri.indexOf("list.do") == -1 && info == null) {
			System.out.println("�������� �ʴ� ������ ���� || �α��� ���� ����");
			resp.sendRedirect(cp + "/member/login.do");
			return;
		}

		// �����ڸ� ���� �ø� �� �ֵ��� ���� �����ϱ�
		if (!isAvailable(uri, info)) {
			System.out.println("ERR: �����ڸ� �����ؾ� ��\n���� ���� ����: " + info);
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
		String[] acceptPages = { "list.do", "download.do" };
//		String[] adminPages = {"created.do", "created_ok.do", "update.do", "update_ok.do", "delete.do", "deletefile.do"};
		for (String page : acceptPages) {
			if (uri.indexOf(page) > -1) {
				System.out.println("������ �� �ִ� ������ " + page);
				return true;
			}
		}
		if (info == null || !info.getUserId().equals("admin")) {
			System.out.println("�α����� �ʿ���");
			return false;
		}
		// �������� ��쿡�� ������ �� ����.
		System.out.println("������ ���� ����");
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
		int rows = r!=null?Integer.parseInt(r):10;
		int total_page = myUtil.pageCount(rows, dataCount);
		if (current_page > total_page) {
			current_page = total_page;
		}

		int offset = (current_page - 1) * rows;
		if (offset < 0) {
			offset = 0;
		}

		List<NoticeDTO> list;
		if (keyword.length() > 0) {
			list = dao.listNotice(offset, rows, condition, keyword);
		} else {
			list = dao.listNotice(offset, rows);
		}

		// ����Ʈ �� ��ȣ �����
		int listNum, n = 0;
		for (NoticeDTO dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);
			n++;
		}

		String query = "";
		if (keyword.length() > 0) {
			query = "condition=" + condition + "&keyword=" + URLEncoder.encode(keyword, "UTF-8"); 
		}
		query +=  "&rows=" + rows;

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

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "notice";

		try {
			// ���� ���ε�
			Part p = req.getPart("upload");
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {//������ ���ε�ǰ� �����ϴ� ��쿡��
				dto.setSaveFilename(map.get(SAVE_FILENAME));
				dto.setOriginalFilename(map.get(ORIGINAL_FILENAME));
				dto.setFileSize(p.getSize());
			}
			// DB ����
			String notice = req.getParameter("notice");
			if (notice != null) {
				dto.setNotice(Integer.parseInt(notice));
			}
			dto.setUserId(info.getUserId());
			dto.setSubject(req.getParameter("subject"));
			dto.setContent(req.getParameter("content"));

			for (int i = 0 ;i<100;i++) {
				int random = (int)(Math.random()*1000)+1;
				String subject =  dto.getSubject() + random;
				String content = dto.getContent()+ random;
				
				dto.setSubject(subject);
				dto.setContent(content);
				dao.insertNotice(dto);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.sendRedirect(cp + "/notice/list.do");

	}

	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �� ����
		req.setAttribute("mode", "update");
		forward(req, resp, VIEWS + "/notice/created.jsp");
	}

	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// DB�� �� �������� �ݿ��ϱ�
	}

	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �� ����
	}

	protected void download(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// �ٿ�ε�
	}

	protected void deleteFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// ���� ����

	}

}
