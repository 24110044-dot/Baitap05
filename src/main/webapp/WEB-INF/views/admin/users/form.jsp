<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../../layout/header.jspf" %>
<div class="row justify-content-center">
	<div class="col-12 col-xl-9">
		<div class="card form-card"><div class="card-body">
			<span class="page-eyebrow">ACCESS / ACCOUNT CONFIGURATION</span>
			<h1 class="page-title mb-4"><c:out value="${pageTitle}"/></h1>
			<c:forEach items="${requestScope['org.springframework.validation.BindingResult.form'].allErrors}" var="validationError">
				<div class="alert alert-danger py-2"><c:out value="${validationError.defaultMessage}"/></div>
			</c:forEach>
			<form action="${pageContext.request.contextPath}${formAction}" method="post">
				<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
				<div class="row">
					<div class="col-md-6 mb-3">
						<label class="form-label" for="username">Username *</label>
						<input class="form-control" id="username" name="username" value="${form.username}" placeholder="Username" required maxlength="50">
					</div>
					<div class="col-md-6 mb-3">
						<label class="form-label" for="email">Email *</label>
						<input class="form-control" id="email" name="email" type="email" value="${form.email}" placeholder="name@example.com" required maxlength="100">
					</div>
					<div class="col-md-6 mb-3">
						<label class="form-label" for="fullName">Họ tên</label>
						<input class="form-control" id="fullName" name="fullName" value="${form.fullName}" maxlength="100">
					</div>
					<div class="col-md-6 mb-3">
						<label class="form-label" for="phone">Điện thoại</label>
						<input class="form-control" id="phone" name="phone" value="${form.phone}" maxlength="15">
					</div>
					<div class="col-md-6 mb-3">
						<label class="form-label" for="password">Mật khẩu <c:if test="${creating}">*</c:if></label>
						<input class="form-control" id="password" name="password" type="password" placeholder="${creating ? 'Ít nhất 8 ký tự' : 'Nhập mật khẩu mới (không bắt buộc)'}" ${creating ? 'required' : ''}
							   minlength="8" maxlength="100" autocomplete="new-password">
						<c:if test="${not creating}"><div class="form-text">Để trống để giữ nguyên mật khẩu hiện tại.</div></c:if>
					</div>
					<div class="col-md-3 mb-3">
						<label class="form-label" for="role">Role</label>
						<select class="form-select" id="role" name="role">
							<option value="USER" ${form.role == 'USER' ? 'selected' : ''}>USER</option>
							<option value="ADMIN" ${form.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
						</select>
					</div>
					<div class="col-md-3 mb-3 d-flex align-items-end">
						<div class="form-check mb-2">
							<input class="form-check-input" id="status" name="status" type="checkbox" value="true" ${form.status ? 'checked' : ''}>
							<label class="form-check-label" for="status">Hoạt động</label>
						</div>
					</div>
				</div>
				<button class="btn btn-primary" type="submit"><i class="bi bi-check2-circle me-2" aria-hidden="true"></i>Lưu</button>
				<a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/users">Hủy</a>
			</form>
		</div></div>
	</div>
</div>
<%@ include file="../../layout/footer.jspf" %>
