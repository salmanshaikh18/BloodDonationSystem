<%@ tag description="Shows a date as 10 Aug 2026, or the fallback text when there is no date" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
        import="java.time.format.DateTimeFormatter,java.util.Locale" %>
<%@ attribute name="value" type="java.time.temporal.TemporalAccessor" %>
<%@ attribute name="fallback" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
    <c:when test="${empty value}">
        <c:out value="${fallback}"/>
    </c:when>
    <c:otherwise>
        <time datetime="${value}">${DateTimeFormatter.ofPattern('d MMM yyyy', Locale.ENGLISH).format(value)}</time>
    </c:otherwise>
</c:choose>
