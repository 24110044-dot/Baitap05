<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../../layout/header.jspf" %>
<div class="page-heading">
	<div>
		<span class="page-eyebrow">ACCESS / ACCOUNT MANAGEMENT</span>
		<h1>Người dùng</h1>
		<p class="page-subtitle">Tìm kiếm, phân quyền và quản lý tài khoản hệ thống.</p>
	</div>
	<a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/users/create"><i class="bi bi-person-plus me-2" aria-hidden="true"></i>Thêm người dùng</a>
</div>
<form class="search-form toolbar-panel panel" method="get" action="${pageContext.request.contextPath}/admin/users">
	<div class="search-input-wrap">
		<i class="bi bi-search" aria-hidden="true"></i>
		<input class="form-control" name="keyword" value="${keyword}" placeholder="Username, email hoặc họ tên" aria-label="Tìm người dùng">
	</div>
	<select class="form-select" name="role" aria-label="Lọc theo role">
		<option value="">Mọi role</option>
		<option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
		<option value="USER" ${role == 'USER' ? 'selected' : ''}>USER</option>
	</select>
	<select class="form-select" name="status" aria-label="Lọc theo trạng thái">
		<option value="">Mọi trạng thái</option>
		<option value="true" ${status == true ? 'selected' : ''}>Hoạt động</option>
		<option value="false" ${status == false ? 'selected' : ''}>Đã khóa</option>
	</select>
	<select class="form-select" name="size" aria-label="Số dòng mỗi trang">
		<option value="5" ${size == 5 ? 'selected' : ''}>5 dòng</option>
		<option value="10" ${size == 10 ? 'selected' : ''}>10 dòng</option>
		<option value="20" ${size == 20 ? 'selected' : ''}>20 dòng</option>
		<option value="50" ${size == 50 ? 'selected' : ''}>50 dòng</option>
	</select>
	<button class="btn btn-outline-primary" type="submit"><i class="bi bi-search me-2" aria-hidden="true"></i>Tìm kiếm</button>
</form>
<div class="table-responsive table-panel">
	<table class="table table-striped table-hover align-middle">
		<thead><tr><th>ID</th><th>Username</th><th>Email</th><th>Họ tên</th><th>Điện thoại</th><th>Role</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
		<tbody>
		<c:forEach items="${users.content}" var="user">
			<tr>
				<td><c:out value="${user.userId}"/></td>
				<td><c:out value="${user.username}"/></td>
				<td><c:out value="${user.email}"/></td>
				<td><c:out value="${user.fullName}"/></td>
				<td><c:out value="${user.phone}"/></td>
				<td><c:choose><c:when test="${user.role == 'ADMIN'}"><span class="role-pill role-admin">ADMIN</span></c:when><c:otherwise><span class="role-pill role-user">USER</span></c:otherwise></c:choose></td>
				<td><c:choose><c:when test="${user.status}"><span class="status-pill status-active">ACTIVE</span></c:when><c:otherwise><span class="status-pill status-inactive">LOCKED</span></c:otherwise></c:choose></td>
				<td><c:out value="${user.createdAt}"/></td>
				<td class="text-nowrap">
					<a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/users/edit/${user.userId}"><i class="bi bi-pencil-square me-1" aria-hidden="true"></i>Sửa</a>
					<form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/users/delete/${user.userId}"
						  onsubmit="return confirm('Bạn có chắc muốn xóa người dùng này?')">
						<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
						<button class="btn btn-sm btn-outline-danger" type="submit"><i class="bi bi-trash3 me-1" aria-hidden="true"></i>Xóa</button>
					</form>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${empty users.content}"><tr><td colspan="9" class="text-center py-4">Không tìm thấy người dùng.</td></tr></c:if>
		</tbody>
	</table>
</div>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-3">
	<span class="table-meta">Tổng ${users.totalElements} bản ghi · Trang ${users.number + 1}/${users.totalPages == 0 ? 1 : users.totalPages}</span>
	<nav aria-label="Phân trang người dùng">
		<ul class="pagination mb-0">
			<c:url var="firstUrl" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="size" value="${size}"/><c:param name="page" value="0"/></c:url>
			<c:url var="prevUrl" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="size" value="${size}"/><c:param name="page" value="${users.number - 1}"/></c:url>
			<c:url var="nextUrl" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="size" value="${size}"/><c:param name="page" value="${users.number + 1}"/></c:url>
			<c:url var="lastUrl" value="/admin/users"><c:param name="keyword" value="${keyword}"/><c:param name="role" value="${role}"/><c:param name="status" value="${status}"/><c:param name="size" value="${size}"/><c:param name="page" value="${users.totalPages - 1}"/></c:url>
			<li class="page-item ${users.first ? 'disabled' : ''}"><a class="page-link" href="${firstUrl}">Đầu</a></li>
			<li class="page-item ${users.first ? 'disabled' : ''}"><a class="page-link" href="${prevUrl}">Trước</a></li>
			<li class="page-item ${users.last || users.totalPages == 0 ? 'disabled' : ''}"><a class="page-link" href="${nextUrl}">Sau</a></li>
			<li class="page-item ${users.last || users.totalPages == 0 ? 'disabled' : ''}"><a class="page-link" href="${lastUrl}">Cuối</a></li>
		</ul>
	</nav>
</div>
<%@ include file="../../layout/footer.jspf" %>
