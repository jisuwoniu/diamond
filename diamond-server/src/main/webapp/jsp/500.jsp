<%@page contentType="text/html;charset=GBK" isErrorPage="true"%>
<html>
<head><title>���ִ���</title>
<script type="text/javascript">
  function displayErrorInfo()
  {
      var errorInfo=document.getElementById("errorInfo");
      errorInfo.style.display=(errorInfo.style.display=="none"?"":"none");
  }
</script>
</head>
<body>
     <p>�����������ڲ���������ϵ����Ա</p>
     <p><a onclick="displayErrorInfo();" href="#">�鿴�쳣��Ϣ</a></p>
     <div id="errorInfo" style="display:none"><%=exception.getMessage()%></div>
</body>
</html>