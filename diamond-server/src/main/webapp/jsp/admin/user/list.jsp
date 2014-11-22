 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK" />
<title>Diamond������Ϣ����</title>
<script type="text/javascript">
   function confirmForDelete(){
       return window.confirm("��ȷ��Ҫɾ�����û���??");  
   }
   
   function changePassword(user,link){
       var newPass=window.prompt("�����������룺");
       if(newPass==null||newPass.length==0)
         return false;
       link.href=link.href+"&password="+newPass;
       return window.confirm("��ȷ��Ҫ��"+user+"�������޸�Ϊ"+newPass+"��??");  
   }
  
</script>
</head>
<body>
<c:import url="/jsp/common/message.jsp"/>
<center><h1><strong>�û�����</strong></h1></center>
   <p align='center'>
     <c:if test="${userMap!=null}">
      <table border='1' width="800">
          <tr>
              <td>�û���</td>
              <td>����</td>
              <td>����</td>
          </tr>
          <c:forEach items="${userMap}" var="user">
            <tr>
               <td>
                  <c:out value="${user.key}"/>
               </td>
              <td>
                  <c:out value="${user.value}" />
               </td>
              <c:url var="changePasswordUrl" value="/admin.do" >
                  <c:param name="method" value="changePassword" />
                  <c:param name="userName" value="${user.key}" />
              </c:url>
              <c:url var="deleteUserUrl" value="/admin.do" >
                  <c:param name="method" value="deleteUser" />
                  <c:param name="userName" value="${user.key}" />
                  <c:param name="password" value="${user.value}" />
              </c:url>
              <td>
                 <a href="${changePasswordUrl}" onclick="return changePassword('${user.key}',this);">�޸�����</a>&nbsp;&nbsp;&nbsp;
                 <a href="${deleteUserUrl}" onclick="return confirmForDelete();">ɾ��</a>&nbsp;&nbsp;&nbsp;
              </td>
            </tr>
          </c:forEach>
       </table>
    </c:if>
  </p>
  <p align='center'>
    <a href="<c:url value='/jsp/admin/user/new.jsp' />">����û�</a> &nbsp;&nbsp;&nbsp;&nbsp;<a href="<c:url value='/admin.do?method=reloadUser' />">���¼����û���Ϣ</a>
  </p>
</body>
</html>