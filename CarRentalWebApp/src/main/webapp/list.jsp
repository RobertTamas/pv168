<%@page contentType="text/html;charset=utf-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>WebApp - Car manager</title>
    <meta name="author" content="Robert Tamas">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/fonts/font-awesome/css/font-awesome.min.css">
</head>
<body>
    <div class="page-content">
        <h1>Database of cars</h1>
        <div class="table">
            <table>
                <thead>
                <tr>
                    <th class="id">ID</th>
                    <th class="data">License plate</th>
                    <th class="data">Brand</th>
                    <th class="data">Model</th>
                    <th class="data">Price per day</th>
                    <th class="services">Edit</th>
                    <th class="services">Delete</th>
                </tr>
                </thead>
                <tbody>
                    <c:forEach items="${cars}" var="car">
                        <tr>
                            <td><c:out value="${car.id}"/></td>
                            <td><c:out value="${car.licencePlate}"/></td>
                            <td><c:out value="${car.brand}"/></td>
                            <td><c:out value="${car.model}"/></td>
                            <td><c:out value="${car.pricePerDay}"/></td>
                            <td><form method="post" action="${pageContext.request.contextPath}/cars/update?id=${car.id}"
                                      style="margin-bottom: 0;"><button type="submit"><i class="fa fa-pencil"></i></button></form></td>
                            <td><form method="post" action="${pageContext.request.contextPath}/cars/delete?id=${car.id}"
                                      style="margin-bottom: 0;"><button type="submit"><i class="fa fa-trash-o"></i></button></form></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <c:if test="${empty update}">
            <div class="form">
                <h2>Add new car</h2>
                <form action="${pageContext.request.contextPath}/cars/add" method="post">
                    <ul>
                        <li>
                            <div class="input">
                                <h3>Licence plate</h3>
                                <input type="text" name="licencePlate" value="<c:out value='${param.licencePlate}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Brand</h3>
                                <input type="text" name="brand" value="<c:out value='${param.brand}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Model</h3>
                                <input type="text" name="model" value="<c:out value='${param.model}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Price per day</h3>
                                <input type="text" name="pricePerDay" value="<c:out value='${param.pricePerDay}'/>"/>
                            </div>
                        </li>
                    </ul>
                    <button type="Submit">Submit</button>
                </form>
                <c:if test="${not empty error1}">
                    <div class="msg">
                        <c:out value="${error1}"/>
                    </div>
                </c:if>
            </div>
        </c:if>

        <c:if test="${not empty update}">
            <div class="form">
                <h2>Updating car with ID ${update.id}</h2>
                <form action="${pageContext.request.contextPath}/cars/change?id=${update.id}" method="post">
                    <ul>
                        <li>
                            <div class="input">
                                <h3>Licence plate</h3>
                                <input type="text" name="licencePlate" value="<c:out value='${update.licencePlate}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Brand</h3>
                                <input type="text" name="brand" value="<c:out value='${update.brand}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Model</h3>
                                <input type="text" name="model" value="<c:out value='${update.model}'/>"/>
                            </div>
                        </li>
                        <li>
                            <div class="input">
                                <h3>Price per day</h3>
                                <input type="text" name="pricePerDay" value="<c:out value='${update.pricePerDay}'/>"/>
                            </div>
                        </li>
                    </ul>
                    <button type="Submit">Submit</button>
                </form>
                <c:if test="${not empty error2}">
                    <div class="msg">
                        <c:out value="${error2}"/>
                    </div>
                </c:if>
            </div>
        </c:if>
    </div>


</body>
</html>