package cz.muni.fi.web;

import cz.muni.fi.car.Car;
import cz.muni.fi.car.CarManager;
import cz.muni.fi.exceptions.ServiceFailureException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet for implementation of car.
 *
 * @author Robert Tamas
 */
@WebServlet(CarsServlet.URL_MAPPING + "/*")
public class CarsServlet extends HttpServlet{

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/cars";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        showCarsList(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");
        String action = request.getPathInfo();
        switch (action) {
            case "/add":
                String licensePlate = request.getParameter("licencePlate");
                String brand = request.getParameter("brand");
                String model = request.getParameter("model");
                String pricePerDayString = request.getParameter("pricePerDay");
                int pricePerDay;

                if (licensePlate == null || licensePlate.length() == 0 ||
                        brand == null || brand.length() == 0 ||
                        model == null || model.length() == 0 ||
                        pricePerDayString == null ||
                        pricePerDayString.length() == 0) {
                    request.setAttribute("error1",
                            "All fields are required!");
                    showCarsList(request, response);
                    return;
                }

                try{
                    pricePerDay = Integer.parseInt(pricePerDayString);
                    if (pricePerDay < 0) {
                        request.setAttribute("error1",
                                "Price per day can't be negative!");
                        showCarsList(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("error1",
                            "Price per day must be a number!");
                    showCarsList(request, response);
                    return;
                }

                try {
                    Car car = new Car(licensePlate, brand, model, pricePerDay);
                    getCarManager().createCar(car);
                    response.sendRedirect(request.getContextPath() +
                            URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getCarManager().deleteCar(getCarManager().getCarById(id));
                    response.sendRedirect(request.getContextPath() +
                            URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            e.getMessage());
                    return;
                }
            case "/update":
                Long id = Long.parseLong(request.getParameter("id"));
                try {
                    Car car = getCarManager().getCarById(id);
                    request.setAttribute("update", car);
                    showCarsList(request, response);
                } catch (ServiceFailureException e) {
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            e.getMessage());
                    return;
                }
            case "/change":
                id = Long.parseLong(request.getParameter("id"));
                licensePlate = request.getParameter("licencePlate");
                brand = request.getParameter("brand");
                model = request.getParameter("model");
                pricePerDayString = request.getParameter("pricePerDay");
                Car car = getCarManager().getCarById(id);
                if (licensePlate == null || licensePlate.length() == 0 ||
                        brand == null || brand.length() == 0 ||
                        model == null || model.length() == 0 ||
                        pricePerDayString == null ||
                        pricePerDayString.length() == 0) {
                    request.setAttribute("update", car);
                    request.setAttribute("error2", "All fields are required!");
                    showCarsList(request, response);
                    return;
                }

                try{
                    pricePerDay = Integer.parseInt(pricePerDayString);
                    if (pricePerDay < 0) {
                        request.setAttribute("update", car);
                        request.setAttribute("error2",
                                "Price per day can't be negative!");
                        showCarsList(request, response);
                        return;
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("update", car);
                    request.setAttribute("error2",
                            "Price per day must be a number!");
                    showCarsList(request, response);
                    return;
                }

                try {
                    car = getCarManager().getCarById(id);
                    car.setLicencePlate(licensePlate);
                    car.setBrand(brand);
                    car.setModel(model);
                    car.setPricePerDay(pricePerDay);
                    getCarManager().updateCar(car);
                    response.sendRedirect(request.getContextPath() +
                            URL_MAPPING);
                    return;
                } catch (ServiceFailureException e) {
                    response.sendError(
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            e.getMessage());
                    return;
                }


            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Unknown action " + action);
        }
    }

    /**
     * Gets CarManager from ServletContext, where it was
     * stored by {@link StartListener}.
     *
     * @return CarManager instance
     */
    private CarManager getCarManager() {
        return (CarManager) getServletContext().getAttribute("carManager");
    }

    /**
     * Stores the list of cars to request attribute "cars" and forwards
     * to the JSP to display it.
     */
    private void showCarsList(HttpServletRequest request,
                              HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("cars", getCarManager().findAllCars());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (ServiceFailureException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
    }
}
