<%--

  Hello World Component component.

  

--%><%
%><%@include file="/apps/training/components/page/global.jsp"%>

<action:action actionClassName="com.virtusa.training.action.HelloWorldAction" bean="helloWorld" actionName="helloWorld"  />
HELLO ${helloWorld.message} ! to virtusa training