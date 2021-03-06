package com.notice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.util.DBConn;

public class NoticeDAO {
	public Connection conn = DBConn.getConnection();

	// 게시물 삽입
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
			pstmt.setLong(7, dto.getFileSize());

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

	// 게시물 건수 조회하기
	public int dataCount() {
		int result = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT NVL(COUNT(num),0) FROM notice";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				result = rs.getInt(1);
			}
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

	public int dataCount(String condition, String keyword) {
		int result = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT NVL(COUNT(*),0) FROM notice n ");

		try {
			if (condition.equalsIgnoreCase("created")) {
				sql.append(" WHERE TO_CHAR(created,'YYYYMMDD') = ? ");
			} else if (condition.equalsIgnoreCase("username")) {
				sql.append(" JOIN member1 m1 ON n.userId = m1.userId ");
				sql.append(" WHERE INSTR(username, ?) = 1 ");
			} else {
				sql.append(" WHERE INSTR(" + condition + ", ?) > 0 ");
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

	// 게시물 목록 조회하기
	public List<NoticeDTO> listNotice(int offset, int rows) {
		List<NoticeDTO> list = new ArrayList<>();
		String sql = "SELECT num, notice, n.userId, userName, subject, content, saveFilename, hitCount, created FROM notice n "
				+ "JOIN member1 m1 ON n.userId = m1.userId "
				+ " ORDER BY num DESC OFFSET ? ROWS FETCH FIRST ? ROWS ONLY";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, rows);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				NoticeDTO dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setNotice(rs.getInt("notice"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setSaveFilename(rs.getString("saveFilename"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
				list.add(dto);
			}
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

		return list;
	}

	// 게시글 조회하기 (검색글 조회)
	public List<NoticeDTO> listNotice(int offset, int rows, String condition, String keyword) {
		List<NoticeDTO> list = new ArrayList<NoticeDTO>();
		// 검색 방식: created, name, 기타

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder(
				"SELECT num, notice, n.userId, userName, subject, content, saveFilename, hitCount, created FROM notice n "
						+ "JOIN member1 m1 ON n.userId = m1.userId " + " WHERE ");
		try {
			if (condition.equalsIgnoreCase("created")) {
				sql.append("TO_CHAR(created, 'YYYYMMDD') = ? ");
			} else if (condition.equalsIgnoreCase("name")) {
				sql.append("INSTR(name, ?) = 1 ");
			} else {
				sql.append("INSTR(" + condition + ", ?) > 0 ");
			}
			sql.append(" ORDER BY num DESC OFFSET ? ROWS FETCH FIRST ? ROWS ONLY");

			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, keyword);
			pstmt.setInt(2, offset);
			pstmt.setInt(3, rows);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				NoticeDTO dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setNotice(rs.getInt("notice"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setSaveFilename(rs.getString("saveFilename"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e2) {
				}
			}
		}

		return list;
	}

	// 상단 박제 공지글 가져오기
	public List<NoticeDTO> listNotice() {
		List<NoticeDTO> list = new ArrayList<>();
		String sql = "SELECT num, notice, n.userId, userName, subject, content, saveFilename, hitCount, TO_CHAR(created,'YYYY-MM-DD') created FROM notice n "
				+ "JOIN member1 m1 ON n.userId = m1.userId " + " WHERE notice=1 ORDER BY num DESC ";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				NoticeDTO dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setNotice(rs.getInt("notice"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setSaveFilename(rs.getString("saveFilename"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));
				list.add(dto);
			}
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

		return list;
	}

	// 조회수 올리기
	public int updateHitCount(int num) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "UPDATE notice SET hitCount=hitCount+1 WHERE num = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
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

	// 공지게시글 읽기
	public NoticeDTO readNotice(int num) {
		NoticeDTO dto = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT num, notice, n.userId, userName, subject, content, saveFilename, originalFilename, fileSize, hitCount, created "
				+ " FROM notice n JOIN member1 m1 ON n.userId = m1.userId WHERE num = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setNotice(rs.getInt("notice"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
				dto.setSaveFilename(rs.getString("saveFilename"));
				dto.setOriginalFilename(rs.getString("originalFilename"));
				dto.setFileSize(rs.getLong("fileSize"));
				dto.setHitCount(rs.getInt("hitCount"));
				dto.setCreated(rs.getString("created"));

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

		return dto;
	}

	public NoticeDTO preReadNotice(int num, String condition, String keyword) {
		NoticeDTO dto = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT num, subject FROM notice ");
		int idx = 1;
		try {

			if (keyword != null && keyword.length() > 0) {
				// 검색 조건이 있을 때
				if (condition.equalsIgnoreCase("created")) {
					sql.append(" WHERE TO_CHAR(created, 'YYYYMMDD') = ? ");
				} else if (condition.equalsIgnoreCase("userName")) {
					sql.append(" JOIN member1 m1 ON bbs.userId = m1.userId ");
					sql.append(" WHERE INSTR(userName, ?) = 1 ");
				} else {
					// 기타
					sql.append(" WHERE INSTR(" + condition + ", ?) > 0 ");
				}
				sql.append(" AND");
			} else {
				sql.append(" WHERE ");
			}
			sql.append(" num > ? ");
			sql.append(" ORDER BY num OFFSET 0 ROWS  FETCH FIRST 1 ROWS ONLY");

			pstmt = conn.prepareStatement(sql.toString());
			if (keyword != null && keyword.length() > 0) {
				pstmt.setString(idx++, keyword);
			}
			pstmt.setInt(idx++, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setSubject(rs.getString("subject"));
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
		return dto;
	}

	public NoticeDTO nextReadNotice(int num, String condition, String keyword) {
		NoticeDTO dto = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT num, subject FROM notice ");
		int idx = 1;
		try {

			if (keyword != null && keyword.length() > 0) {
				// 검색 조건이 있을 때
				if (condition.equalsIgnoreCase("created")) {
					sql.append(" WHERE TO_CHAR(created, 'YYYYMMDD') = ? ");
				} else if (condition.equalsIgnoreCase("userName")) {
					sql.append(" JOIN member1 m1 ON bbs.userId = m1.userId ");
					sql.append(" WHERE INSTR(userName, ?) = 1 ");
				} else {
					// 기타
					sql.append(" WHERE INSTR(" + condition + ", ?) > 0 ");
				}
				sql.append(" AND");
			} else {
				sql.append(" WHERE ");
			}
			sql.append(" num < ? ");
			sql.append(" ORDER BY num DESC OFFSET 0 ROWS  FETCH FIRST 1 ROWS ONLY");

			pstmt = conn.prepareStatement(sql.toString());
			if (keyword != null && keyword.length() > 0) {
				pstmt.setString(idx++, keyword);
			}
			pstmt.setInt(idx++, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				dto = new NoticeDTO();
				dto.setNum(rs.getInt("num"));
				dto.setSubject(rs.getString("subject"));
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
		return dto;
	}
	
	public int updateNotice(NoticeDTO dto) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "UPDATE notice SET notice=?, subject=?, content=?, saveFilename=?, originalFilename=?, filesize=? WHERE num = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			System.out.println(dto+"d.d.d.");
			pstmt.setInt(1, dto.getNotice());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
			pstmt.setString(4, dto.getSaveFilename());
			pstmt.setString(5, dto.getOriginalFilename());
			pstmt.setLong(6,dto.getFileSize());
			pstmt.setInt(7,dto.getNum());
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (Exception e2) {
				}
			}
		}
		return result;
	}
	
	public int deleteNotice(int num) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "DELETE FROM notice WHERE num = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			result = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
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
