package com.office.cook.test;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/cloudinary")
public class CloudinaryTestController {

	@Autowired
	private Cloudinary cloudinary;

	@GetMapping("/uploadForm")
	public String showUploadForm() {
		return "test/uploadForm"; // src/main/resources/templates/test/uploadForm.html
	}

	@PostMapping("/upload")
	public String uploadImage(@RequestParam("file") MultipartFile file, Model model) {
		try {
			Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
			model.addAttribute("imageUrl", uploadResult.get("secure_url"));
		} catch (IOException e) {
			model.addAttribute("error", "업로드 실패: " + e.getMessage());
		}
		return "test/uploadResult"; // 업로드 결과 보기
	}
}
