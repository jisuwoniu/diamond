<html xmlns="http://www.w3.org/1999/xhtml">
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond����̨</title>
</head>
<body>
<ul>
  <li><a href="config/list.jsp" target="rightFrame">������Ϣ����</a></li>
  <li><a href="<c:url value='/admin.do?method=listUser'/>" target="rightFrame">Ȩ�޹���</a></li>
  <li><a href="<c:url value='/admin.do?method=getRefuseRequestCount'/>" target="rightFrame">���þܾ�����</a></li>
  <c:url var="logoutUrl" value="/login.do">
     <c:param name="method" value="logout"/>
  </c:url>
  <li><a href="${logoutUrl}" target="_top">�˳�</a></li>
</ul>
</body>
</html>