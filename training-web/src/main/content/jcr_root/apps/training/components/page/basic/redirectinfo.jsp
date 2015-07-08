<%@ include file="/apps/training/components/page/global.jsp" %><%
%><body>
<%-- Display the following message in the author mode --%>
    <c:if test="${ func:isAuthorMode( pageContext ) }">
        <h1>This Page Is Redirected</h1>
        <div>
            <c:set var="redirect" value="${ pedfunc:getRedirectUrl( currentPage ) }" />
            This page redirects to: <a href="${ redirect }">${ redirect }</a>
        </div>
    </c:if>
</body>