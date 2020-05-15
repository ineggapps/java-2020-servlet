package com.photo;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.util.MyUploadServlet;

@WebServlet("/photo/*")
public class PhotoServlet extends MyUploadServlet{
	
	private static final String VIEWS="/WEB-INF/views";
	
	@Override
	protected void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI();
		if(uri.indexOf("list.do")!=-1) {
			list(req,resp);
		}else if(uri.indexOf("created.do")!=-1) {
			createdForm(req, resp);
		}else if(uri.indexOf("created_ok.do")!=-1) {
			createdSubmit(req, resp);
		}else if(uri.indexOf("update.do")!=-1) {
			updateForm(req, resp);
		}else if(uri.indexOf("update_ok.do")!=-1) {
			updateSubmit(req, resp);
		}else if(uri.indexOf("delete.do")!=-1) {
			delete(req, resp);
		}else if(uri.indexOf("deletePhoto.do")!=-1) {
			deletePhoto(req, resp);
		}
	}
	
	protected void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void createdForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void createdSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void updateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void updateSubmit(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	protected void deletePhoto(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	}
	
}
