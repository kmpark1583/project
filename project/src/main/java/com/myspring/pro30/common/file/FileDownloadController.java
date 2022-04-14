package com.myspring.pro30.common.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class FileDownloadController {
	private static final String ARTICLE_IMAGE_REPO = "C:\\board\\article_image";
	@RequestMapping("/download.do")
							//RequestParam은 jsp에서 보낸 request값을 받기위해 사용된다.
							// 만약 호출한 곳에서 imageFileName값이 있을경우 String imageFileName에 담기게 되고,
							// articleNO값이 있을경우 String articleNO 에 담기게 된다.
							// 값을 받을땐 ?imageFileName=xxx 이런식으로 jsp로부터 받게된다.
	protected void download(@RequestParam("imageFileName") String imageFileName,
							@RequestParam("articleNO") String articleNO,
			                 HttpServletResponse response)throws Exception {
		
		// OutputStream 은 데이터를 출력할때 사용되는 스트림
		// 스트림은 데이터의 입력과 출력을 하도록 이어주는 통로 개념
		// response에서 파일을 내보낼것들을 가지고온다.
		OutputStream out = response.getOutputStream();
		// downFile = 네임스페이스\\articleNO\\imageFileName
		String downFile = ARTICLE_IMAGE_REPO + "\\" +articleNO+"\\"+ imageFileName;
		// downFile의 경로의 파일을 선택
		File file = new File(downFile);

		// 응답할 response의 Cache-Control헤더의 값을 no-cache로 설정 
		response.setHeader("Cache-Control", "no-cache");
		// 응답할 response의 헤더부분에 fileName= jsp로부터 받은 imageFileName 값을 value로 추가함
		response.addHeader("Content-disposition", "attachment; fileName=" + imageFileName);
		
		// 선택된 file(downFile의 경로)에 값들을 input 할수있다.
		FileInputStream in = new FileInputStream(file);
		// inputstream인 in 에서 1024 * 8 만큼을 읽어 buffer 배열에 저장 
		byte[] buffer = new byte[1024 * 8];
		
		while (true) {
			// 배열에 저장된 buffer값들을 읽어서 count에 넣어준다.
			int count = in.read(buffer); 
			// count 가 모든값들을 읽었으면(모든내용을 읽어오면 -1을 return해줌)
			if (count == -1) 
				// 이 반복문을 그만한다.
				break;
			// out에 buffer크기만큼 쓰고, 시작위치(0) 부터 count 값까지만 쓴다.
			out.write(buffer, 0, count);
		}
		// 모든 값들을 읽고 사용했으면 반드시 close를 해주어야함.
		in.close();
		out.close();
	}

}
