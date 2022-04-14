package com.myspring.pro30.member.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myspring.pro30.member.service.MemberService;
import com.myspring.pro30.member.vo.MemberVO;



@Controller("memberController")
@EnableAspectJAutoProxy
public class MemberControllerImpl implements MemberController {
	
	// 컨테이너안의 빈을 자동으로 주입(빈을 자동으로 매핑해주는 개념 즉 의존성 주입)
	@Autowired
	private MemberService memberService;
	
	@Autowired
	MemberVO memberVO ;
	
	@RequestMapping(value={"/","/main.do"}, method=RequestMethod.GET)
	private ModelAndView main(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = (String)request.getAttribute("viewName");
		ModelAndView mav = new ModelAndView();
		mav.setViewName(viewName);
		return mav;
	}
	
	@Override
	@RequestMapping(value="/member/listMembers.do" ,method = RequestMethod.GET)
	public ModelAndView listMembers(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewName = getViewName(request);
		List membersList = memberService.listMembers();
		ModelAndView mav = new ModelAndView(viewName);
		mav.addObject("membersList", membersList);
		return mav;
	}

	@Override
	@RequestMapping(value="/member/addMember.do" ,method = RequestMethod.POST)
	public ModelAndView addMember(@ModelAttribute("member") MemberVO member,
			                  HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("utf-8");
		int result = 0;
		result = memberService.addMember(member);
		ModelAndView mav = new ModelAndView("redirect:/member/listMembers.do");
		return mav;
	}
	
	@Override
	@RequestMapping(value="/member/removeMember.do" ,method = RequestMethod.GET)
	public ModelAndView removeMember(@RequestParam("id") String id, 
			           HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("utf-8");
		memberService.removeMember(id);
		ModelAndView mav = new ModelAndView("redirect:/member/listMembers.do");
		return mav;
	}
	
	@Override
	@RequestMapping(value = "/member/login.do", method = RequestMethod.POST)
	public ModelAndView login(@ModelAttribute("member") MemberVO member,
				              RedirectAttributes rAttr,
		                       HttpServletRequest request, HttpServletResponse response) throws Exception {
	ModelAndView mav = new ModelAndView();
	memberVO = memberService.login(member);
	
	// 로그인이 성공시(null값이 아닐때)
	if(memberVO != null) {
			// 서버에 생성된 세션이 있다면 세션을 반환, 없다면 새 세션을 생성하여 반환
		    HttpSession session = request.getSession();
		    // 세션에 member의 이름으로 memberVO(로그인값)을 바인딩
		    session.setAttribute("member", memberVO);
		    // 세션에 isLogOn 이름으로 true 값을 바인딩
		    session.setAttribute("isLogOn", true);
		    // 세션에 저장된 action 값을 가져온다.
		    String action = (String)session.getAttribute("action");
		    // action값을 가져왔으니, 세션의 action값은 삭제 
		    session.removeAttribute("action");
		    
		    // 가져온 action 값이 null이 아닐때
		    if(action != null) {
		    	// action 값을 뷰이름으로 지정
		    	mav.setViewName("redirect:"+action);
		    } else {
		    	// action 값이 null 일때 뷰이름으로 지정
		    	mav.setViewName("redirect:/member/listMembers.do");
		    }
	}else {
		    rAttr.addAttribute("result","loginFailed");
		    // 로그인 실패시 뷰이름으로 지정
		    mav.setViewName("redirect:/member/loginForm.do");
	}
	// 조건에 맞는 뷰이름으로 페이지 이동
	return mav;
	}

	@Override
	@RequestMapping(value = "/member/logout.do", method =  RequestMethod.GET)
	public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpSession session = request.getSession();
		// 세션에 저장된 member값 제거
		session.removeAttribute("member");
		// 세션에 저장된 isLogON값 제거(로그인시 true로 셋팅해두었음)
		session.removeAttribute("isLogOn");
		ModelAndView mav = new ModelAndView();
		// 뷰이름으로 지정
		mav.setViewName("redirect:/member/listMembers.do");
		// 뷰이름으로 페이지 이동
		return mav;
	}	

	@RequestMapping(value = "/member/*Form.do", method =  RequestMethod.GET)
							//RequestParam 은 컨트롤러에서 파라메터 값을 넘겨받을때 사용
							//required=false 이므로 필수파라메터가 아니다. 즉 존재하지않으면 null
	public ModelAndView form(@RequestParam(value= "result", required=false) String result,
			  				@RequestParam(value= "action", required=false) String action,
			  				// 요청받을때 전달받은 정보를 HttpServletRequest객체를 생성하여 저장
			  				HttpServletRequest request, 
			  				// 웹브라우저에게 응답을 돌려줄 HttpServletResponse 객체를 생성(빈 객체)
			  				HttpServletResponse response) throws Exception {
		String viewName = (String) request.getAttribute("viewName");
		HttpSession session = request.getSession();
		// 세션에 action의 이름으로 RequestParam으로 받아온 action값을 바인딩
		session.setAttribute("action", action);
		ModelAndView mav = new ModelAndView();
		// result이름으로 RequestParam으로 받아온 result 값을 바인딩 (데이터 값 저장)
		mav.addObject("result", result);
		// 인터셉터에서 받아온 viewName을 뷰이름으로 지정 (뷰 이름 지정)
		mav.setViewName(viewName);
		// 뷰이름과 데이터값을 return
		return mav;
	}
	

	private String getViewName(HttpServletRequest request) throws Exception {
		String contextPath = request.getContextPath();
		String uri = (String) request.getAttribute("javax.servlet.include.request_uri");
		if (uri == null || uri.trim().equals("")) {
			uri = request.getRequestURI();
		}

		int begin = 0;
		if (!((contextPath == null) || ("".equals(contextPath)))) {
			begin = contextPath.length();
		}

		int end;
		if (uri.indexOf(";") != -1) {
			end = uri.indexOf(";");
		} else if (uri.indexOf("?") != -1) {
			end = uri.indexOf("?");
		} else {
			end = uri.length();
		}

		String viewName = uri.substring(begin, end);
		if (viewName.indexOf(".") != -1) {
			viewName = viewName.substring(0, viewName.lastIndexOf("."));
		}
		if (viewName.lastIndexOf("/") != -1) {
			viewName = viewName.substring(viewName.lastIndexOf("/", 1), viewName.length());
		}
		return viewName;
	}

}
