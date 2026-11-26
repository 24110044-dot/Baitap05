(() => {
	"use strict";

	const toggle = document.querySelector("[data-sidebar-toggle]");
	const closeTarget = document.querySelector("[data-sidebar-close]");
	if (toggle) {
		const closeSidebar = () => {
			document.body.classList.remove("sidebar-open");
			toggle.setAttribute("aria-expanded", "false");
		};

		toggle.addEventListener("click", () => {
			const isOpen = document.body.classList.toggle("sidebar-open");
			toggle.setAttribute("aria-expanded", String(isOpen));
		});

		if (closeTarget) {
			closeTarget.addEventListener("click", closeSidebar);
		}

		document.addEventListener("keydown", (event) => {
			if (event.key === "Escape") {
				closeSidebar();
			}
		});
	}

	const imageUrlInput = document.querySelector("#images");
	const imageFileInput = document.querySelector("#imageFile");
	const preview = document.querySelector("#categoryImagePreview");
	const emptyPreview = document.querySelector("#categoryImagePreviewEmpty");

	if (!imageUrlInput || !imageFileInput || !preview || !emptyPreview) {
		return;
	}

	let objectUrl;
	const clearObjectUrl = () => {
		if (objectUrl) {
			URL.revokeObjectURL(objectUrl);
			objectUrl = undefined;
		}
	};
	const showPreview = (source) => {
		if (!source) {
			preview.classList.add("d-none");
			emptyPreview.classList.remove("d-none");
			return;
		}
		const imageSource = source.startsWith("/uploads/categories/")
			? `${preview.dataset.contextPath || ""}${source}`
			: source;
		preview.src = imageSource;
		preview.classList.remove("d-none");
		emptyPreview.classList.add("d-none");
	};

	preview.addEventListener("load", () => {
		preview.classList.remove("d-none");
		emptyPreview.classList.add("d-none");
	});
	preview.addEventListener("error", () => {
		preview.classList.add("d-none");
		emptyPreview.classList.remove("d-none");
	});
	imageFileInput.addEventListener("change", () => {
		const file = imageFileInput.files && imageFileInput.files[0];
		if (!file) {
			return;
		}
		imageUrlInput.value = "";
		clearObjectUrl();
		objectUrl = URL.createObjectURL(file);
		showPreview(objectUrl);
	});
	imageUrlInput.addEventListener("input", () => {
		if (imageUrlInput.value.trim()) {
			imageFileInput.value = "";
			clearObjectUrl();
			showPreview(imageUrlInput.value.trim());
		} else if (!imageFileInput.files || imageFileInput.files.length === 0) {
			showPreview("");
		}
	});
})();
