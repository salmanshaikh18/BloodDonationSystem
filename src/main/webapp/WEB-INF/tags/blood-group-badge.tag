<%@ tag description="Blood group pill, for example A+" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="group" required="true" type="com.group2.blooddonation.model.BloodGroup" %>
<%@ attribute name="size" description="Set to lg for the large version used on the donor page" %>
<span class="blood-badge${size == 'lg' ? ' blood-badge-lg' : ''}" title="Blood group ${group.label}">${group.label}</span>
