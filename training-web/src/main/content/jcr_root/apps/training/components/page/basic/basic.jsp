<%@ include file="/apps/training/components/page/global.jsp" %>
<cq:include script="redirect.jsp" />
<!DOCTYPE html>
<!--[if lte IE 7]> <html lang="en" xmlns="http://www.w3.org/1999/xhtml" class="ie7"> <![endif]-->  
<!--[if IE 8]>     <html lang="en" xmlns="http://www.w3.org/1999/xhtml" class="ie8"> <![endif]-->
<!--[if IE 9]>     <html lang="en" xmlns="http://www.w3.org/1999/xhtml" class="ie9"> <![endif]-->    
<!--[if !IE]><!--> <html lang="en" xmlns="http://www.w3.org/1999/xhtml"> <!--<![endif]--> 
    <cq:include script="head.jsp" />
    <c:choose>
        <c:when test="${ func:isRedirect( currentPage ) }">
            <cq:include script="redirectinfo.jsp" />
        </c:when>
        <c:otherwise>
            <cq:include script="body.jsp" />
        </c:otherwise>
    </c:choose>
</html>