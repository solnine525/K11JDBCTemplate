package springboard.command;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import springboard.controller.BbsCommandImpl;
import springboard.model.JDBCTemplateDAO;
import springboard.model.SpringBbsDTO;

public class ReplyActionCommand implements BbsCommandImpl{
	
	@Override
	public void execute(Model model) {
		
		Map<String, Object> paramMap = model.asMap();
		HttpServletRequest req = (HttpServletRequest)paramMap.get("req");
		
		SpringBbsDTO dto = (SpringBbsDTO)paramMap.get("springBbsDTO");
		
		JDBCTemplateDAO dao = new JDBCTemplateDAO();
		
		dao.reply(dto);
	}
}
