<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:page title="Page not found">
    <div class="error-page">
        <p class="error-code">404</p>
        <h1 class="error-title">We couldn't find that page</h1>
        <p class="error-text">The donor may have been removed, or the link may be wrong.</p>
        <div class="error-actions">
            <a class="btn btn-primary" href="<c:url value='/'/>">Go to home</a>
            <a class="btn btn-secondary" href="<c:url value='/donors'/>">View donors</a>
        </div>
    </div>
</ui:page>
