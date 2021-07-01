package springboard.controller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import model.JdbcTemplateConst;
import springboard.command.DeleteActionCommand;
import springboard.command.EditActionCommand;
import springboard.command.EditCommand;
import springboard.command.ReplyActionCommand;
import springboard.command.ReplyCommand;
import springboard.command.ViewCommand;
import springboard.command.WriteActionCommand;
import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

/*
기본패키지로 설정한 곳에 컨트롤러를 선언하면 요청이
들어왔을 때 Auto Scan 된다.
 */
@Controller
public class BbsController {
	/*
	Autowired
		: 스프링 설정파일에서 이미 생성된 빈을 자동으로 주입받고
		싶을 때사용한다. 타입을 기반으로 자동 주입되며, 만약 해당 타입의
		빈이 존재하지 않으면 에러가 발생되어 서버를 시작할 우 벗다.
		- 생성자, 멤버변수, 메소드(setter)에 적용가능함
		- 해당 어노테이션은 멤버변수에만 적용할 수 있다. 함수 내이 지역변수세어슨 사용할 수 없다.
		- 타입을 통해 자동으로 설정되므로 같은 타입이 2개 이상 존재하면
		예외가 발생됨.
	 */
	BbsCommandImpl command = null;
	private JdbcTemplate template;
	
	@Autowired
	public void setTemplate(JdbcTemplate template) {
		this.template = template;
		System.out.println("@Autowired -> JDBCTemplate 연결성공");
		//JdbcTemplate을 해당 프로그램 전체에서 사용하기 이한 설정 (static 타입)
		JdbcTemplateConst.template = this.template;
	}
	
	/*
	멤버변수로 선언하여 클래스에서 전역적으로 사용할 수 있다.
	해당 클래스의 모든 command 객체는 해당 인터페이스를 구현하여 정의한다.
	 */
	
	//게시판 리스트
	@RequestMapping("/board/list.do")
	public String list(Model model, HttpServletRequest req) {
		/*
		사용자로부터 받은 모든 요청은 request 객체에 저장되고,
		이를 listCommand 객체로 전달하기 위해 model에 저장한 후 
		매개변수로 전달한다.
		 */
		model.addAttribute("req",req);
		command = new ListCommand(); 
		command.execute(model);
		
		return "07Board/list";
	}
	
	//글쓰기 - 폼에 진입시에는 get방식으로 처리
	@RequestMapping("/board/write.do")
	public String write(Model model) {
		
		return "07Board/write";
	}
	//글쓰기 처리 - post 방식으로 전송되므로 value, method를 둘 다 명시.
	@RequestMapping(value="/board/writeAction.do",
			method=RequestMethod.POST)
	public String writeAction(Model model,
			HttpServletRequest req, SpringBbsDTO springBbsDTO) {
		/*
		글쓰기 페이지에서 전송된 모든 폼값을 SpringBbsDTO 객체가 한번에
		받는다. Spring에서는 커맨드 객체를 통해 이와 같이 할 수 있다.
		 */
		
		//뷰에서 전송된 폼 값을 저장한 커맥드 객체를 model에 저장한다.
		model.addAttribute("req",req);
		model.addAttribute("springBbsDTO", springBbsDTO);
		command = new WriteActionCommand();
		command.execute(model);
		
		/*
		redirect:이동할페이지경로(요청명)와 같이 하면 뷰를 호출하지 않고
		페이지 이동이 된다.
		 */
		return "redirect:list.do?nowPage=1";
	}
	
	@RequestMapping("/board/view.do")
	public String view(Model model, HttpServletRequest req) {
		model.addAttribute("req",req);
		command = new ViewCommand();
		command.execute(model);
		
		return "07Board/view";
	}
	
	//패스워드 검증 폼
	@RequestMapping("/board/password.do")
	public String password(Model model, HttpServletRequest req) {
		model.addAttribute("idx", req.getParameter("idx"));
		return "07Board/password";
	}
	
	//패스워드 검증 처리
	@RequestMapping("/board/passwordAction.do")
	public String passwordAction(Model model, HttpServletRequest req) {
		String modePage = null;
		
		String mode = req.getParameter("mode");
		String idx = req.getParameter("idx");
		String noewPage = req.getParameter("nowPage");
		String pass = req.getParameter("pass");
		
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		
		int rowExist = dao.password(idx, pass);
				
		if(rowExist<=0) {
			/*
			게시물의 일련번호가 0 이하의 값을 가질 수 없으므로
			검증 실패로 판단할 수 있다.
			 */
			//3개의 파라미터 중 일련번호를 받아서 model에 저장
			model.addAttribute("isCorrMsg", "패스워드가 일치하지 않습니다.");
			model.addAttribute("idx", idx);
			
			modePage = "07Board/password";
		}
		else {
			System.out.println("검증완료");
		}
		
		if(mode.equals("edit")) {
			//mode가 edit인 경우 수정페이지로 진입
			model.addAttribute("req", req);
			command = new EditCommand();
			command.execute(model);
			
			modePage = "07Board/edit";
		}
		else if(mode.equals("delete")) {
			//mode가 delete인 경우 즉시 삭제 처리
			model.addAttribute("req",req);
			command = new DeleteActionCommand();
			command.execute(model);
			
			model.addAttribute("nowPage", req.getParameter("nowPage"));
			modePage = "redirect:list.do";
		}
		
		return modePage;
	}
	
	@RequestMapping("/board/editAction.do")
	public String editAction(HttpServletRequest req,
			Model model, SpringBbsDTO springBbsDTO) {
		
		model.addAttribute("req", req);
		model.addAttribute("springBbsDTO", springBbsDTO);
		command = new EditActionCommand();
		command.execute(model);
		
		/*
		뷰로 이동시 redirect를 이용하면 해당 페이지로 리다이렉트 된다.
		이 때 파라미터가 필요하면 model에 저장하기만 하면 자동으로
		쿼리스트링이 추가된다.
		 */
		model.addAttribute("idx", req.getParameter("idx"));
		model.addAttribute("nowPage", req.getParameter("nowPage"));
		
		return "redirect:view.do";
	}
	
	@RequestMapping("/board/reply.do")
	public String reply(HttpServletRequest req, Model model) {
		
		System.out.println("reply() 메소드호출");
		
		model.addAttribute("req", req);
		command = new ReplyCommand();
		
		command.execute(model);
		
		model.addAttribute("idx", req.getParameter("idx"));
		return "07Board/reply";
	}
	
	//답변글 쓰기 처리
	@RequestMapping("/board/replyAction.do")
	public String replyAction(HttpServletRequest req, Model model,
			SpringBbsDTO springBbsDTO) {
		
		//작성폼에서 전송한 내용은 커맨드 객체로 한번에 저장
		model.addAttribute("springBbsDTO",springBbsDTO);
		model.addAttribute("req",req);
		command = new ReplyActionCommand();
		command.execute(model);
		
		model.addAttribute("nowPage", req.getParameter("nowPage"));
		return "redirect:list.do";
				
	}

}
