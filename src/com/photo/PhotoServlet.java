package com.photo;

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

@WebServlet("/photo/*")
@MultipartConfig //이거 안 쓰면 파일 업로드 실패한다니깐?.. 상속받는다고 하더라도 언급하는 거 잊지 말기
public class PhotoServlet extends MyUploadServlet {

	// CONTEXT
	private static final String VIEW = "/WEB-INF/views";
	private static final String PHOTO = "photo";
	private static final String VIEWS = VIEW + "/" + PHOTO;
	private static final String SESSION_INFO = "member";

	// MODE
	private static final String MODE = "mode";
	private static final String MODE_CREATED = "created";
	private static final String MODE_UPDATE = "update";

	// PAGE
	private static final String API_LIST = "list.do";
	private static final String API_CREATED = "created.do";
	private static final String API_CREATED_OK = "created_ok.do";
	private static final String API_UPDATE = "update.do";
	private static final String API_UPDATE_OK = "update_ok.do";
	private static final String API_DELETE = "delete.do";
	private static final String API_DELETE_PHOTO = "deletePhoto.do";

	// PARAM
	private static final String PARAM_SUBJECT = "subject";
	private static final String PARAM_CONTENT = "content";
	private static final String PARAM_IMAGE_FILENAME = "imageFilename";

	// URI
	private String pathname;
	private String contextPath;

	// ETC
	private final static String SAVE_FILENAME = "saveFilename";

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");//POST 한글 처리방식
		String uri = req.getRequestURI();
		String root = req.getSession().getServletContext().getRealPath("/");// 실제 물리 경로 구하기
		pathname = root + "uploads" + File.separator + "photo";// 물리 경로 + 업로드되는 곳
		contextPath = req.getContextPath();
		SessionInfo info = getSessionInfo(req.getSession());

		if (info == null) {
			resp.sendRedirect(req.getContextPath() + "/member/login.do");
			return;
		}

		if (uri.indexOf(API_LIST) != -1) {
			list(req, resp);
		} else if (uri.indexOf(API_CREATED) != -1) {
			createdForm(req, resp);
		} else if (uri.indexOf(API_CREATED_OK) != -1) {
			createdSubmit(req, resp);
		} else if (uri.indexOf(API_UPDATE) != -1) {
			updateForm(req, resp);
		} else if (uri.indexOf(API_UPDATE_OK) != -1) {
			updateSubmit(req, resp);
		} else if (uri.indexOf(API_DELETE) != -1) {
			delete(req, resp);
		} else if (uri.indexOf(API_DELETE_PHOTO) != -1) {
			deletePhoto(req, resp);
		}
	}

	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

	}

	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/created.jsp";
		// 포워딩 처리
		req.setAttribute(MODE, MODE_CREATED);
		forward(req, resp, path);
	}

	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PhotoDAO dao = new PhotoDAO();

		SessionInfo info = getSessionInfo(req.getSession());
		// DTO 관련 파라미터 받기
		String userId = info.getUserId();
		String subject = req.getParameter(PARAM_SUBJECT);
		String content = req.getParameter(PARAM_CONTENT);
		String imageFilename = "";
		// TODO: 페이징 처리, 검색어 처리를 위한 파라미터 받기

		try {
			// 파일 업로드
			Part p = req.getPart("upload");
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {// 파일이 업로드되고 존재하는 경우만
				imageFilename = map.get(SAVE_FILENAME);
			}
			// dto객체 생성하기
			PhotoDTO dto = new PhotoDTO(userId, subject, content, imageFilename);
			// dto객체 삽입하기
			dao.insertPhoto(dto);

			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_LIST);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_LIST);
			return;
		}
	}

	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	protected void deletePhoto(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}

	private SessionInfo getSessionInfo(HttpSession session) {
		Object o = session.getAttribute(SESSION_INFO);
		if (o instanceof SessionInfo) {
			return (SessionInfo) o;
		}
		return null;
	}

}
