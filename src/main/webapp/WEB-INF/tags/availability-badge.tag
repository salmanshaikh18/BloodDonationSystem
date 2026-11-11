<%@ tag description="Whether a donor can donate now, later, or is not available" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="donor" required="true" type="com.group2.blooddonation.model.Donor" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>
<c:choose>
    <c:when test="${not donor.available}">
        <span class="status status-off">Not available</span>
    </c:when>
    <c:when test="${donor.eligibleToDonate}">
        <span class="status status-ready">Can donate now</span>
    </c:when>
    <c:otherwise>
        <span class="status status-waiting">Can donate from <ui:date value="${donor.nextEligibleDate}"/></span>
    </c:otherwise>
</c:choose>
