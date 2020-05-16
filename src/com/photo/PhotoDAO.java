package com.photo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		return result;
	}

	//검색하지 않았을 경우 데이터 개수 불러오기
	public int dataCount() {
		int result = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT NVL(COUNT(num),0) FROM photo";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}

		return result;
	}

	//검색했을 경우 데이터 개수 불러오기
	public int dataCount(String condition, String keyword) {
		int result = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT NVL(COUNT(num),0) FROM photo ");
		try {

			if (condition.equalsIgnoreCase("created")) {
				keyword = keyword.replaceAll("-", "");
				sql.append("WHERE TO_CHAR(created,'YYYYMMDD') = ? ");
			} else if (condition.equalsIgnoreCase("userName")) {
				sql.append("WHERE INSTR(username, ?) = 1 ");
			} else {
				sql.append("WHERE INSTR(" + condition + ", ?) > 0");
			}
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, keyword);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}

		return result;
	}
	
	public List<PhotoDTO> listPhoto(int offset, int rows){
		List<PhotoDTO> list = new ArrayList<PhotoDTO>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT num, p.userId, userName, subject, content, imageFilename, TO_CHAR(created, 'YYYY-MM-DD') created FROM photo p "
				+ " JOIN member1 m1 ON p.userId = m1.userId "
				+ " ORDER BY num DESC OFFSET ? ROWS FETCH FIRST ? ROWS ONLY";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, rows);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int num = rs.getInt("num");
				String userId = rs.getString("userId");
				String userName = rs.getString("userName");
				String subject = rs.getString("subject");
				String content = rs.getString("content");
				String imageFilename = rs.getString("imageFilename");
				String created = rs.getString("created");
				list.add(new PhotoDTO(num, userId, userName, subject, content, imageFilename, created));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return list;
	}
	
	public List<PhotoDTO> listPhoto(int offset, int rows, String condition, String keyword){
		List<PhotoDTO> list = new ArrayList<PhotoDTO>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT num, p.userId, userName, subject, content, imageFilename, TO_CHAR(created, 'YYYY-MM-DD') created FROM photo p ");
		sql.append(" JOIN member1 m1 ON p.userId = m1.userId ");
		try {
			if(condition.equalsIgnoreCase("created")) {
				keyword = keyword.replaceAll("-", "");
				sql.append(" WHERE TO_CHAR(created,'YYYYMMDD') = ? ");
			}else if(condition.equalsIgnoreCase("userName")) {
				sql.append(" WHERE INSTR(userName, ?) > 0 ");
			}else {
				sql.append(" WHERE INSTR(" + condition + ", ?) > 0");
			}
			sql.append(" ORDER BY num DESC OFFSET ? ROWS FETCH FIRST ? ROWS ONLY ");

			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, keyword);
			pstmt.setInt(2, offset);
			pstmt.setInt(3, rows);
			
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int num = rs.getInt("num");
				String userId = rs.getString("userId");
				String userName = rs.getString("userName");
				String subject = rs.getString("subject");
				String content = rs.getString("content");
				String imageFilename = rs.getString("imageFilename");
				String created = rs.getString("created");
				list.add(new PhotoDTO(num, userId, userName, subject, content, imageFilename, created));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return list;
	}
	
	public PhotoDTO readPhoto(int num) {
		PhotoDTO dto = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT num, p.userId, userName, subject, content, imageFilename, TO_CHAR(created, 'YYYY-MM-DD') created FROM photo p "
				+" JOIN member1 m1 ON p.userId = m1.userId " 
				+ " WHERE num = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()) {
//				num = rs.getInt("num");
				String userId = rs.getString("userId");
				String userName = rs.getString("userName");
				String subject = rs.getString("subject");
				String content = rs.getString("content");
				String imageFilename = rs.getString("imageFilename");
				String created = rs.getString("created");
				dto = new PhotoDTO(num, userId, userName, subject, content, imageFilename, created);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		return dto;
	}
	
	public int updatePhoto(PhotoDTO dto) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "UPDATE photo SET subject=?, content=?, imageFilename=? WHERE num=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getSubject());
			pstmt.setString(2, dto.getContent());
			pstmt.setString(3, dto.getImageFilename());
			pstmt.setInt(4, dto.getNum());
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
	
	public int deletePhoto(int num) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "DELETE FROM photo WHERE num = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1,num);
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
