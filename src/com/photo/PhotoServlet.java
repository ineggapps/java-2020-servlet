package com.photo;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import com.util.FileManager;
import com.util.MyUploadServlet;
import com.util.MyUtil;

@WebServlet("/photo/*")
@MultipartConfig // 이거 안 쓰면 파일 업로드 실패한다니깐?.. 상속받는다고 하더라도 언급하는 거 잊지 말기
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
	private static final String PARAM_PAGE = "page";
	private static final String ATTRIBUTE_SUBJECT = "subject";
	private static final String ATTRIBUTE_CONTENT = "content";
	private static final String ATTRIBUTE_CURRENT_PAGE = "current_page";
	private static final String ATTRIBUTE_TOTAL_PAGE = "total_page";
	private static final String ATTRIBUTE_DATA_COUNT = "dataCount";
	private static final String ATTRIBUTE_IMAGE_PATH = "image_path";
	private static final String ATTRIBUTE_PAGING = "paging";
	private static final String ATTRIBUTE_LIST = "list";
	private static final String ATTRIBUTE_LIST_URL = "listUrl";
	private static final String ATTRIBUTE_ARTICLE_URL = "articleUrl";
	private static final String ATTRIBUTE_DELETE_PHOTO_URL = "deletePhotoUrl";
	private static final String ATTRIBUTE_DTO = "dto";
	private static final String ATTRIBUTE_QUERY = "query";
	private static final String ATTRIBUTE_IMAGE_FILENAME = "imageFilename";

	// SEARCH
	private static final String CONDITION = "condition";
	private static final String KEYWORD = "keyword";
	private static final String CONDITION_CREATED = "created";
	private static final String CONDITION_SUBJECT = "subject";
	private static final String CONDITION_CONTENT = "content";
	private static final String CONDITION_USERNAME = "username";

	//ETC
	private static final String SAVE_FILENAME = "saveFilename";
	
	// URI
	private String pathname;
	private String contextPath;
	private String imagePath;

	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("utf-8");// POST 한글 처리방식
		String uri = req.getRequestURI();
		String root = req.getSession().getServletContext().getRealPath("/");// 실제 물리 경로 구하기
		pathname = root + "uploads" + File.separator + "photo";// 물리 경로 + 업로드되는 곳
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
		Map<String, Object> attributes = new HashMap<>();
		// 검색 파라미터 불러오고 정리하기
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		checkSearchParameter(condition, keyword);
		if(keyword != null ) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		String page = req.getParameter(PARAM_PAGE);

		List<PhotoDTO> list;
		// 검색여부 확인
		int currentPage = isNumeric(page) ? Integer.parseInt(page) : 1;
		int dataCount = 0;
		int rows = 6;
		int offset = (currentPage - 1) * rows;
		if (isSearchMode(attributes)) {
			// 검색모드인 경우
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
		attributes.put(ATTRIBUTE_DATA_COUNT, dataCount + "");
		attributes.put(CONDITION, condition);
		attributes.put(ATTRIBUTE_CURRENT_PAGE, currentPage + "");
		attributes.put(ATTRIBUTE_TOTAL_PAGE, totalPage + "");
		attributes.put(ATTRIBUTE_LIST, list);
		attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
		String query = makeQuery(attributes);//페이지 빠진 파라미터 쿼리
		String listURL = contextPath + "/" + PHOTO + "/" + API_LIST;
		String articleURL = contextPath + "/" + PHOTO + "/" + API_ARTICLE;
		attributes.put(ATTRIBUTE_PAGING, util.paging(currentPage, totalPage, listURL + query));
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		attributes.put(ATTRIBUTE_LIST_URL, listURL);
		attributes.put(ATTRIBUTE_ARTICLE_URL, articleURL);
		query = makeQuery(attributes);//페이징 포함 파라미터 쿼리
		attributes.put(ATTRIBUTE_QUERY, query);

		// 기본 파라미터 setAttribute하기
		setAttributes(req, attributes);
		forward(req, resp, path);
	}

	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/" + JSP_ARTICLE;
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		Map<String, Object> attributes = new HashMap<>();
		// 검색 파라미터 불러오고 정리하기
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		checkSearchParameter(condition, keyword);
		if(keyword != null ) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		String num = req.getParameter(PARAM_NUM);
		String page = req.getParameter(PARAM_PAGE);
		String query;
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		checkSearchParameter(condition, keyword);
		try {
			dto = dao.readPhoto(Integer.parseInt(num));
			attributes.put(ATTRIBUTE_DTO, dto);

			// PARAM
			query = makeQuery(attributes);
			attributes.put(CONDITION, condition);
			attributes.put(PARAM_PAGE, page);
			attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
			String listURL = contextPath + "/" + PHOTO + "/" + API_LIST;
			attributes.put(ATTRIBUTE_LIST_URL, listURL);
			attributes.put(ATTRIBUTE_QUERY, query);

			// 기본 파라미터 setAttribute하기
			setAttributes(req, attributes);
			forward(req, resp, path);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_LIST);
			return;
		}

	}

	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/" + JSP_CREATED;
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
		String subject = req.getParameter(ATTRIBUTE_SUBJECT);
		String content = req.getParameter(ATTRIBUTE_CONTENT);
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
		String path = VIEWS + "/" + JSP_UPDATE;
		String deletePhotoUrl = contextPath + "/" + PHOTO + "/" + API_DELETE_PHOTO;
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		SessionInfo info = getSessionInfo(req.getSession());
		// 파라미터 처리
		Map<String, Object> attributes = new HashMap<>();
		String num = req.getParameter(PARAM_NUM);
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		if(keyword != null && req.getMethod().equalsIgnoreCase("GET")) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		String page = req.getParameter(PARAM_PAGE);
		String query;
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
		attributes.put(ATTRIBUTE_DELETE_PHOTO_URL, deletePhotoUrl);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		attributes.put(PARAM_NUM,num);
		checkSearchParameter(condition, keyword);

		// 포워딩 처리 준비
		attributes.put(MODE, MODE_UPDATE);
		query = makeQuery(attributes);
		attributes.put(ATTRIBUTE_QUERY,query);
		try {
			dto = dao.readPhoto(Integer.parseInt(num));
			if (!dto.getUserId().equals(info.getUserId())) {
				resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_ARTICLE + query + "&" + PARAM_NUM + "=" + num);
				return;
			}
			attributes.put(ATTRIBUTE_DTO, dto);
			// 기본 파라미터 setAttribute하기
			setAttributes(req, attributes);
			forward(req, resp, path);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_ARTICLE + query + "&" + PARAM_NUM + "=" + num);
			return;
		}

	}

	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		SessionInfo info = getSessionInfo(req.getSession());
		// 파라미터 처리
		Map<String, Object> attributes = new HashMap<>();
		String num = req.getParameter(PARAM_NUM);
		// 검색 파라미터 불러오고 정리하기
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		checkSearchParameter(condition, keyword);
		if(keyword != null ) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		String page = req.getParameter(PARAM_PAGE);
		String query;
		attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		checkSearchParameter(condition, keyword);
		query = makeQuery(attributes);
		try {
			int n = Integer.parseInt(num);
			String userId = info.getUserId();
			String subject = req.getParameter(ATTRIBUTE_SUBJECT);
			String content = req.getParameter(ATTRIBUTE_CONTENT);
			String imageFilename = req.getParameter(ATTRIBUTE_IMAGE_FILENAME);
			dto = new PhotoDTO(n, userId, subject, content, imageFilename);
			
			// 파일 업로드
			Part p = req.getPart("upload");//input name이 upload임!!
			Map<String, String> map = doFileUpload(p, pathname);
			if (map != null) {// 파일이 업로드되고 존재하는 경우에만
				//기존 파일 삭제하기
				if( imageFilename !=null && imageFilename.length()!=0) {
					FileManager.doFiledelete(pathname, imageFilename);
				}
				//새로운 파일 업로드한 파일명 씌우기
				dto.setImageFilename(map.get(SAVE_FILENAME));
			}
			
			dao.updatePhoto(dto);
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_ARTICLE + "/" + query + "&" + PARAM_NUM + "=" + num);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(contextPath + "/" + PHOTO + "/" + API_UPDATE + "/" + query + "&" + PARAM_NUM + "=" + num);
			return;
		}

	}

	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String listUrl = contextPath + "/" + PHOTO + "/" + API_LIST;
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		SessionInfo info = getSessionInfo(req.getSession());
		// 파라미터 처리
		Map<String, Object> attributes = new HashMap<>();
		String num = req.getParameter(PARAM_NUM);
		// 검색 파라미터 불러오고 정리하기
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		checkSearchParameter(condition, keyword);
		if(keyword != null ) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		String page = req.getParameter(PARAM_PAGE);
		String query;
		attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		attributes.put(PARAM_NUM, num);
		attributes.put(MODE, MODE_UPDATE);
		checkSearchParameter(condition, keyword);

		// 포워딩 처리 준비
		query = makeQuery(attributes);
		attributes.put(ATTRIBUTE_QUERY, query);
		try {
			dto = dao.readPhoto(Integer.parseInt(num));
			//작성자가 아니면 글 보기 페이지로 이동하기
			if(!dto.getUserId().equals(info.getUserId())) {
				throw new Exception("작성자가 아님");
			}
			FileManager.doFiledelete(pathname, dto.getImageFilename());
			dao.deletePhoto(Integer.parseInt(num));//갱신
		} catch (Exception e) {
			e.printStackTrace();
		}
		//포워딩 처리
		resp.sendRedirect(listUrl + query);

	}

	protected void deletePhoto(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = VIEWS + "/" + JSP_UPDATE;
		String updateUrl = contextPath + "/" + PHOTO + "/" + API_UPDATE;
		PhotoDAO dao = new PhotoDAO();
		PhotoDTO dto;
		SessionInfo info = getSessionInfo(req.getSession());
		// 파라미터 처리
		Map<String, Object> attributes = new HashMap<>();
		String num = req.getParameter(PARAM_NUM);
		String condition = req.getParameter(CONDITION);
		String keyword = req.getParameter(KEYWORD);
		if(keyword != null) {
			keyword = URLDecoder.decode(keyword,"utf-8");
		}
		String page = req.getParameter(PARAM_PAGE);
		String query;
		attributes.put(CONDITION, condition);
		attributes.put(KEYWORD, keyword);
		attributes.put(ATTRIBUTE_IMAGE_PATH, imagePath);
		attributes.put(PARAM_PAGE, page != null ? page : "1");
		attributes.put(PARAM_NUM, num);
		attributes.put(MODE, MODE_UPDATE);
		checkSearchParameter(condition, keyword);

		// 포워딩 처리 준비
		query = makeQuery(attributes);
		attributes.put(MODE, MODE_UPDATE);
		attributes.put(ATTRIBUTE_QUERY, query);
		try {
			dto = dao.readPhoto(Integer.parseInt(num));
			//작성자가 아니면 글 보기 페이지로 이동하기
			if(!dto.getUserId().equals(info.getUserId())) {
				throw new Exception("작성자가 아님");
			}
			if(FileManager.doFiledelete(pathname, dto.getImageFilename())){
				dto.setImageFilename("");
				dao.updatePhoto(dto);//갱신
			}else {
				throw new Exception("이미지 삭제 실패");
			}
			//포워딩 처리
			attributes.put(ATTRIBUTE_DTO, dto);
			setAttributes(req, attributes);
			forward(req, resp, path);
		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(updateUrl + query + "&" + PARAM_NUM + "=" + num);
			return;
		}
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
			Object value = attributes.getOrDefault(key, "");
			if(value != null && req.getMethod().equalsIgnoreCase("GET") && key.equals(KEYWORD) &&  value instanceof String) {
				try {
					value = (Object)URLDecoder.decode(((String)value),"utf-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			req.setAttribute(key, value);
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
		if (attributes.keySet().size() == 0) {
			// 아무것도 없다면
			attributes.put(PARAM_PAGE, "1");
		}
		for (String key : keys) {
			Object value = attributes.get(key);
			if (value != null) {
				if(key.equals(KEYWORD) &&  value instanceof String) {
					try {
						value = (Object)URLEncoder.encode(((String)value),"utf-8");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (value instanceof String || value instanceof Integer || value instanceof Long
						|| value instanceof Double || value instanceof Float) {
					query.append("&" + key + "=" + value);
				}
			}
		}
		String result = query.toString();
//		System.out.println("반환되는 쿼리" + result);
		if (result.length() > 0) {
			return "?" + result.substring(1);
		} else {
			return "";
		}

	}

}
