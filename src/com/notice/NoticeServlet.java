package com.notice;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import com.member.SessionInfo;
import com.util.MyUploadServlet;

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

			dao.insertNotice(dto);
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
