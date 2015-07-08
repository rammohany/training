<%@ include file="/apps/training/components/page/global.jsp" %>
<%@ page import="com.day.cq.wcm.foundation.ELEvaluator" %>

<c:if test="${ !func:isAuthorMode( pageContext ) && func:isRedirect( currentPage ) }">
<%    // try to resolve the redirect target in order to the the title
    String path = properties.get("redirectTarget", "");
    // resolve variables in path
    path = ELEvaluator.evaluate(path, slingRequest, pageContext);
    if (path.length() > 0) {
        Page target = pageManager.getPage(path);
        String title = target == null ? path : target.getTitle();
        %><p class="cq-redirect-notice">This page redirects to <a href="<%= xssAPI.getValidHref(path) %>.html"><%= xssAPI.filterHTML(title) %></a></p><%
    }
%>
</c:if>