package com.photo;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.util.DBConn;

public class PhotoDAO {
	private Connection conn = DBConn.getConnection();
	
	public int insertPhoto(PhotoDTO dto) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO photo(userId, subject, content, imageFilename) VALUES(?, ?, ?, ?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getUserId());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getImageFilename());
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
