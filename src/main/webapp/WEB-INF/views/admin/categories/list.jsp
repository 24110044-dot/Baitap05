<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../../layout/header.jspf" %>
<div class="page-heading">
	<div>
		<span class="page-eyebrow">CATALOG / DATA MANAGEMENT</span>
		<h1>Danh mục</h1>
		<p class="page-subtitle">Quản lý danh mục và trạng thái hiển thị trong hệ thống.</p>
	</div>
	<a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/categories/create"><i class="bi bi-plus-lg me-2" aria-hidden="true"></i>Thêm danh mục</a>
</div>
<form class="search-form toolbar-panel panel" method="get" action="${pageContext.request.contextPath}/admin/categories">
	<div class="search-input-wrap">
		<i class="bi bi-search" aria-hidden="true"></i>
		<input class="form-control" name="keyword" value="${keyword}" placeholder="Tìm tên danh mục" aria-label="Tìm tên danh mục">
	</div>
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
		<thead><tr><th>ID</th><th>Tên</th><th>Ảnh</th><th>Trạng thái</th><th>Ngày tạo</th><th>Thao tác</th></tr></thead>
		<tbody>
		<c:forEach items="${categories.content}" var="category">
			<tr>
				<td><c:out value="${category.categoryId}"/></td>
				<td><c:out value="${category.categoryName}"/></td>
				<td class="category-image-cell">
					<c:choose>
						<c:when test="${not empty category.images}">
							<span class="category-icon">
								<c:choose>
									<c:when test="${fn:startsWith(category.images, '/uploads/categories/')}">
										<img class="category-icon-image" src="${pageContext.request.contextPath}${category.images}" alt="Ảnh danh mục" loading="lazy">
									</c:when>
									<c:otherwise>
										<img class="category-icon-image" src="<c:out value='${category.images}'/>" alt="Ảnh danh mục" loading="lazy" referrerpolicy="no-referrer">
									</c:otherwise>
								</c:choose>
							</span>
						</c:when>
						<c:otherwise><span class="category-icon category-icon-empty"><i class="bi bi-image" aria-hidden="true"></i></span></c:otherwise>
					</c:choose>
				</td>
				<td><c:choose><c:when test="${category.status}"><span class="status-pill status-active">ACTIVE</span></c:when><c:otherwise><span class="status-pill status-inactive">INACTIVE</span></c:otherwise></c:choose></td>
				<td><c:out value="${category.createdAt}"/></td>
				<td class="text-nowrap">
					<a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/categories/edit/${category.categoryId}"><i class="bi bi-pencil-square me-1" aria-hidden="true"></i>Sửa</a>
					<form class="d-inline" method="post" action="${pageContext.request.contextPath}/admin/categories/delete/${category.categoryId}"
						  onsubmit="return confirm('Bạn có chắc muốn xóa danh mục này?')">
						<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
						<button class="btn btn-sm btn-outline-danger" type="submit"><i class="bi bi-trash3 me-1" aria-hidden="true"></i>Xóa</button>
					</form>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${empty categories.content}"><tr><td colspan="6" class="text-center py-4">Không tìm thấy danh mục.</td></tr></c:if>
		</tbody>
	</table>
</div>
<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-3">
	<span class="table-meta">Tổng ${categories.totalElements} bản ghi · Trang ${categories.number + 1}/${categories.totalPages == 0 ? 1 : categories.totalPages}</span>
	<nav aria-label="Phân trang danh mục">
		<ul class="pagination mb-0">
			<c:url var="firstUrl" value="/admin/categories"><c:param name="keyword" value="${keyword}"/><c:param name="size" value="${size}"/><c:param name="page" value="0"/></c:url>
			<c:url var="prevUrl" value="/admin/categories"><c:param name="keyword" value="${keyword}"/><c:param name="size" value="${size}"/><c:param name="page" value="${categories.number - 1}"/></c:url>
			<c:url var="nextUrl" value="/admin/categories"><c:param name="keyword" value="${keyword}"/><c:param name="size" value="${size}"/><c:param name="page" value="${categories.number + 1}"/></c:url>
			<c:url var="lastUrl" value="/admin/categories"><c:param name="keyword" value="${keyword}"/><c:param name="size" value="${size}"/><c:param name="page" value="${categories.totalPages - 1}"/></c:url>
			<li class="page-item ${categories.first ? 'disabled' : ''}"><a class="page-link" href="${firstUrl}">Đầu</a></li>
			<li class="page-item ${categories.first ? 'disabled' : ''}"><a class="page-link" href="${prevUrl}">Trước</a></li>
			<li class="page-item ${categories.last || categories.totalPages == 0 ? 'disabled' : ''}"><a class="page-link" href="${nextUrl}">Sau</a></li>
			<li class="page-item ${categories.last || categories.totalPages == 0 ? 'disabled' : ''}"><a class="page-link" href="${lastUrl}">Cuối</a></li>
		</ul>
	</nav>
</div>
<%@ include file="../../layout/footer.jspf" %>
