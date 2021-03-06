<%@ page contentType="text/html; charset=iso-8859-1" language="java"
import="java.sql.*" errorPage="" %>
<html>
	<body>
		<h2>Fancy Clock, v3</h2>
		<p>The time is now: <%= new java.util.Date() %></p>

		<% 
		String url = "jdbc:mysql://${db.host}:3306/${db.name}";
		String user= "${db.bps.user}";
		String pass= "${db.bps.user.password}";
		boolean isAvail = false;
		try{
		Class.forName ("{$db.jdbc.driver.class}").newInstance ();
			Connection conn = DriverManager.getConnection(url, user, pass);
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT lockoutActive lo FROM DBInfo");

			if(rs.next()){
				isAvail = !rs.getBoolean("lo"); 
				out.println("<p>Got lockout from DB...</p>");
			} else {
				out.println("<p>Problem getting lockout from DB...</p>");
			}
			rs.close();
			conn.close();
		} catch(Exception e) {
			out.println("<p>Error Encountered trying to get to database:\n"
			+e.toString()+"\n</p>");
		}
		%>
		<h2>The BPS Database <%= isAvail?"IS":"IS <i>NOT</i>" %> available.</h2>

	</body>
</html>

