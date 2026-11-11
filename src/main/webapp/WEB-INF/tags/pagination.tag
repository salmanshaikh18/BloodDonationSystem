<%@ tag description="Showing X-Y of Z, with Previous / Next links and page numbers. Hidden when everything fits on one page." pageEncoding="UTF-8" %>
<%@ attribute name="pageResult" required="true" type="org.springframework.data.domain.Page" description="The Spring Data page to show links for" %>
<%@ attribute name="path" required="true" description="Address of the list without parameters, for example /donors" %>
<%@ attribute name="paramName" description="Optional filter kept in every link, for example bloodGroup" %>
<%@ attribute name="paramValue" type="java.lang.Object" description="Value of that filter (empty = no filter)" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- Spring Data counts pages from 0; people count from 1 --%>
<c:set var="current" value="${pageResult.number + 1}"/>
<c:set var="total" value="${pageResult.totalPages}"/>
<c:set var="fromItem" value="${pageResult.number * pageResult.size + 1}"/>
<c:set var="toItem" value="${pageResult.number * pageResult.size + pageResult.numberOfElements}"/>

<c:if test="${total > 1}">
    <c:url var="previousUrl" value="${path}">
        <c:if test="${not empty paramValue}"><c:param name="${paramName}" value="${paramValue}"/></c:if>
        <c:if test="${current - 1 > 1}"><c:param name="page" value="${current - 1}"/></c:if>
    </c:url>
    <c:url var="nextUrl" value="${path}">
        <c:if test="${not empty paramValue}"><c:param name="${paramName}" value="${paramValue}"/></c:if>
        <c:param name="page" value="${current + 1}"/>
    </c:url>

    <nav class="pagination" aria-label="Pages">
        <p class="pagination-summary">
            Showing <strong>${fromItem}&ndash;${toItem}</strong> of <strong>${pageResult.totalElements}</strong>
        </p>

        <ul class="pagination-list">
            <li>
                <c:choose>
                    <c:when test="${pageResult.first}">
                        <span class="page-link page-link-disabled" aria-disabled="true">&larr; Previous</span>
                    </c:when>
                    <c:otherwise>
                        <a class="page-link" href="${previousUrl}" rel="prev">&larr; Previous</a>
                    </c:otherwise>
                </c:choose>
            </li>

            <%-- Page numbers: always the first and last page, plus the pages next to the current one --%>
            <c:forEach begin="1" end="${total}" var="number">
                <c:choose>
                    <c:when test="${number == 1 or number == total or (number >= current - 1 and number <= current + 1)}">
                        <c:url var="numberUrl" value="${path}">
                            <c:if test="${not empty paramValue}"><c:param name="${paramName}" value="${paramValue}"/></c:if>
                            <c:if test="${number > 1}"><c:param name="page" value="${number}"/></c:if>
                        </c:url>
                        <li class="pagination-number">
                            <a class="page-link" href="${numberUrl}" ${number == current ? 'aria-current="page"' : ''}>${number}</a>
                        </li>
                    </c:when>
                    <c:when test="${(number == 2 and current > 3) or (number == total - 1 and current < total - 2)}">
                        <li class="pagination-number"><span class="page-gap" aria-hidden="true">&hellip;</span></li>
                    </c:when>
                </c:choose>
            </c:forEach>

            <%-- Phones show "Page 2 of 5" instead of every number --%>
            <li class="pagination-compact">Page ${current} of ${total}</li>

            <li>
                <c:choose>
                    <c:when test="${pageResult.last}">
                        <span class="page-link page-link-disabled" aria-disabled="true">Next &rarr;</span>
                    </c:when>
                    <c:otherwise>
                        <a class="page-link" href="${nextUrl}" rel="next">Next &rarr;</a>
                    </c:otherwise>
                </c:choose>
            </li>
        </ul>
    </nav>
</c:if>
