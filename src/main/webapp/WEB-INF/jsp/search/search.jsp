<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<c:url var="searchUrl" value="/search"/>

<ui:page title="Find donors" activeNav="search">

    <header class="page-header">
        <div>
            <h1 class="page-title">Find suitable donors</h1>
            <p class="page-subtitle">Choose the patient's blood group to see donors who can safely give blood.</p>
        </div>
    </header>

    <div class="search-layout">

        <form:form modelAttribute="criteria" action="${searchUrl}" method="get" cssClass="card search-form">
            <div class="field">
                <form:label path="bloodGroup" cssClass="label">Patient's blood group <span class="required">*</span></form:label>
                <form:select path="bloodGroup" cssClass="input" required="required">
                    <form:option value="">Select blood group</form:option>
                    <form:options items="${bloodGroups}" itemLabel="label"/>
                </form:select>
            </div>

            <div class="field">
                <form:label path="city" cssClass="label">City</form:label>
                <form:input path="city" cssClass="input" placeholder="Any city" autocomplete="address-level2"/>
            </div>

            <label class="checkbox">
                <form:checkbox path="includeCompatible"/>
                <span>
                    <span class="checkbox-title">Include compatible groups</span>
                    <span class="hint">For example, O- donors for an A+ patient.</span>
                </span>
            </label>

            <label class="checkbox">
                <form:checkbox path="onlyReady"/>
                <span>
                    <span class="checkbox-title">Only donors who can donate today</span>
                    <span class="hint">Available, and enough days since their last donation.</span>
                </span>
            </label>

            <button class="btn btn-primary btn-block" type="submit">Search donors</button>
        </form:form>

        <section class="search-results" aria-live="polite">
            <c:choose>

                <%-- Before searching: explain who can receive from whom --%>
                <c:when test="${empty criteria.bloodGroup}">
                    <div class="card">
                        <h2 class="card-title">Who can receive blood from whom?</h2>
                        <p class="hint">A patient can safely receive red blood cells from these donor groups.</p>
                        <table class="compat-table">
                            <thead>
                            <tr>
                                <th scope="col">Patient</th>
                                <th scope="col">Can receive from</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${bloodGroups}" var="group">
                                <tr>
                                    <td><ui:blood-group-badge group="${group}"/></td>
                                    <td>
                                        <span class="chip-list">
                                            <c:forEach items="${group.compatibleDonors}" var="donorGroup">
                                                <span class="chip chip-sm">${donorGroup.label}</span>
                                            </c:forEach>
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>

                <%-- Search results --%>
                <c:otherwise>
                    <c:set var="patientGroup" value="${criteria.bloodGroup}"/>
                    <c:set var="resultCount" value="${fn:length(results)}"/>

                    <div class="results-summary">
                        <h2 class="section-title">${resultCount} ${resultCount == 1 ? 'donor' : 'donors'} found</h2>
                        <p class="hint">
                            ${patientGroup.label} patients can receive from
                            <c:forEach items="${patientGroup.compatibleDonors}" var="group" varStatus="loop">
                                ${group.label}${loop.last ? '.' : ','}
                            </c:forEach>
                        </p>
                    </div>

                    <c:choose>
                        <c:when test="${empty results}">
                            <div class="empty-state">
                                <p class="empty-state-title">No matching donors</p>
                                <p class="empty-state-text">
                                    Try including compatible groups, clearing the city,
                                    or also showing donors who can't donate today.
                                </p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <ul class="result-list">
                                <c:forEach items="${results}" var="donor">
                                    <c:set var="sameGroup" value="${donor.bloodGroup == patientGroup}"/>
                                    <li class="result-card">
                                        <div class="result-main">
                                            <ui:blood-group-badge group="${donor.bloodGroup}"/>
                                            <div class="result-text">
                                                <div class="result-heading">
                                                    <a class="result-name" href="<c:url value='/donors/${donor.id}'/>"><c:out value="${donor.fullName}"/></a>
                                                    <span class="match-tag${sameGroup ? ' match-tag-exact' : ''}">${sameGroup ? 'Same group' : 'Compatible'}</span>
                                                </div>
                                                <p class="result-meta">
                                                    <c:out value="${donor.city}"/> &middot;
                                                    Last donated <ui:date value="${donor.lastDonationDate}" fallback="never"/>
                                                </p>
                                            </div>
                                        </div>
                                        <div class="result-actions">
                                            <ui:availability-badge donor="${donor}"/>
                                            <a class="btn btn-secondary btn-sm" href="tel:+91${donor.phone}">Call ${donor.phone}</a>
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </c:otherwise>
                    </c:choose>
                </c:otherwise>

            </c:choose>
        </section>
    </div>

</ui:page>
