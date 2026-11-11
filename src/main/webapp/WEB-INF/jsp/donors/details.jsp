<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<c:url var="donorUrl" value="/donors/${donor.id}"/>

<ui:page title="${donor.fullName}" activeNav="donors">

    <a class="back-link" href="<c:url value='/donors'/>">&larr; All donors</a>

    <div class="details-layout">
        <article class="card">
            <header class="profile-header">
                <ui:blood-group-badge group="${donor.bloodGroup}" size="lg"/>
                <div class="profile-heading">
                    <h1 class="page-title"><c:out value="${donor.fullName}"/></h1>
                    <p class="page-subtitle">
                        ${donor.age} years &middot; ${donor.gender.label} &middot; <c:out value="${donor.city}"/>
                    </p>
                </div>
                <ui:availability-badge donor="${donor}"/>
            </header>

            <dl class="detail-list">
                <div class="detail-item">
                    <dt>Mobile</dt>
                    <dd><a href="tel:+91${donor.phone}">${donor.phone}</a></dd>
                </div>
                <div class="detail-item">
                    <dt>Email</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${empty donor.email}">Not given</c:when>
                            <c:otherwise><a href="mailto:<c:out value='${donor.email}'/>"><c:out value="${donor.email}"/></a></c:otherwise>
                        </c:choose>
                    </dd>
                </div>
                <div class="detail-item">
                    <dt>Last donation</dt>
                    <dd><ui:date value="${donor.lastDonationDate}" fallback="Never donated"/></dd>
                </div>
                <div class="detail-item">
                    <dt>Can donate again</dt>
                    <dd>
                        <c:choose>
                            <c:when test="${donor.eligibleToDonate}">Now</c:when>
                            <c:otherwise>From <ui:date value="${donor.nextEligibleDate}"/></c:otherwise>
                        </c:choose>
                    </dd>
                </div>
                <div class="detail-item">
                    <dt>Can give blood to</dt>
                    <dd class="chip-list">
                        <c:forEach items="${donor.bloodGroup.canDonateTo}" var="group">
                            <span class="chip chip-sm">${group.label}</span>
                        </c:forEach>
                    </dd>
                </div>
                <div class="detail-item">
                    <dt>Registered on</dt>
                    <dd><ui:date value="${donor.createdAt}"/></dd>
                </div>
            </dl>
        </article>

        <aside class="card actions-card" aria-label="Donor actions">
            <h2 class="card-title">Actions</h2>

            <a class="btn btn-secondary btn-block" href="${donorUrl}/edit">Edit details</a>

            <form method="post" action="${donorUrl}/availability">
                <input type="hidden" name="available" value="${not donor.available}">
                <button class="btn btn-secondary btn-block" type="submit">
                    ${donor.available ? 'Mark as not available' : 'Mark as available'}
                </button>
            </form>

            <form method="post" action="${donorUrl}/donations"
                  data-confirm="Record a blood donation made today by this donor?">
                <button class="btn btn-secondary btn-block" type="submit" ${donor.eligibleToDonate ? '' : 'disabled'}>
                    Record donation today
                </button>
            </form>
            <c:if test="${not donor.eligibleToDonate}">
                <p class="hint">Too soon after the last donation to donate again.</p>
            </c:if>

            <form method="post" action="${donorUrl}/delete"
                  data-confirm="Delete this donor? This can't be undone.">
                <button class="btn btn-danger btn-block" type="submit">Delete donor</button>
            </form>
        </aside>
    </div>

</ui:page>
