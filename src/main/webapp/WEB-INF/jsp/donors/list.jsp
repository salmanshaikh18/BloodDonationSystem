<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<c:set var="donorCount" value="${fn:length(donors)}"/>

<ui:page title="Donors" activeNav="donors">

    <header class="page-header">
        <div>
            <h1 class="page-title">Donors</h1>
            <p class="page-subtitle">
                <c:choose>
                    <c:when test="${empty selectedBloodGroup}">
                        ${donorCount} registered ${donorCount == 1 ? 'donor' : 'donors'}
                    </c:when>
                    <c:otherwise>
                        ${donorCount} ${donorCount == 1 ? 'donor' : 'donors'} with blood group ${selectedBloodGroup.label}
                    </c:otherwise>
                </c:choose>
            </p>
        </div>
        <a class="btn btn-primary" href="<c:url value='/donors/new'/>">Register donor</a>
    </header>

    <nav class="chip-list filter-bar" aria-label="Filter by blood group">
        <a class="chip" href="<c:url value='/donors'/>" ${empty selectedBloodGroup ? 'aria-current="page"' : ''}>All</a>
        <c:forEach items="${bloodGroups}" var="group">
            <c:url var="filterUrl" value="/donors">
                <c:param name="bloodGroup" value="${group}"/>
            </c:url>
            <a class="chip" href="${filterUrl}" ${selectedBloodGroup == group ? 'aria-current="page"' : ''}>${group.label}</a>
        </c:forEach>
    </nav>

    <c:choose>
        <c:when test="${empty donors}">
            <div class="empty-state">
                <p class="empty-state-title">No donors found</p>
                <p class="empty-state-text">
                    <c:choose>
                        <c:when test="${empty selectedBloodGroup}">Nobody has been registered yet.</c:when>
                        <c:otherwise>No registered donor has blood group ${selectedBloodGroup.label}.</c:otherwise>
                    </c:choose>
                </p>
                <div class="empty-state-actions">
                    <a class="btn btn-primary" href="<c:url value='/donors/new'/>">Register a donor</a>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="data-table-wrap">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th scope="col">Name</th>
                        <th scope="col">Blood group</th>
                        <th scope="col">City</th>
                        <th scope="col">Mobile</th>
                        <th scope="col">Last donation</th>
                        <th scope="col">Status</th>
                        <th scope="col"><span class="sr-only">Actions</span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${donors}" var="donor">
                        <c:url var="donorUrl" value="/donors/${donor.id}"/>
                        <tr>
                            <td data-label="Name">
                                <a class="data-table-link" href="${donorUrl}"><c:out value="${donor.fullName}"/></a>
                            </td>
                            <td data-label="Blood group"><ui:blood-group-badge group="${donor.bloodGroup}"/></td>
                            <td data-label="City"><c:out value="${donor.city}"/></td>
                            <td data-label="Mobile"><a href="tel:+91${donor.phone}">${donor.phone}</a></td>
                            <td data-label="Last donation"><ui:date value="${donor.lastDonationDate}" fallback="Never"/></td>
                            <td data-label="Status"><ui:availability-badge donor="${donor}"/></td>
                            <td class="data-table-actions">
                                <a class="btn btn-secondary btn-sm" href="${donorUrl}">View</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>

</ui:page>
