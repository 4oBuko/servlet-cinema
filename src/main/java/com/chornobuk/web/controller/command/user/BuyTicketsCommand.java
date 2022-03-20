package com.chornobuk.web.controller.command.user;

import com.chornobuk.web.controller.Path;
import com.chornobuk.web.controller.command.ICommand;
import com.chornobuk.web.controller.command.common.LogOutCommand;
import com.chornobuk.web.model.dao.OrderDao;
import com.chornobuk.web.model.dao.TicketDao;
import com.chornobuk.web.model.entity.MovieSession;
import com.chornobuk.web.model.entity.Order;
import com.chornobuk.web.model.entity.Ticket;
import com.chornobuk.web.model.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class BuyTicketsCommand implements ICommand {
	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp) {
		Logger log = LogManager.getLogger(BuyTicketsCommand.class);
		String errorTag = "is-invalid";
		String forward = Path.BUY_TICKETS_PAGE;
		String ownerNameRegex = "[\\p{Upper}]+ [\\p{Upper}]+";
		String cardNumberRegex = "[\\d]{16}";
		DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MM/yy");
		String cvvCodeRegex = "[\\d]{3}";
//		check
		String cardOwnerName = req.getParameter("cardOwner");
		String cardNumber = req.getParameter("cardNumber");
		String cardExpirationDate = req.getParameter("cardExpirationDate");
		YearMonth cardDate;
		try {
			cardDate = YearMonth.parse(cardExpirationDate, monthYearFormatter);
		} catch (DateTimeParseException e) {
			req.setAttribute("cardExpirationDateError", errorTag);
			return forward;
		}
		String cvvCode = req.getParameter("cvvCode");

		if (cardOwnerName == null || cardOwnerName.isEmpty() || !Pattern.matches(ownerNameRegex, cardOwnerName)) {
			req.setAttribute("ownerNameError", errorTag);
		} else if (cardNumber == null || cardNumber.isEmpty() || !Pattern.matches(cardNumberRegex, cardNumber)) {
			req.setAttribute("cardNumberError", errorTag);
		} else if (cardDate.isBefore(YearMonth.now())) {
			req.setAttribute("cardExpirationDateError", errorTag);
		} else if (cvvCode == null || cvvCode.isEmpty() || !Pattern.matches(cvvCodeRegex, cvvCode)) {
			req.setAttribute("cvvError", errorTag);
		} else {
//			take money from card
			int[] places = (int[]) req.getSession().getAttribute("orderPlaces");
			Ticket[] tickets = new Ticket[places.length];
			MovieSession session = (MovieSession) req.getSession().getAttribute("orderSession");
			TicketDao ticketDao = new TicketDao();
			User user = (User) req.getSession().getAttribute("user");
//			creating tickets
			for (int i = 0; i < tickets.length; i++) {
				tickets[i] = new Ticket();
				tickets[i].setPlaceNumber(places[i]);
				tickets[i].setSessionId(session.getId());
				if(ticketDao.getTicketBySession(tickets[i],session) != null) {
					req.setAttribute("ticketsAlreadyReservedError", errorTag);
					return forward;
				}
			}
			forward = Path.REDIRECT_COMMAND;
//			create order
			Order order = new Order();
			order.setCreationDate((LocalDateTime) req.getSession().getAttribute("orderCreatingTime"));
			order.setUserId(user.getId());
//			put them in database using transaction
			OrderDao orderDao = new OrderDao();

//			remove order attributes from session
			req.getSession().removeAttribute("orderCreatingTime");
			req.getSession().removeAttribute("orderSession");
			req.getSession().removeAttribute("orderPlaces");
			req.getSession().removeAttribute("totalPrice");

			orderDao.addOrderWithTickets(order, user, session, tickets);

			try {
				resp.sendRedirect(Path.INDEX_PAGE);
			} catch (IOException e) {
				log.error("redirect error", e);
				e.printStackTrace();
			}
		}
		return forward;
	}
}
