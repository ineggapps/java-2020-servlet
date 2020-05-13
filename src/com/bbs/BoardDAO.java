package com.bbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.util.DBConn;
import com.util.MyUtil;

public class BoardDAO {
	private Connection conn = DBConn.getConnection();

	public int insertBoard(BoardDTO dto) {
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = "INSERT INTO bbs(userId, subject, content) VALUES(?,?,?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getUserId());
			pstmt.setString(2, dto.getSubject());
			pstmt.setString(3, dto.getContent());
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

	public int dataCount() {
		int result = 0;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT NVL(COUNT(*),0) FROM bbs";
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

	public int dataCount(String condition, String keyword) {
		int result = 0;

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder("SELECT NVL(COUNT(*),0) FROM bbs ");

		try {
			if (condition.equalsIgnoreCase("created")) {
				sql.append(" WHERE TO_CHAR(created,'YYYYMMDD') = ? ");
			} else if (condition.equalsIgnoreCase("username")) {
				sql.append(" JOIN member1 m1 ON bbs.userId = m1.userId ");
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

	public List<BoardDTO> listBoard(int offset, int rows) {
		List<BoardDTO> list = new ArrayList<BoardDTO>();

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT num, bbs.userId, userName, subject, content, hitCount, TO_CHAR(created,'YYYY-MM-DD') created FROM bbs "
				+ "JOIN member1 m1 ON bbs.userId = m1.userId "
				+ " ORDER BY num DESC OFFSET ? ROWS FETCH FIRST ? ROWS ONLY";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, rows);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				BoardDTO dto = new BoardDTO();
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
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

	public List<BoardDTO> listBoard(int offset, int rows, String condition, String keyword) {
		List<BoardDTO> list = new ArrayList<BoardDTO>();
		// 검색 방식: created, name, 기타

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = new StringBuilder(
				"SELECT num, bbs.userId, userName, subject, content, hitCount, TO_CHAR(created,'YYYY-MM-DD') created FROM bbs "
						+ "JOIN member1 m1 ON bbs.userId = m1.userId " + " WHERE ");
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
				BoardDTO dto = new BoardDTO();
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
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

	public BoardDTO readBoard(int num) {
		BoardDTO dto = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT num, bbs.userId, userName, subject, content, hitCount, TO_CHAR(created,'YYYY-MM-DD') created FROM bbs "
				+ " JOIN member1 m1 ON bbs.userId = m1.userId" + " WHERE num=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				dto = new BoardDTO();
				dto.setNum(rs.getInt("num"));
				dto.setUserId(rs.getString("userId"));
				dto.setUserName(rs.getString("userName"));
				dto.setSubject(rs.getString("subject"));
				dto.setContent(rs.getString("content"));
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

}
