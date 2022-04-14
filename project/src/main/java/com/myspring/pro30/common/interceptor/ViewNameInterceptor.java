package com.myspring.pro30.common.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ViewNameInterceptor extends HandlerInterceptorAdapter {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		try {
			// 요청받은 request를 
			// 아래 getViewName 함수의 인자값으로 넣어서 실행
			// 요청받은 request값에 대한 fileName을 리턴받아서 viewName에 바인딩.
			String viewName = getViewName(request);
			request.setAttribute("viewName", viewName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	private String getViewName(HttpServletRequest request) throws Exception {
		// request.getContextPath() 는
		// http://localhost:8090/project/~~ 의 프로젝트 path부분
		// 즉 /project만 가져온다.
		String contextPath = request.getContextPath();
		// request.getAttribute()로 HTTP에 설정된 파라미터값을 얻을수 있다.
		// include된 뷰에서도 원래 요청 URI 정보를 얻는다.
		String uri = (String) request.getAttribute("javax.servlet.include.request_uri");
		
		// 요청 URI정보를 받아온 값이 null값이거나, 문자열 양끝의 공백을 제거(trim)한 값이 "" 인경우
		if (uri == null || uri.trim().equals("")) {
			// getRequestURI() 는
			// 프로젝트 + 파일경로를 가져온다.
			// http://localhost:8090/project/~~.jsp 의 프로젝트와 파일경로부분
			// 즉 /project~~.jsp 를 가져온다.
			uri = request.getRequestURI();
		}

		int begin = 0;
		// (받아온 프로젝트 path부분이 null값이거나 공백과 받아온 path가 같은지 확인해서 true일경우)의 반대일때
		if (!((contextPath == null) || ("".equals(contextPath)))) {
			// 0으로 초기화했던 begin에 받아왔던 path의 길이를 넣어준다.
			begin = contextPath.length();
		}

		int end;
		// indexOf() 함수는 문자열에서 특정문자열을 찾고, 검색된 문자열이 첫번째로 나타나는 위치를 리턴한다.
		// 찾는 문자열이 없으면 -1을 리턴
		// 즉 uri에서 ;가 있다면
		if (uri.indexOf(";") != -1) {
			// uri의 ;가 있는 위치값(숫자)을 end에 바인딩해준다.
			end = uri.indexOf(";");
		} else if (uri.indexOf("?") != -1) {
			// uri에서 !는 없지만 ? 가 있다면 ?가 있는 위치값을 end에 바인딩
			end = uri.indexOf("?");
		} else {
			// ?와 !가 둘다 없어서 -1을 반환했다면
			// end 는 uri의 길이를 바인딩.
			end = uri.length();
		}

		// uri의 return된 begin과 end값 사이의 값까지의 위치값을 토대로
		// 그 위치값에 해당하는 문자열들만 잘라서 리턴해준다.
		String fileName = uri.substring(begin, end);
		// 받아온 문자열들중 .이 있다면
		if (fileName.indexOf(".") != -1) {
			// 0번째 위치부터 문자열들중 마지막 "." 이있는곳까지의 위치값만잘라내서
			// 그 위치값에 해당하는 문자열들만 추출
			fileName = fileName.substring(0, fileName.lastIndexOf("."));
		}
		// 받아온 문자열 중 끝에서부터 검색해서 / 가 있다면 
		if (fileName.lastIndexOf("/") != -1) {
			// 문자열의 끝부분에서 (위치 : 0,1,...) 1번째 이후의 / 위치값 부터 문자열의 전체길이의 위치값까지의
			// 문자열들을 추출
			fileName = fileName.substring(fileName.lastIndexOf("/", 1), fileName.length());
		}
		// 그렇게 잘라낸 문자열들만 return
		return fileName;
	}
}
