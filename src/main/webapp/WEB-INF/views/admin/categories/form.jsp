<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../../layout/header.jspf" %>
<div class="row justify-content-center">
	<div class="col-12 col-lg-8">
		<div class="card form-card"><div class="card-body">
			<span class="page-eyebrow">CATALOG / CONFIGURATION</span>
			<h1 class="page-title mb-4"><c:out value="${pageTitle}"/></h1>
			<c:forEach items="${requestScope['org.springframework.validation.BindingResult.form'].allErrors}" var="validationError">
				<div class="alert alert-danger py-2"><c:out value="${validationError.defaultMessage}"/></div>
			</c:forEach>
			<form action="${pageContext.request.contextPath}${formAction}" method="post" enctype="multipart/form-data">
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
				<div class="mb-3">
					<label class="form-label" for="categoryName">Tên danh mục *</label>
					<input class="form-control" id="categoryName" name="categoryName" value="${form.categoryName}" placeholder="Nhập tên danh mục" required maxlength="50">
				</div>
				<div class="mb-3">
					<label class="form-label" for="images">Đường dẫn hình ảnh</label>
					<input class="form-control" id="images" name="images" type="text" inputmode="url" value="<c:out value='${form.images}'/>" placeholder="https://example.com/image.jpg" maxlength="500">
					<div class="form-text">Hoặc chọn ảnh PNG, JPEG, GIF (tối đa 5 MB).</div>
				</div>
				<div class="mb-4">
					<label class="form-label" for="imageFile">Chọn ảnh từ máy</label>
					<input class="form-control" id="imageFile" name="imageFile" type="file" accept="image/png,image/jpeg,image/gif">
					<div class="category-image-preview-wrap mt-3">
						<c:choose>
							<c:when test="${not empty form.images}">
								<c:choose>
									<c:when test="${fn:startsWith(form.images, '/uploads/categories/')}">
										<img class="category-image-preview" id="categoryImagePreview" src="${pageContext.request.contextPath}${form.images}" data-context-path="${pageContext.request.contextPath}" alt="Ảnh xem trước">
									</c:when>
									<c:otherwise>
										<img class="category-image-preview" id="categoryImagePreview" src="<c:out value='${form.images}'/>" data-context-path="${pageContext.request.contextPath}" alt="Ảnh xem trước">
									</c:otherwise>
								</c:choose>
								<span class="category-image-preview-empty d-none" id="categoryImagePreviewEmpty"><i class="bi bi-image" aria-hidden="true"></i> Chưa có ảnh</span>
							</c:when>
							<c:otherwise>
								<img class="category-image-preview d-none" id="categoryImagePreview" data-context-path="${pageContext.request.contextPath}" alt="Ảnh xem trước">
								<span class="category-image-preview-empty" id="categoryImagePreviewEmpty"><i class="bi bi-image" aria-hidden="true"></i> Chưa có ảnh</span>
							</c:otherwise>
						</c:choose>
						<span class="form-text">Ảnh sẽ được dùng làm biểu tượng danh mục.</span>
					</div>
				</div>
				<div class="form-check mb-3">
					<input class="form-check-input" id="status" name="status" type="checkbox" value="true" ${form.status ? 'checked' : ''}>
					<label class="form-check-label" for="status">Đang hoạt động</label>
				</div>
				<button class="btn btn-primary" type="submit"><i class="bi bi-check2-circle me-2" aria-hidden="true"></i>Lưu</button>
				<a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/categories">Hủy</a>
			</form>
		</div></div>
	</div>
</div>
<%@ include file="../../layout/footer.jspf" %>
