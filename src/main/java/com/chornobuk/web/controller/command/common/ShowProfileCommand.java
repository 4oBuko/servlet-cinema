package com.chornobuk.web.controller.command.common;

import com.chornobuk.web.controller.Path;
import com.chornobuk.web.controller.command.ICommand;
import com.chornobuk.web.model.dao.OrderDao;
import com.chornobuk.web.model.entity.User;
import com.chornobuk.web.model.entity.UserRole;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class ShowProfileCommand implements ICommand {
	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp) {
		String forward = Path.PROFILE_PAGE;
		OrderDao orderDao = new OrderDao();
		UserRole role = (UserRole) req.getSession().getAttribute("role");

		if (role == UserRole.USER) {
			User user = (User) req.getSession().getAttribute("user");
			HashMap<Long, Integer> orderIdPriceMap = new HashMap<>(orderDao.getOrdersPricesByUser(user));
			req.setAttribute("ordersPrices", orderIdPriceMap);
		}
		return forward;
	}
}