package com.chornobuk.web.controller.command;

import com.chornobuk.web.model.dao.MovieDao;
import com.chornobuk.web.model.entity.Movie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;

public class NewSessionCommand implements ICommand {
	@Override
	public String execute(HttpServletRequest req, HttpServletResponse resp) {
		MovieDao movieDao = new MovieDao();
		LinkedList<Movie> movies = new LinkedList<>(movieDao.getAll());
//		todo get movies from database and set as an attribute
		req.getServletContext().setAttribute("movies", movies);
		return "WEB-INF/jsp/admin/newSession.jsp";
	}
}
