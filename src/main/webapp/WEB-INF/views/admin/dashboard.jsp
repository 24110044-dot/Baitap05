<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../layout/header.jspf" %>
<div class="page-heading">
	<div>
		<span class="page-eyebrow">OVERVIEW / CONTROL CENTER</span>
		<h1>Admin Dashboard</h1>
		<p class="page-subtitle">Tổng quan hệ thống quản lý của bạn.</p>
	</div>
</div>
<div class="row g-3 g-xl-4">
	<div class="col-md-6">
		<div class="card stat-card"><div class="card-body">
			<div class="stat-icon"><i class="bi bi-collection" aria-hidden="true"></i></div>
			<div class="stat-label">Tổng danh mục</div>
			<div class="stat-value"><c:out value="${categoryCount}"/></div>
			<a class="dashboard-shortcut" href="${pageContext.request.contextPath}/admin/categories">Quản lý danh mục <i class="bi bi-arrow-up-right" aria-hidden="true"></i></a>
		</div></div>
	</div>
	<div class="col-md-6">
		<div class="card stat-card"><div class="card-body">
			<div class="stat-icon teal"><i class="bi bi-people" aria-hidden="true"></i></div>
			<div class="stat-label">Tổng người dùng</div>
			<div class="stat-value"><c:out value="${userCount}"/></div>
			<a class="dashboard-shortcut" href="${pageContext.request.contextPath}/admin/users">Quản lý người dùng <i class="bi bi-arrow-up-right" aria-hidden="true"></i></a>
		</div></div>
	</div>
</div>
<%@ include file="../layout/footer.jspf" %>
