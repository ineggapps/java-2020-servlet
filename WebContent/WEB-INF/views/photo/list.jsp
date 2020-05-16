<%@page import="com.photo.PhotoDTO"%>
<%@page import="java.util.List"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%
   String cp = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>spring</title>
<link rel="icon" href="data:;base64,iVBORw0KGgo=">
<link rel="stylesheet" href="<%=cp%>/resource/css/style.css" type="text/css">
<link rel="stylesheet" href="<%=cp%>/resource/css/layout.css" type="text/css">
<link rel="stylesheet" href="<%=cp%>/resource/jquery/css/smoothness/jquery-ui.min.css" type="text/css">
<style type="text/css">
	.gallery{
		text-align:center;
	}
	.gallery li{
		display:inline-block;
		
	}
	.gallery .row_image{
		width:220px;
		height:220px; 
		border-radius:8px;
		background-repeat:no-repeat;
		background-position:center center;
		background-size:200%;
		transition:background-size linear 0.2s; 
	}
	.gallery .row_image:hover{
		background-size:300%;
	}
	.gallery .row_image,
	.gallery .row_subject {
		cursor:pointer;
	}
</style>

<script type="text/javascript" src="<%=cp%>/resource/js/util.js"></script>
<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery.min.js"></script>
<script type="text/javascript">
	function searchList() {
		var f=document.searchForm;
		f.submit();
	}
	
	function article(num){
		const url = "${articleUrl}${query}&num="+num;
		location.href=url;
	}
</script>
</head>
<body>

<div class="header">
    <jsp:include page="/WEB-INF/views/layout/header.jsp"></jsp:include>
</div>
	
<div class="container">
    <div class="body-container" style="width: 700px;">
        <div class="body-title">
            <h3><span style="font-family: Webdings">2</span> 포토갤러리 </h3>
        </div>
        
        <div>
			  <ul class="gallery"> 
			 <c:forEach var="dto" items="${list}">
			      <li>
			      	<div class="photo">
			      		<div class="row_image" style="background-image:url('${image_path}/${dto.imageFilename}')"
			      		onclick="article(${dto.num})"></div>
						<div class="row_subject" onclick="article(${dto.num})">${dto.subject}</div>
			      	</div>
			      </li>
			 </c:forEach>
			  </ul>
			<table style="width: 100%; margin: 10px auto; border-spacing: 0px;">
				<tr>
					<td>
						<c:if test="${dataCount==0}">
							게시물이 존재하지 않습니다.
						</c:if>
						<c:if test="${dataCount>0}">
							${paging}
						</c:if>
					</td>
				</tr>
				<tr>
					<td align="right" width="100">
			          <button type="button" class="btn" onclick="javascript:location.href='<%=cp%>/photo/created.do${query}';">사진 올리기</button>
			     	</td>
			   </tr>
			</table>
        </div>

    </div>
</div>

<div class="footer">
    <jsp:include page="/WEB-INF/views/layout/footer.jsp"></jsp:include>
</div>

<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery-ui.min.js"></script>
<script type="text/javascript" src="<%=cp%>/resource/jquery/js/jquery.ui.datepicker-ko.js"></script>
</body>
</html>