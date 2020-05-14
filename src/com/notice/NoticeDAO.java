package com.notice;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.util.DBConn;

public class NoticeDAO {
	public Connection conn = DBConn.getConnection();
	
	public int insertNotice(NoticeDTO dto) {
		int result = 0;		
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO notice(notice, userId, subject, content, saveFilename, originalFilename, filesize) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, dto.getNotice());
			pstmt.setString(2, dto.getUserId());
			pstmt.setString(3, dto.getSubject());
			pstmt.setString(4, dto.getContent());
			pstmt.setString(5, dto.getSaveFilename());
			pstmt.setString(6, dto.getOriginalFilename());
			pstmt.setLong(7,dto.getFileSize());
			
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return result;
	}
	
}
