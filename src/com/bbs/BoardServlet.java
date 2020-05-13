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

		// context path���� ������ ���δ�.
		String uri = req.getRequestURI();
		if(uri.indexOf("list.do")!=-1) {
			
		}else if(uri.indexOf("???.do")!=-1) {
			
		}
	}
	
	//�Խñ� ���
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� ����
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� ���ó��
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� ����
	protected void article(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� ����
	protected void updatForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� ����ó��
	protected void updatSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}
	//�Խñ� �����
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
	}

}
