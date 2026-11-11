<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:page title="Something went wrong">
    <div class="error-page">
        <p class="error-code">500</p>
        <h1 class="error-title">Something went wrong</h1>
        <p class="error-text">Please try again in a moment.</p>
        <div class="error-actions">
            <a class="btn btn-primary" href="<c:url value='/'/>">Go to home</a>
        </div>
    </div>
</ui:page>
