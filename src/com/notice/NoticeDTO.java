package com.notice;

public class NoticeDTO {
	private int num;
	private int listNum;
	private int notice;
	private String userId;
	private String userName;
	private String subject;
	private String content;
	private String created;
	private int hitCount;
	// 파일
	private String saveFilename;
	private String originalFilename;
	private long fileSize;// File의 크기는 long형으로 반환된다.
	// 공지
	private long gap;// 시간계산

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getListNum() {
		return listNum;
	}

	public void setListNum(int listNum) {
		this.listNum = listNum;
	}

	public int getNotice() {
		return notice;
	}

	public void setNotice(int notice) {
		this.notice = notice;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public String getSaveFilename() {
		return saveFilename;
	}

	public void setSaveFilename(String saveFilename) {
		this.saveFilename = saveFilename;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getGap() {
		return gap;
	}

	public void setGap(long gap) {
		this.gap = gap;
	}

	@Override
	public String toString() {
		return "NoticeDTO [num=" + num + ", listNum=" + listNum + ", notice=" + notice + ", userId=" + userId
				+ ", userName=" + userName + ", subject=" + subject + ", content=" + content + ", created=" + created
				+ ", hitCount=" + hitCount + ", saveFilename=" + saveFilename + ", originalFilename=" + originalFilename
				+ ", fileSize=" + fileSize + ", gap=" + gap + "]";
	}
	
	

}
