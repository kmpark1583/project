package com.myspring.pro30.board.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.myspring.pro30.board.service.BoardService;
import com.myspring.pro30.board.vo.ArticleVO;
import com.myspring.pro30.member.vo.MemberVO;


@Controller("boardController")
public class BoardControllerImpl  implements BoardController{
	
	// 네임스페이스 ARTICLE_IMAGE_REPO를 지정
	private static final String ARTICLE_IMAGE_REPO = "C:\\board\\article_image";
	
	@Autowired
	private BoardService boardService;
	@Autowired
	private ArticleVO articleVO;
	
	// 전체 글목록 조회하기.
	@Override
	@RequestMapping(value= "/board/listArticles.do", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView listArticles(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		// 인터셉터에서 받아온 viewName을 가져온다.
		String viewName = (String)request.getAttribute("viewName");
		
		// boardService 의 listArticles()를 호출
		// DAO를 호출해서 지정한 SQL문을 DB에서 실행
		// 글목록은 하나가 아닌 여러개라서 형태를 List로 지정해주었음.
		List articlesList = boardService.listArticles();
		
		// ModelAndView를 mav의 변수로 지정하고 활용
		ModelAndView mav = new ModelAndView(viewName);
		
		// articlesList의 이름으로 articlesList의 값을 저장(바인딩)
		mav.addObject("articlesList", articlesList);
		
		// jsp로 전달
		return mav;
	}
	
	//한 개 이미지 글쓰기
	@Override
	@RequestMapping(value="/board/addNewArticle.do" ,method = RequestMethod.POST)
	// 자바객체를 HTTP의 body로 전송함. (자바객체를 HTTP의 body내용으로 매핑)
	@ResponseBody
	public ResponseEntity addNewArticle(MultipartHttpServletRequest multipartRequest, 
	HttpServletResponse response) throws Exception {
		// 넘어온 multipartRequest 의 문자셋을 utf-8로 변환
		multipartRequest.setCharacterEncoding("utf-8");
		// 글 정보를 저장하기 위한 articleMap을 생성
		Map<String,Object> articleMap = new HashMap<String, Object>();
		// Enumeration 은 객체들을 모아놓은 집합에서 객체들을 하나씩 처리할수 있게 해줌
		// 객체들을 하나씩 while 반복문으로 돌릴수있다.
		Enumeration enu=multipartRequest.getParameterNames();
		// enu의 객체중 읽어올 요소가 남아있을때까지 반복문 실행
		// hasMoreElements() = 읽어올요소가 남아있으면 true, 없으면 false를 반환
		while(enu.hasMoreElements()){
			// nextElement() = 다음요소로 이동시키고, 가리키고 있는 요소객체를 꺼내 반환한다.
			// 즉 hasMoreElements는 0 부터 시작되고 하나라도 들어있다면 true를 리턴하니까
			// 첫번째엔 0 바로앞인 제일 첫번째 객체가 선택되고 리턴되게된다.
			// 반복될때마다 그 다음 객체가 선택된다.
			String name=(String)enu.nextElement();
			// value 는 multipartRequest의 name 파라메터를 받아온다.
			String value=multipartRequest.getParameter(name);
			// articleMap에 name과 value를 키/값 형태로 지정
			articleMap.put(name,value);
		}
		
		// 업로드한 이미지파일이름을 가져온다.
		String imageFileName= upload(multipartRequest);
		HttpSession session = multipartRequest.getSession();
		MemberVO memberVO = (MemberVO) session.getAttribute("member");
		String id = memberVO.getId();
		// articleMap에 parentNO 이름으로 0을 바인딩
		articleMap.put("parentNO", 0);
		// articleMap에 id 이름으로 memberVO에서 받은 id값을 바인딩
		articleMap.put("id", id);
		// articleMap에 imageFileName 이름으로 imageFileName값을 바인딩
		articleMap.put("imageFileName", imageFileName);
		
		// 초기화 작업
		String message;
		ResponseEntity resEnt=null;
		
		// HttpHeaders = HTTP 요청 또는 응답 헤더를 나타내는 데이터 구조
		// HttpHeaders 클래스 는 Header를 만들어준다.
		HttpHeaders responseHeaders = new HttpHeaders();
		// add로 지정된 컬렉션을 헤더에 추가
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		// try하고, 예외발생시 catch구문 실행.
		try {
			// articleMap을 boardService의 addNewArticle()메서드로 전달.
			int articleNO = boardService.addNewArticle(articleMap);
			// imageFileName이 null이 아니거나, 길이가 0이 아닐때
			if(imageFileName!=null && imageFileName.length()!=0) {
				// srcFile 은  네임스페이스값\\temp\\imageFileName 의 경로인 폴더를 지정
				File srcFile = new 
				File(ARTICLE_IMAGE_REPO+ "\\" + "temp"+ "\\" + imageFileName);
				// destDir 은 네임스페이스값\\articleNO 의 경로인 폴더를 지정
				File destDir = new File(ARTICLE_IMAGE_REPO+"\\"+articleNO);
				// 글정보 추가 후 업로드한 이미지파일을 글번호로 만든 폴더로 이동합니다.
				// 존재하지않는 폴더는 생성 (true이기때문)
				FileUtils.moveFileToDirectory(srcFile, destDir,true);
			}
			
			message = "<script>";
			message += " alert('새글을 추가했습니다.');";
			message += " location.href='"+multipartRequest.getContextPath()+"/board/listArticles.do'; ";
			message +=" </script>";
			// 상태,결과 값을 전달해줌
			// HttpStatus의 CREATED 일때 상태코드는 201
		    resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		    // 예외 발생시
		}catch(Exception e) {
			// srcFile 은 네임스페이스값\\temp\\imageFileName 의 경로인 폴더를 지정
			File srcFile = new File(ARTICLE_IMAGE_REPO+"\\"+"temp"+"\\"+imageFileName);
			// srcFile값 삭제
			srcFile.delete();
			
			message = " <script>";
			message +=" alert('오류가 발생했습니다. 다시 시도해 주세요');');";
			message +=" location.href='"+multipartRequest.getContextPath()+"/board/articleForm.do'; ";
			message +=" </script>";
			
			resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
			// 에러발생시 단계별로 에러메세지 출력
			e.printStackTrace();
		}
		return resEnt;
	}
	
	  @Override
	  @RequestMapping(value="/board/removeArticle.do" ,method = RequestMethod.POST)
	  @ResponseBody
	  public ResponseEntity  removeArticle(@RequestParam("articleNO") int articleNO,
	                              HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("text/html; charset=UTF-8");
		String message;
		ResponseEntity resEnt=null;
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/html; charset=utf-8");
		try {
			boardService.removeArticle(articleNO);
			File destDir = new File(ARTICLE_IMAGE_REPO+"\\"+articleNO);
			FileUtils.deleteDirectory(destDir);
			
			message = "<script>";
			message += " alert('글을 삭제했습니다.');";
			message += " location.href='"+request.getContextPath()+"/board/listArticles.do';";
			message +=" </script>";
		    resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		       
		}catch(Exception e) {
			message = "<script>";
			message += " alert('작업중 오류가 발생했습니다.다시 시도해 주세요.');";
			message += " location.href='"+request.getContextPath()+"/board/listArticles.do';";
			message +=" </script>";
		    resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
		    e.printStackTrace();
		}
		return resEnt;
	  }  
	
	//한개의 이미지 보여주기
	@RequestMapping(value="/board/viewArticle.do" ,method = RequestMethod.GET)
	public ModelAndView viewArticle(@RequestParam("articleNO") int articleNO,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception{
		String viewName = (String)request.getAttribute("viewName");
		articleVO=boardService.viewArticle(articleNO);
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		mav.addObject("article", articleVO);
		return mav;
	}
	
	
  //한 개 이미지 수정 기능
  @RequestMapping(value="/board/modArticle.do" ,method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity modArticle(MultipartHttpServletRequest multipartRequest,  
    HttpServletResponse response) throws Exception{
    multipartRequest.setCharacterEncoding("utf-8");
	Map<String,Object> articleMap = new HashMap<String, Object>();
	Enumeration enu=multipartRequest.getParameterNames();
	while(enu.hasMoreElements()){
		String name=(String)enu.nextElement();
		String value=multipartRequest.getParameter(name);
		articleMap.put(name,value);
	}
	
	String imageFileName= upload(multipartRequest);
	articleMap.put("imageFileName", imageFileName);
	
	String articleNO=(String)articleMap.get("articleNO");
	String message;
	ResponseEntity resEnt=null;
	HttpHeaders responseHeaders = new HttpHeaders();
	responseHeaders.add("Content-Type", "text/html; charset=utf-8");
    try {
       boardService.modArticle(articleMap);
       if(imageFileName!=null && imageFileName.length()!=0) {
         File srcFile = new File(ARTICLE_IMAGE_REPO+"\\"+"temp"+"\\"+imageFileName);
         File destDir = new File(ARTICLE_IMAGE_REPO+"\\"+articleNO);
         FileUtils.moveFileToDirectory(srcFile, destDir, true);
         
         String originalFileName = (String)articleMap.get("originalFileName");
         File oldFile = new File(ARTICLE_IMAGE_REPO+"\\"+articleNO+"\\"+originalFileName);
         oldFile.delete();
       }	
       message = "<script>";
	   message += " alert('글을 수정했습니다.');";
	   message += " location.href='"+multipartRequest.getContextPath()+"/board/viewArticle.do?articleNO="+articleNO+"';";
	   message +=" </script>";
       resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
    }catch(Exception e) {
      File srcFile = new File(ARTICLE_IMAGE_REPO+"\\"+"temp"+"\\"+imageFileName);
      srcFile.delete();
      message = "<script>";
	  message += " alert('오류가 발생했습니다.다시 수정해주세요');";
	  message += " location.href='"+multipartRequest.getContextPath()+"/board/viewArticle.do?articleNO="+articleNO+"';";
	  message +=" </script>";
      resEnt = new ResponseEntity(message, responseHeaders, HttpStatus.CREATED);
    }
    return resEnt;
  }
	
	@RequestMapping(value = "/board/*Form.do", method =  RequestMethod.GET)
	private ModelAndView form(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = (String)request.getAttribute("viewName");
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		return mav;
	}

	//한개 이미지 업로드하기
	private String upload(MultipartHttpServletRequest multipartRequest) throws Exception{
		String imageFileName= null;
		// Iterator = Enumeration 와 비슷한개념
		// multipartRequest의 파일이름을 받아온다.
		Iterator<String> fileNames = multipartRequest.getFileNames();
		
		// hasNext = hasMoreElements() 와 비슷한개념.
		// 다음에 가져올 값이있으면 true이니 가져올값이 있을때까지 반복
		while(fileNames.hasNext()){
			// hasNext는 true와 false로 반환이 되지만
			// next는 그 다음 값을 직접 가져온다.
			String fileName = fileNames.next();
			// 업로드된 파일에서 fileName객체를 반환
			MultipartFile mFile = multipartRequest.getFile(fileName);
			// 업로드한 파일의 이름을 구한다.
			// 파일의 이름은 단순히 이름이 아닌 전체경로.
			imageFileName=mFile.getOriginalFilename();
			// file 은 네임스페이스값\\temp\\fileName 의 경로를 지정
			File file = new File(ARTICLE_IMAGE_REPO +"\\"+"temp"+"\\" + fileName);
			
			// 업로드된파일에서 fileName객체를 반환했던 mFile의 크기가 0이 아닐경우 
			if(mFile.getSize()!=0){
				// 경로상에 file이 존재하지 않을 경우
				if(!file.exists()){
					// 경로에 해당하는 디렉토리들을 생성
					file.getParentFile().mkdirs();
					// mFile을 네임스페이스값\\temp\\imageFileName의 위치인 폴더에 저장
					mFile.transferTo(new File(ARTICLE_IMAGE_REPO +"\\"+"temp"+ "\\"+imageFileName)); //임시로 저장된 multipartFile을 실제 파일로 전송
				}
			}
		}
		
		// 업로드한 파일이름을 반환.
		return imageFileName;
	}
}