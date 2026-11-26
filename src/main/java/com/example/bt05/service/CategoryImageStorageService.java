package com.example.bt05.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CategoryImageStorageService {

	private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
	private static final long MAX_IMAGE_PIXELS = 12_000_000L;

	private final Path uploadDirectory;

	public CategoryImageStorageService(@Value("${app.uploads.directory:uploads}") String uploadDirectory) {
		this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve("categories");
	}

	public String store(MultipartFile image) throws IOException {
		if (image == null || image.isEmpty()) {
			throw new IllegalArgumentException("Chọn một tệp hình ảnh trước khi lưu.");
		}
		if (image.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("Kích thước hình ảnh không được vượt quá 5 MB.");
		}

		String extension = detectImageExtension(image);
		Files.createDirectories(uploadDirectory);
		String fileName = UUID.randomUUID() + "." + extension;
		Path destination = uploadDirectory.resolve(fileName);
		Path temporaryFile = Files.createTempFile(uploadDirectory, ".category-image-", ".tmp");

		try (InputStream input = image.getInputStream()) {
			Files.copy(input, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
			try {
				Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporaryFile, destination);
			}
		} catch (IOException exception) {
			try {
				Files.deleteIfExists(temporaryFile);
			} catch (IOException cleanupException) {
				exception.addSuppressed(cleanupException);
			}
			throw exception;
		}

		return "/uploads/categories/" + fileName;
	}

	public String resourceLocation() {
		String location = uploadDirectory.toUri().toString();
		return location.endsWith("/") ? location : location + "/";
	}

	private String detectImageExtension(MultipartFile image) {
		try (InputStream source = image.getInputStream();
				ImageInputStream input = ImageIO.createImageInputStream(source)) {
			if (input == null) {
				throw new IllegalArgumentException("Tệp đã chọn không phải hình ảnh hợp lệ.");
			}

			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext()) {
				throw new IllegalArgumentException("Chỉ hỗ trợ hình ảnh PNG, JPEG hoặc GIF.");
			}

			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (width <= 0 || height <= 0 || (long) width * height > MAX_IMAGE_PIXELS) {
					throw new IllegalArgumentException("Kích thước ảnh tối đa là 12 megapixel.");
				}
				if (reader.read(0) == null) {
					throw new IllegalArgumentException("Tệp đã chọn không phải hình ảnh hợp lệ.");
				}

				return switch (reader.getFormatName().toLowerCase(Locale.ROOT)) {
					case "png" -> "png";
					case "jpeg", "jpg" -> "jpg";
					case "gif" -> "gif";
					default -> throw new IllegalArgumentException("Chỉ hỗ trợ hình ảnh PNG, JPEG hoặc GIF.");
				};
			} finally {
				reader.dispose();
			}
		} catch (IOException exception) {
			throw new IllegalArgumentException("Không thể đọc hình ảnh đã chọn.", exception);
		}
	}
}
