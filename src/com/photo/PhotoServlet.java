package com.photo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
import com.util.MyUploadServlet;
import com.util.MyUtil;

@WebServlet("/photo/*")
@MultipartConfig // �̰� �� ���� ���� ���ε� �����Ѵٴϱ�?.. ��ӹ޴´ٰ� �ϴ��� ����ϴ� �� ���� ����
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

	// API
	private static final String API_LIST = "list.do";
	private static final String API_ARTICLE = "article.do";
	private static final String API_CREATED = "created.do";
	private static final String API_CREATED_OK = "created_ok.do";
	private static final String API_UPDATE = "update.do";
	private static final String API_UPDATE_OK = "update_ok.do";
	private static final String API_DELETE = "delete.do";
	private static final String API_DELETE_PHOTO = "deletePhoto.do";

	// JSP
	private static final String JSP_LIST = "list.jsp";
	private static final String JSP_CREATED = "created.jsp";
	private static final String JSP_UPDATE = JSP_CREATED;
	private static final String JSP_ARTICLE = "article.jsp";

	// PARAM
	private static final String PARAM_NUM = "num";
	private static final String PARAM_SUBJECT = "subject";
	private static final String PARAM_CONTENT = "content";
	private static final String PARAM_PAGE = "page";
	private static final String PARAM_CURRENT_PAGE = "current_page";
	private static final String PARAM_TOTAL_PAGE = "total_page";
	private static final String PARAM_DATA_COUNT = "dataCount";
	private static final String PARAM_IMAGE_PATH = "image_path";
	private static final String ATTRIBUTE_PAGING = "paging";
	private static final String ATTRIBUTE_LIST = "list";
	private static final String ATTRIBUTE_LIST_URL = "listUrl";
	private static final String ATTRIBUTE_ARTICLE_URL = "articleUrl";
	private static final String ATTRIBUTE_DTO = "dto";
	private static final String ATTRIBUTE_QUERY = "query";

	// SEARCH
	private static final String CONDITION = "condition";
	private static final String KEYWORD = "keyword";
	private static final String CONDITION_CREATED = "created";
	private static final String CONDITION_SUBJECT = "subject";
	private static final String CONDITION_CONTENT = "content";
	private static final String CONDITION_USERNAME = "username";

	// ETC
	private final static String SAVE_FILENAME = "saveFilename";

	// URI
	private String pathname;
	private String contextPath;
	private String imagePath;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");// POST �ѱ� ó�����
		String uri = req.getRequestURI();
		String root = req.getSession().getServletContext().getRealPath("/");// ���� ���� ��� ���ϱ�
		pathname = root + "uploads" + File.separator + "photo";// ���� ��� + ���ε�Ǵ� ��
		contextPath = req.getContextPath();
		imagePath = contextPath + "/uploads/photo";
		SessionInfo info = getSessionInfo(req.getSession());

		if (info == null) {
			resp.sendRedirect(req.getContextPath() + "/member/login.do");
			return;
		}

		if (uri.indexOf(API_LIST) != -1) {
			list(req, resp);
		} else if (uri.indexOf(API_ARTICLE) != -1) {
			article(req, resp);
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
		String path = VIEWS + "/" + JSP_LIST;
		PhotoDAO dao = new PhotoDAO();
		MyUtil util = new MyUtil();
		// �˻� �Ķ���� �ҷ����� �����ϱ�
		Map<String, Object> attributes = new HashMap<>();
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		String page = req.getParameter(PARAM_PAGE);
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		checkSearchParameter(condition, keyword);

		List<PhotoDTO> list;
		// �˻����� Ȯ��
		int currentPage = isNumeric(page) ? Integer.parseInt(page) : 1;
		int dataCount = 0;
		int rows = 6;
		int offset = (currentPage - 1) * rows;
		if (isSearchMode(attributes)) {
			// �˻������ ���
			dataCount = dao.dataCount((String) attributes.get(CONDITION), (String) attributes.get(KEYWORD));
			list = dao.listPhoto(offset, rows, condition, keyword);
		} else {
			dataCount = dao.dataCount();
			list = dao.listPhoto(offset, rows);
		}
		int listNum, n = 0;
		for (PhotoDTO dto : list) {
			listNum = dataCount - (offset + n);
			dto.setListNum(listNum);
		}
		int totalPage = util.pageCount(rows, dataCount);
		attributes.put(PARAM_DATA_COUNT, dataCount + "");
		attributes.put(CONDITION, condition);
		attributes.put(PARAM_CURRENT_PAGE, currentPage + "");
		attributes.put(PARAM_TOTAL_PAGE, totalPage + "");
		attributes.put(ATTRIBUTE_LIST, list);
		attributes.put(PARAM_IMAGE_PATH, imagePath);
		String query = makeQuery(attributes);
		String listURL = contextPath + "/" + PHOTO + "/" + API_LIST;
		String articleURL = contextPath + "/" + PHOTO + "/" + API_ARTICLE + query;
		attributes.put(ATTRIBUTE_LIST_URL, listURL);
		attributes.put(ATTRIBUTE_PAGING, util.paging(currentPage, totalPage, listURL));
		attributes.put(ATTRIBUTE_ARTICLE_URL, articleURL);
		attributes.put(ATTRIBUTE_QUERY, query);

		// �⺻ �Ķ���� setAttribute�ϱ�
		setAttributes(req, attributes);
		forward(req, resp, path);
	}

	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/" + JSP_ARTICLE;
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		// �˻� �Ķ���� �ҷ����� �����ϱ�
		Map<String, Object> attributes = new HashMap<>();
		String num = req.getParameter(PARAM_NUM);
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		String page = req.getParameter(PARAM_PAGE);
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		checkSearchParameter(condition, keyword);
		try {
			dto = dao.readPhoto(Integer.parseInt(num));
			attributes.put(ATTRIBUTE_DTO, dto);
			
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_LIST);
			return;
		}

		// PARAM
		attributes.put(CONDITION, condition);
		attributes.put(PARAM_PAGE, page);
		attributes.put(PARAM_IMAGE_PATH, imagePath);
		String query = makeQuery(attributes);
		String listURL = contextPath + "/" + PHOTO + "/" + API_LIST + query;
		attributes.put(ATTRIBUTE_LIST_URL, listURL);
		// �⺻ �Ķ���� setAttribute�ϱ�
		setAttributes(req, attributes);
		forward(req, resp, path);
	}

	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/" + JSP_CREATED;
		// ������ ó��
		req.setAttribute(MODE, MODE_CREATED);
		forward(req, resp, path);
	}

	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PhotoDAO dao = new PhotoDAO();

		SessionInfo info = getSessionInfo(req.getSession());
		// DTO ���� �Ķ���� �ޱ�
		String userId = info.getUserId();
		String subject = req.getParameter(PARAM_SUBJECT);
		String content = req.getParameter(PARAM_CONTENT);
		String imageFilename = "";
		// TODO: ����¡ ó��, �˻��� ó���� ���� �Ķ���� �ޱ�

		try {
			// ���� ���ε�
			Part p = req.getPart("upload");
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {// ������ ���ε�ǰ� �����ϴ� ��츸
				imageFilename = map.get(SAVE_FILENAME);
			}
			// dto��ü �����ϱ�
			PhotoDTO dto = new PhotoDTO(userId, subject, content, imageFilename);
			// dto��ü �����ϱ�
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

	private void checkSearchParameter(String condition, String keyword) {
		if (condition == null) {
			condition = CONDITION_SUBJECT;
		}

		if (keyword == null || keyword.length() == 0) {
			keyword = "";
		}
	}

	private void setAttributes(HttpServletRequest req, Map<String, Object> attributes) {
		if (req == null || attributes == null) {
			return;
		}
		for (String key : attributes.keySet()) {
			req.setAttribute(key, attributes.getOrDefault(key, ""));
		}
	}

	private boolean isSearchMode(Map<String, Object> attributes) {
		try {
			if (attributes == null) {
				return false;
			}
			String keyword = (String) attributes.get(KEYWORD);
			if (keyword != null && keyword.length() > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private String makeQuery(Map<String, Object> attributes) {
		StringBuilder query = new StringBuilder();
		String[] keys = { CONDITION, KEYWORD, PARAM_PAGE };
		if(attributes.keySet().size()==0) {
			//�ƹ��͵� ���ٸ�
			attributes.put(PARAM_PAGE, "1");
		}
		for (String key : keys) {
			Object value = attributes.get(key);
			if (value != null) {
				if (value instanceof String || value instanceof Integer || value instanceof Long
						|| value instanceof Double || value instanceof Float) {
					query.append("&" + key + "=" + value);
				}
			}
		}
		String result = query.toString();
		if (result.length() > 0) {
			return "?" + result.substring(1);
		} else {
			return null;
		}

	}

}
