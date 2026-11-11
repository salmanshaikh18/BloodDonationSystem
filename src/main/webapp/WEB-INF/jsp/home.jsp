<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<ui:page title="Home">

    <section class="hero">
        <div>
            <p class="hero-eyebrow">Every drop counts</p>
            <h1 class="hero-title">Find the right blood donor, fast.</h1>
            <p class="hero-lead">
                Keep donor details in one place and search by blood group to find donors
                who can give blood today.
            </p>
            <div class="hero-actions">
                <a class="btn btn-primary" href="<c:url value='/donors/new'/>">Register a donor</a>
                <a class="btn btn-secondary" href="<c:url value='/donors'/>">View all donors</a>
            </div>
        </div>

        <form class="card quick-search" action="<c:url value='/search'/>" method="get">
            <h2 class="card-title">Need blood?</h2>
            <label class="label" for="quick-blood-group">Patient's blood group</label>
            <div class="quick-search-row">
                <select class="input" id="quick-blood-group" name="bloodGroup" required>
                    <option value="">Select blood group</option>
                    <c:forEach items="${bloodGroups}" var="group">
                        <option value="${group}">${group.label}</option>
                    </c:forEach>
                </select>
                <button class="btn btn-primary" type="submit">Find donors</button>
            </div>
            <p class="hint">Lists donors with a compatible blood group who can donate today.</p>
        </form>
    </section>

    <section class="stats" aria-label="Donor numbers">
        <div class="stat-card">
            <span class="stat-value">${stats.totalDonors}</span>
            <span class="stat-label">Registered donors</span>
        </div>
        <div class="stat-card">
            <span class="stat-value">${stats.availableDonors}</span>
            <span class="stat-label">Available to donate</span>
        </div>
        <div class="stat-card stat-card-highlight">
            <span class="stat-value">${stats.readyDonors}</span>
            <span class="stat-label">Can donate today</span>
        </div>
    </section>

    <section class="section">
        <div class="section-header">
            <h2 class="section-title">Donors by blood group</h2>
            <a class="section-link" href="<c:url value='/donors'/>">View all</a>
        </div>
        <div class="group-grid">
            <c:forEach items="${bloodGroups}" var="group">
                <c:url var="groupUrl" value="/donors">
                    <c:param name="bloodGroup" value="${group}"/>
                </c:url>
                <c:set var="count" value="${stats.donorsPerBloodGroup[group]}"/>
                <a class="group-tile" href="${groupUrl}">
                    <span class="group-tile-label">${group.label}</span>
                    <span class="group-tile-count">${count} ${count == 1 ? 'donor' : 'donors'}</span>
                </a>
            </c:forEach>
        </div>
    </section>

    <section class="section">
        <div class="section-header">
            <h2 class="section-title">Recently registered</h2>
        </div>
        <c:choose>
            <c:when test="${empty recentDonors}">
                <div class="empty-state">
                    <p class="empty-state-title">No donors yet</p>
                    <p class="empty-state-text">Register the first donor to get started.</p>
                    <div class="empty-state-actions">
                        <a class="btn btn-primary" href="<c:url value='/donors/new'/>">Register a donor</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <ul class="donor-list">
                    <c:forEach items="${recentDonors}" var="donor">
                        <li>
                            <a class="donor-list-item" href="<c:url value='/donors/${donor.id}'/>">
                                <ui:blood-group-badge group="${donor.bloodGroup}"/>
                                <span class="donor-list-text">
                                    <span class="donor-list-name"><c:out value="${donor.fullName}"/></span>
                                    <span class="donor-list-meta"><c:out value="${donor.city}"/></span>
                                </span>
                                <ui:availability-badge donor="${donor}"/>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </c:otherwise>
        </c:choose>
    </section>

</ui:page>
