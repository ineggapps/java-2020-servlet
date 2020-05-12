package com.member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.util.DBConn;

public class MemberDAO {
	private Connection conn=DBConn.getConnection();
	
	public MemberDTO readMember(String userId) {
		MemberDTO dto=null;
		PreparedStatement pstmt=null;
		ResultSet rs=null;
		StringBuilder sb=new StringBuilder();
		
		try {
			sb.append("SELECT m1.userId, userName, userPwd,");
			sb.append("      enabled, created_date, modify_date,");
			sb.append("      TO_CHAR(birth, 'YYYY-MM-DD') birth, ");
			sb.append("      email, tel,");
			sb.append("      zip, addr1, addr2");
			sb.append("      FROM member1 m1");
			sb.append("      LEFT OUTER JOIN member2 m2 ");
			sb.append("      ON m1.userId=m2.userId");
			sb.append("      WHERE m1.userId=?");
			
			pstmt=conn.prepareStatement(sb.toString());
			pstmt.setString(1, userId);
			rs=pstmt.executeQuery();
			if(rs.next()) {
				dto=new MemberDTO();
				dto.setUserId(rs.getString("userId"));
				dto.setUserPwd(rs.getString("userPwd"));
				dto.setUserName(rs.getString("userName"));
				dto.setEnabled(rs.getInt("enabled"));
				dto.setCreated_date(rs.getString("created_date"));
				dto.setModify_date(rs.getString("modify_date"));
				dto.setBirth(rs.getString("birth"));
				dto.setTel(rs.getString("tel"));
				if(dto.getTel()!=null) {
					String[] ss=dto.getTel().split("-");
					if(ss.length==3) {
						dto.setTel1(ss[0]);
						dto.setTel2(ss[1]);
						dto.setTel3(ss[2]);
					}
				}
				dto.setEmail(rs.getString("email"));
				if(dto.getEmail()!=null) {
					String[] ss=dto.getEmail().split("@");
					if(ss.length==2) {
						dto.setEmail1(ss[0]);
						dto.setEmail2(ss[1]);
					}
				}
				dto.setZip(rs.getString("zip"));
				dto.setAddr1(rs.getString("addr1"));
				dto.setAddr2(rs.getString("addr2"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
				
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return dto;
	}
	
	public void insertMember(MemberDTO dto) throws Exception {
		PreparedStatement pstmt=null;
		String sql;
		
		try {
//			conn.setAutoCommit(false);
//			sql = "INSERT INTO member1(userId, userName, userPwd) VALUES(?, ?, ?)";
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, dto.getUserId());
//			pstmt.setString(2, dto.getUserName());
//			pstmt.setString(3, dto.getUserPwd());
//			pstmt.executeUpdate();
//			pstmt.close();
//			
//			sql = "INSERT INTO member2(userId, birth, email, tel, zip, addr1, addr2) VALUES(?, ?, ?, ?, ?, ?, ?)";
//			pstmt.setString(1, dto.getUserId());
//			pstmt.setString(2, dto.getBirth());
//			pstmt.setString(3, dto.getEmail());
//			pstmt.setString(4,dto.getTel());
//			pstmt.setString(5,dto.getZip());
//			pstmt.setString(6, dto.getAddr1());
//			pstmt.setString(7,dto.getAddr2());
//			pstmt.executeUpdate();
			
			sql = "INSERT ALL " + " INTO member1(userId, userName, userPwd) VALUES(?, ?, ?) " 
			+ " INTO member2(userId, birth, email, tel, zip, addr1, addr2) VALUES(?, ?, ?, ?, ?, ?, ?) SELECT * FROM DUAL ";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, dto.getUserId());
			pstmt.setString(2, dto.getUserName());
			pstmt.setString(3, dto.getUserPwd());
			pstmt.setString(4, dto.getUserId());
			pstmt.setString(5, dto.getBirth());
			pstmt.setString(6, dto.getEmail());
			pstmt.setString(7,dto.getTel());
			pstmt.setString(8,dto.getZip());
			pstmt.setString(9, dto.getAddr1());
			pstmt.setString(10,dto.getAddr2());
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
//			conn.setAutoCommit(true);
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	public void updateMember(MemberDTO dto) throws Exception {
		PreparedStatement pstmt=null;
		String sql;
		
		try {
			sql = "UPDATE member1 SET userPwd=?, modify_date=SYSDATE  WHERE userId=?";
			pstmt=conn.prepareStatement(sql);
			
			pstmt.setString(1, dto.getUserPwd());
			pstmt.setString(2, dto.getUserId());
			
			pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			sql = "UPDATE member2 SET birth=?, email=?, tel=?, zip=?, addr1=?, addr2=? WHERE userId=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, dto.getBirth());
			pstmt.setString(2, dto.getEmail());
			pstmt.setString(3, dto.getTel());
			pstmt.setString(4, dto.getZip());
			pstmt.setString(5, dto.getAddr1());
			pstmt.setString(6, dto.getAddr2());
			pstmt.setString(7, dto.getUserId());
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}
	
	public void deleteMember(String userId) throws Exception {
		PreparedStatement pstmt=null;
		String sql;
		
		try {
			sql="UPDATE member1 SET enabled=0 WHERE userId=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt=null;
			
			sql="DELETE FROM  member2 WHERE userId=?";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(pstmt!=null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}
}
