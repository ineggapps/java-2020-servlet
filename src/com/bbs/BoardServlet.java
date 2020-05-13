package com.bbs;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BoardServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	protected void forward(HttpServletRequest req, HttpServletResponse resp, String path) throws ServletException, IOException{
		RequestDispatcher dispatcher = req.getRequestDispatcher(path);
		dispatcher.forward(req, resp);
	}
	
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		req.setCharacterEncoding("UTF-8");

		// context path부터 끝까지 보인다.
		String uri = req.getRequestURI();
		if(uri.indexOf("list.do")!=-1) {
			
		}else if(uri.indexOf("???.do")!=-1) {
			
		}
	}
	
	//게시글 목록
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 쓰기
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 등록처리
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 보기
	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 수정
	protected void updatForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 수정처리
	protected void updatSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//게시글 지우기
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}

}
