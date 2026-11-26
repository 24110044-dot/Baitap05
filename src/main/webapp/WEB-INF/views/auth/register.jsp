<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../layout/header.jspf" %>
<div class="row justify-content-center">
	<div class="col-12 col-xl-9">
		<div class="card auth-card">
			<div class="card-body">
				<div class="auth-kicker">IDENTITY / NEW USER</div>
				<h1 class="auth-title">Tạo tài khoản</h1>
				<p class="auth-description">Tạo tài khoản cá nhân để truy cập không gian người dùng.</p>
				<c:forEach items="${requestScope['org.springframework.validation.BindingResult.form'].allErrors}" var="validationError">
					<div class="alert alert-danger py-2"><c:out value="${validationError.defaultMessage}"/></div>
				</c:forEach>
				<form action="${pageContext.request.contextPath}/register" method="post">
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
					<div class="row">
						<div class="col-md-6 mb-3">
							<label class="form-label" for="username">Username *</label>
							<input class="form-control" id="username" name="username" value="${form.username}" placeholder="Ví dụ: neon_user" required maxlength="50" autocomplete="username">
						</div>
						<div class="col-md-6 mb-3">
							<label class="form-label" for="email">Email *</label>
							<input class="form-control" id="email" name="email" type="email" value="${form.email}" placeholder="name@example.com" required maxlength="100" autocomplete="email">
						</div>
						<div class="col-md-6 mb-3">
							<label class="form-label" for="password">Password (ít nhất 8 ký tự) *</label>
							<input class="form-control" id="password" name="password" type="password" placeholder="Ít nhất 8 ký tự" required minlength="8" maxlength="100" autocomplete="new-password">
						</div>
						<div class="col-md-6 mb-3">
							<label class="form-label" for="confirmPassword">Xác nhận password *</label>
							<input class="form-control" id="confirmPassword" name="confirmPassword" type="password" placeholder="Nhập lại mật khẩu" required autocomplete="new-password">
						</div>
						<div class="col-md-6 mb-3">
							<label class="form-label" for="fullName">Họ tên</label>
							<input class="form-control" id="fullName" name="fullName" value="${form.fullName}" maxlength="100">
						</div>
						<div class="col-md-6 mb-3">
							<label class="form-label" for="phone">Điện thoại</label>
							<input class="form-control" id="phone" name="phone" value="${form.phone}" maxlength="15" autocomplete="tel">
						</div>
					</div>
					<p class="text-secondary small"><i class="bi bi-shield-lock me-1" aria-hidden="true"></i>Tài khoản đăng ký mới được tạo với quyền USER.</p>
					<button class="btn btn-primary" type="submit"><i class="bi bi-person-check me-2" aria-hidden="true"></i>Đăng ký</button>
					<a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
				</form>
			</div>
		</div>
	</div>
</div>
<%@ include file="../layout/footer.jspf" %>
