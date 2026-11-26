<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../layout/header.jspf" %>
<div class="page-heading">
	<div>
		<span class="page-eyebrow">CATEGORY / DISCOVER</span>
		<h1>Trang chủ</h1>
		<p class="page-subtitle">Khám phá các danh mục đang được hiển thị.</p>
	</div>
</div>

<c:choose>
	<c:when test="${not empty categories.content}">
		<div class="row g-4 category-card-grid">
			<c:forEach items="${categories.content}" var="category">
				<div class="col-12 col-sm-6 col-lg-4 col-xl-3">
					<article class="card category-home-card">
						<c:choose>
							<c:when test="${not empty category.images}">
								<c:choose>
									<c:when test="${fn:startsWith(category.images, '/uploads/categories/')}">
										<img class="category-home-image" src="${pageContext.request.contextPath}${category.images}" alt="Ảnh danh mục" loading="lazy">
									</c:when>
									<c:otherwise>
										<img class="category-home-image" src="<c:out value='${category.images}'/>" alt="Ảnh danh mục" loading="lazy" referrerpolicy="no-referrer">
									</c:otherwise>
								</c:choose>
							</c:when>
							<c:otherwise>
								<div class="category-home-image-placeholder"><i class="bi bi-grid-1x2" aria-hidden="true"></i></div>
							</c:otherwise>
						</c:choose>
						<div class="card-body">
							<span class="category-home-label">DANH MỤC</span>
							<h2 class="category-home-title"><c:out value="${category.categoryName}"/></h2>
						</div>
					</article>
				</div>
			</c:forEach>
		</div>

		<c:if test="${categories.totalPages > 1}">
			<div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-4">
				<span class="table-meta">Trang ${categories.number + 1}/${categories.totalPages}</span>
				<nav aria-label="Phân trang danh mục">
					<ul class="pagination mb-0">
						<c:url var="userPreviousUrl" value="/user/dashboard">
							<c:param name="page" value="${categories.number - 1}"/>
							<c:param name="size" value="${categories.size}"/>
						</c:url>
						<c:url var="userNextUrl" value="/user/dashboard">
							<c:param name="page" value="${categories.number + 1}"/>
							<c:param name="size" value="${categories.size}"/>
						</c:url>
						<li class="page-item ${categories.first ? 'disabled' : ''}">
							<a class="page-link" href="${userPreviousUrl}">Trước</a>
						</li>
						<li class="page-item ${categories.last ? 'disabled' : ''}">
							<a class="page-link" href="${userNextUrl}">Sau</a>
						</li>
					</ul>
				</nav>
			</div>
		</c:if>
	</c:when>
	<c:otherwise>
		<div class="card category-home-empty">
			<div class="card-body">
				<div class="stat-icon teal"><i class="bi bi-grid" aria-hidden="true"></i></div>
				<h2 class="h5 mt-3">Chưa có danh mục nào</h2>
				<p class="mb-0 text-secondary">Các danh mục sẽ xuất hiện tại đây khi được quản trị viên bật hiển thị.</p>
			</div>
		</div>
	</c:otherwise>
</c:choose>
<%@ include file="../layout/footer.jspf" %>
