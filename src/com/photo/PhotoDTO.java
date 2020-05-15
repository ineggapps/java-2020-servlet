package com.photo;

public class PhotoDTO {
	private int num;
	private int listNum;
	private String userId;
	private String userName;
	private String subject;
	private String content;
	private String imageFilename;
	private String created;

	public PhotoDTO() {
	}

	public PhotoDTO(String userId, String subject, String content, String imageFilename) {
		// insert할 때 간단하게 만들기
		this.userId = userId;
		this.subject = subject;
		this.content = content;
		this.imageFilename = imageFilename;
	}

	public PhotoDTO(int num, String userId, String subject, String content, String imageFilename, String created) {
		// DB에서 꺼내오는 경우
		this.num = num;
		this.userId = userId;
		this.subject = subject;
		this.content = content;
		this.imageFilename = imageFilename;
		this.created = created;
	}

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

	public String getImageFilename() {
		return imageFilename;
	}

	public void setImageFilename(String imageFilename) {
		this.imageFilename = imageFilename;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	@Override
	public String toString() {
		return "PhotoDTO [num=" + num + ", userId=" + userId + ", subject=" + subject + ", content=" + content
				+ ", imageFilename=" + imageFilename + ", created=" + created + "]";
	}

}
