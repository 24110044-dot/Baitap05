<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="../layout/header.jspf" %>
<div class="row justify-content-center">
	<div class="col-12 col-sm-10 col-md-7 col-lg-5">
		<div class="card auth-card">
			<div class="card-body">
				<div class="auth-kicker">IDENTITY / ACCESS</div>
				<h1 class="auth-title">Đăng nhập</h1>
				<p class="auth-description">Đăng nhập để tiếp tục vào hệ thống.</p>
				<c:if test="${param.error ne null}">
					<div class="alert alert-danger">Thông tin đăng nhập không hợp lệ hoặc tài khoản đã bị khóa.</div>
				</c:if>
				<form action="${pageContext.request.contextPath}/login" method="post">
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
					<div class="mb-3">
						<label class="form-label" for="username">Username <span class="text-danger">*</span></label>
						<input class="form-control" id="username" name="username" placeholder="Nhập username" required maxlength="50" autocomplete="username">
					</div>
					<div class="mb-3">
						<label class="form-label" for="password">Mật khẩu <span class="text-danger">*</span></label>
						<input class="form-control" id="password" name="password" type="password" placeholder="Nhập mật khẩu" required autocomplete="current-password">
					</div>
					<button class="btn btn-primary w-100 mt-2" type="submit"><i class="bi bi-box-arrow-in-right me-2" aria-hidden="true"></i>Đăng nhập</button>
				</form>
				<div class="auth-rule">NEW TO NEONCORE</div>
				<p class="auth-link mb-0 text-center">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
			</div>
		</div>
	</div>
</div>
<%@ include file="../layout/footer.jspf" %>
