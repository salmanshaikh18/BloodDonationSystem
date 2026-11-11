<%@ tag description="Page layout shared by every page: head, header, success message, footer" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="activeNav" description="Highlights a menu item: donors, search or register" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Register blood donors and find suitable donors by blood group.">
    <meta name="theme-color" content="#c62828">
    <title><c:out value="${title}"/> | Blood Donation Management System</title>
    <link rel="icon" type="image/svg+xml" href="<c:url value='/images/logo.svg'/>">
    <link rel="stylesheet" href="<c:url value='/css/app.css'/>">
    <script src="<c:url value='/js/app.js'/>" defer></script>
</head>
<body>
<a class="skip-link" href="#main">Skip to content</a>
<%@ include file="/WEB-INF/jsp/layout/header.jspf" %>

<main id="main" class="page">
    <c:if test="${not empty successMessage}">
        <p class="alert alert-success" role="status"><c:out value="${successMessage}"/></p>
    </c:if>
    <jsp:doBody/>
</main>

<%@ include file="/WEB-INF/jsp/layout/footer.jspf" %>
</body>
</html>
