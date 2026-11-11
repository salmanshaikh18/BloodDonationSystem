<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ui" tagdir="/WEB-INF/tags" %>

<%-- One form for both Register (no donorId) and Edit (donorId set) --%>
<c:set var="editing" value="${not empty donorId}"/>
<c:choose>
    <c:when test="${editing}">
        <c:url var="formAction" value="/donors/${donorId}"/>
        <c:url var="cancelUrl" value="/donors/${donorId}"/>
    </c:when>
    <c:otherwise>
        <c:url var="formAction" value="/donors"/>
        <c:url var="cancelUrl" value="/donors"/>
    </c:otherwise>
</c:choose>

<ui:page title="${editing ? 'Edit donor' : 'Register donor'}" activeNav="${editing ? 'donors' : 'register'}">
<div class="form-page">

    <a class="back-link" href="${cancelUrl}">&larr; ${editing ? 'Back to donor' : 'All donors'}</a>

    <div class="card">
        <header class="form-header">
            <h1 class="page-title">${editing ? 'Edit donor details' : 'Register a donor'}</h1>
            <p class="page-subtitle">Fields marked <span class="required">*</span> are required.</p>
        </header>

        <form:form modelAttribute="donorForm" action="${formAction}" method="post" cssClass="form">

            <fieldset class="form-section">
                <legend class="form-section-title">Personal details</legend>

                <div class="field">
                    <form:label path="fullName" cssClass="label">Full name <span class="required">*</span></form:label>
                    <form:input path="fullName" cssClass="input" cssErrorClass="input input-error"
                                autocomplete="name" maxlength="100" required="required"/>
                    <form:errors path="fullName" cssClass="field-error" element="p"/>
                </div>

                <div class="field-row">
                    <div class="field">
                        <form:label path="age" cssClass="label">Age <span class="required">*</span></form:label>
                        <form:input path="age" type="number" min="18" max="65" inputmode="numeric"
                                    cssClass="input" cssErrorClass="input input-error" required="required"/>
                        <p class="hint">18 to 65 years</p>
                        <form:errors path="age" cssClass="field-error" element="p"/>
                    </div>

                    <div class="field">
                        <form:label path="bloodGroup" cssClass="label">Blood group <span class="required">*</span></form:label>
                        <form:select path="bloodGroup" cssClass="input" cssErrorClass="input input-error" required="required">
                            <form:option value="">Select blood group</form:option>
                            <form:options items="${bloodGroups}" itemLabel="label"/>
                        </form:select>
                        <form:errors path="bloodGroup" cssClass="field-error" element="p"/>
                    </div>
                </div>

                <fieldset class="field">
                    <legend class="label">Gender <span class="required">*</span></legend>
                    <div class="radio-group">
                        <c:forEach items="${genders}" var="option">
                            <label class="radio">
                                <form:radiobutton path="gender" value="${option}" required="required"/>
                                <span>${option.label}</span>
                            </label>
                        </c:forEach>
                    </div>
                    <form:errors path="gender" cssClass="field-error" element="p"/>
                </fieldset>
            </fieldset>

            <fieldset class="form-section">
                <legend class="form-section-title">Contact</legend>

                <div class="field-row">
                    <div class="field">
                        <form:label path="phone" cssClass="label">Mobile number <span class="required">*</span></form:label>
                        <form:input path="phone" type="tel" inputmode="numeric" autocomplete="tel-national"
                                    placeholder="98765 43210" maxlength="16"
                                    cssClass="input" cssErrorClass="input input-error" required="required"/>
                        <form:errors path="phone" cssClass="field-error" element="p"/>
                    </div>

                    <div class="field">
                        <form:label path="email" cssClass="label">Email</form:label>
                        <form:input path="email" type="email" autocomplete="email" maxlength="120"
                                    cssClass="input" cssErrorClass="input input-error"/>
                        <form:errors path="email" cssClass="field-error" element="p"/>
                    </div>
                </div>

                <div class="field">
                    <form:label path="city" cssClass="label">City <span class="required">*</span></form:label>
                    <form:input path="city" autocomplete="address-level2" maxlength="60"
                                cssClass="input" cssErrorClass="input input-error" required="required"/>
                    <form:errors path="city" cssClass="field-error" element="p"/>
                </div>
            </fieldset>

            <fieldset class="form-section">
                <legend class="form-section-title">Donation</legend>

                <div class="field">
                    <form:label path="lastDonationDate" cssClass="label">Last donation date</form:label>
                    <form:input path="lastDonationDate" type="date" max="${today}"
                                cssClass="input" cssErrorClass="input input-error"/>
                    <p class="hint">Leave empty if they have never donated.</p>
                    <form:errors path="lastDonationDate" cssClass="field-error" element="p"/>
                </div>

                <label class="checkbox">
                    <form:checkbox path="available"/>
                    <span>
                        <span class="checkbox-title">Available to donate</span>
                        <span class="hint">Untick when the donor can't be contacted for now, for example while travelling or unwell.</span>
                    </span>
                </label>
            </fieldset>

            <div class="form-actions">
                <a class="btn btn-secondary" href="${cancelUrl}">Cancel</a>
                <button class="btn btn-primary" type="submit">${editing ? 'Save changes' : 'Register donor'}</button>
            </div>

        </form:form>
    </div>

</div>
</ui:page>
