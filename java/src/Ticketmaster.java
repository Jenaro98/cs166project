/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddUser(Ticketmaster esql){//1
		String fname;

		do{
			System.out.print("Enter first name: ");
			try{
				fname = in.readLine();
				if(fname.length() <= 0 || fname.length() > 32){
					throw new RuntimeException("First name can not be empty or exceed 32 characters");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);
		
		String lname;

		do{
			System.out.print("Enter last name: ");
			try{
				lname = in.readLine();
				if(lname.length() <= 0 || lname.length() > 32){
					throw new RuntimeException("Last name can not be empty or exceed 32 characters");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		String email;

		do{
			System.out.print("Enter email: ");
			try{
				email = in.readLine();
				if(email.length() <= 0 || email.length() > 64){
					throw new RuntimeException("Email can not be empty or exceed 64 characters");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		long phone;

		do{
			System.out.print("Enter a 10 digit phone number: ");
			try{
				phone = Long.parseLong(in.readLine());
				break;
			}catch (NumberFormatException e) {
				System.out.println("Invalid input!");
				continue;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		String password;

		do{
			System.out.print("Enter password: ");
			try{
				password = in.readLine();
				if(password.length() <= 0 || password.length() > 64){
					throw new RuntimeException("Password can not be empty or exceed 64 characters");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);


		try{
			String query = "INSERT INTO Users (email, lname, fname, phone, pwd) VALUES (\'" + email + "\', \'" + lname + "\', \'" + fname + "\', " + phone + ", \'" + password + "\');"; 

			esql.executeUpdate(query);
		}catch (Exception e){
			System.err.println(e.getMessage());
		}
				
	}
	
	public static void AddBooking(Ticketmaster esql){//2
	
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		int theaterId;
		int exists = 0;
		int movieId = -1;

		do{//do until existing theater id is input
			do{
				System.out.print("Enter theater id for new movie showing:");
				try{
					theaterId = Integer.parseInt(in.readLine());
					break;
				}catch (NumberFormatException e) {
					System.out.println("Invalid input!");
					continue;
				}catch(Exception e){
					System.out.println(e);
					
				}
			}while(true);

			try{
				String query = "SELECT t.cid FROM Theaters t WHERE t.tid = '" + theaterId + "';";
				exists = esql.executeQuery(query);
				if(exists == 0){
					throw new RuntimeException("Theater Id does not exist! Try again");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				
			}
		}while(exists == 0);

		

		try{
			String query = "SELECT m.mvid FROM Movies m WHERE m.mvid > 0;";
			movieId = esql.executeQueryAndReturnResult(query).size();
			movieId = movieId + 1; //movie id for new movie
			System.out.print("Number of movies: " + movieId + " ");
		}catch(Exception e){
			System.out.print(e);
		}

		String title;
		String relDate;
		String relCntry;
		String des;
		long length;
		String lang;
		String genre;

		do{
			System.out.print("Enter movie title: ");

			try{
				title = in.readLine();
				if(title.length() <= 0 || title.length() > 128){
					throw new RuntimeException("Title can not be empty or exceed 128 characters!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie release date(MM/DD/YYYY): ");

			try{
				relDate = in.readLine();
				if(relDate.length() <= 0 || relDate.length() > 10){
					throw new RuntimeException("Release Date can not be empty or exceed 10 characters including '/''!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie release country: ");

			try{
				relCntry = in.readLine();
				if(relCntry.length() <= 0 || relCntry.length() > 64){
					throw new RuntimeException("Release Country can not be empty or exceed 64 characters!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie description: ");

			try{
				des = in.readLine();
				if(des.length() <= 0 || des.length() > 200){
					throw new RuntimeException("Description can not be empty or exceed 200 characters!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie duration in seconds: ");

			try{
				length = Long.parseLong(in.readLine());
				if(length <= 0 ){
					throw new RuntimeException("Movie Duration can not be empty!");
				}
				break;
			}catch (NumberFormatException e) {
				System.out.println("Invalid input!");
				continue;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie language: ");

			try{
				lang = in.readLine();
				if(lang.length() <= 0 || lang.length() > 2){
					throw new RuntimeException("Language code can not be empty or exceed 2 characters!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		do{
			System.out.print("Enter movie genre: ");

			try{
				genre = in.readLine();
				if(genre.length() <= 0 || genre.length() > 16){
					throw new RuntimeException("Movie Genre can not be empty or exceed 16 characters!");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		try{
			String query = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) VALUES ('" + movieId + "', '" + title + "', '" + relDate + "', '" + relCntry + "', '" + des + "', '" + length + "', '" + lang + "', '" +  genre +"');";
			esql.executeUpdate(query);
		}catch(Exception e){
			System.out.println(e);
		}

		try{
			String query = "INSERT INTO Plays (sid, tid) VALUES ('" + movieId + "', '" + theaterId + "');";
			esql.executeUpdate(query);
		}catch(Exception e){
			System.out.println(e);
		}

		System.out.print("Success adding " + title + " to movie showings to theater with id: " + theaterId + "\n ");

	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		//
		String input;

		do{
			System.out.print("Are you sure you want to cancel pending bookings?(y/n): ");

			try{
				input = in.readLine();
				if(input.equals("y")){
					try{
						String query = "UPDATE BOOKINGS SET status = '" + "Canceled" + "' WHERE status = '" + "Pending" + "';";
						esql.executeUpdate(query);
						System.out.print("Success!!\n");
					}catch (Exception e){
						System.err.println(e.getMessage());
					}
				}else if(!input.equals("y") && !input.equals("n")){
					throw new RuntimeException("Invalid input!");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		int bookingId;
		

		do{
			System.out.print("Enter booking Id to change seat reservations: ");
			try{
				bookingId = Integer.parseInt(in.readLine());
				break;
			}catch(NumberFormatException e){
				System.out.print("Invalid input!");
				continue;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		int seats = 0;
		try{
			String query = "SELECT s.csid FROM ShowSeats s WHERE s.bid = '" + bookingId + "';";
			seats = esql.executeQueryAndReturnResult(query).size();
			System.out.print("You have " + seats + " seats");
		}catch(Exception e){
			System.out.println(e);
		}

		int showId = -1;
		List<List<String>> temp1;
		List<List<String>> temp3;
		//try{
			String query = "SELECT s.sid FROM ShowSeats s WHERE s.bid = '" + bookingId + "';";
			temp1 = esql.executeQueryAndReturnResult(query);

			String query1 = "SELECT s.csid FROM ShowSeats s WHERE s.bid = '" + bookingId + "';";
			temp3 = esql.executeQueryAndReturnResult(query1);

			System.out.print("Show id is: " + temp1.get(0).get(0) + " seats");
		//}catch(Exception e){
		//	System.out.println(e);
		//}

		showId = Integer.parseInt(temp1.get(0).get(0));
		List<List<String>> temp2;
		//try{//get all cinema seat ids for a show
			String query = "SELECT s.csid FROM ShowSeats s WHERE s.sid = '" + showId + "';";
			temp2 = esql.executeQueryAndReturnResult(query);
		//}catch(Exception e){
		//	System.out.println(e);
		//}

		int counter = 0;
		List<Integer> nums;
		do{
			//List<Integer> nums;
			counter = 0; //if not 0 then seat is not available
			int seatNum = -1;
			for(int j = 0; j < seats ; j++){
				System.out.print("Enter new seat number for seat(" + (j + 1) + "): ");
				for(int i = 0; i < temp2.size() ;i++){
					try{
						seatNum = Integer.parseInt(in.readLine());

						String query = "SELECT c.csid FROM CinemaSeats c, ShowSeats s WHERE s.sid = '" + showId + "' AND s.csid = c.csid AND c.sno = '" + seatNum + "';";

						counter = esql.executeQueryAndReturnResult(query).size() + counter;
					}catch(Exception e){
						System.out.println(e);
					}
				}//if counter is 0 after this then seat is available
				if(counter == 0){
					nums.add(seatNum);
				}
				else{
					System.out.print("Seat was not available. Try again");
					continue;
				}
			}
		}while(counter != 0);


		for(int i = 0; i < seats; i++){
			try{
				String query = "UPDATE CinemaSeats SET sno = '" + nums.get(i) + "' WHERE csid = '" + temp3.get(i).get(0) + "';" ;
				esql.executeUpdate(query);
			}catch(Exception e){
				System.out.println(e);
			}
		}

		System.out.print("Success!!\n");
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		int bookId;
		int exists = 0;

		do{
			do{
				System.out.print("Enter booking ID of payment to delete: ");

				try{
					bookId = Integer.parseInt(in.readLine());

					if(bookId <= 0){
						throw new RuntimeException("Booking Id can not be empty! ");
					}
					break;
				}catch(Exception e){
					System.out.println(e);
					continue;
				}
			}while(true);

			try{
				String query = "SELECT p.pid FROM Payments p, Bookings b WHERE b.bid = '" + bookId + "';";
				exists = esql.executeQuery(query);
				if(exists == 0){
					throw new RuntimeException("Booking id does not exist! Try Again");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(exists == 0);

		try{
			String query = "DELETE FROM Payments p WHERE p.bid = '" + bookId + "';";
			esql.executeUpdate(query);

			String query1 = "UPDATE BOOKINGS SET status = '" + "Canceled" + "' WHERE bid = '" + bookId + "';";
			esql.executeUpdate(query1);
		}catch(Exception e){
			System.out.println(e);
		}

		System.out.print("Success!!\n");

	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		String input;
		//boolean response= false;

		do{
			System.out.print("Are you sure you want to clear canceled bookings?(y/n): ");

			try{
				input = in.readLine();
				if(input.equals("y")){
					try{
						String query = "DELETE FROM Bookings WHERE status =  '" + "Canceled" + "';";
						esql.executeUpdate(query);
						System.out.print("Success!!\n");
					}catch (Exception e){
						System.err.println(e.getMessage());
					}
				}else if(!input.equals("y") && !input.equals("n")){
					throw new RuntimeException("Invalid input!");
				}
				break;
			}catch (Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		String date;

		do{
			System.out.print("Enter date for showing(MM/DD/YYYY): ");
			try{
				date = in.readLine();
				if(date.length() < 0 || date.length() > 10){
					throw new RuntimeException("Invalid date. Must not exceed 10 characters including '/''.");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
              continue;
			}
		}while(true);

		String time;

		do{
			System.out.print("Enter time for showing(hh:mm): ");
			try{
				time = in.readLine();
				if(time.length() < 0 || time.length() > 5){
					throw new RuntimeException("Invalid time. Must not exceed 5 characters including ':''.");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		//SELECT M.title FROM Movies M, Shows S WHERE (S.sdate = date AND S.sttime = time) AND S.mvid = U.mvid;
		try{
			String query = "SELECT m.title FROM Movies m, Shows s WHERE s.sdate = '" + date + "' AND s.sttime = '" + time + "' AND s.mvid = m.mvid;";

			esql.executeQueryAndPrintResult(query);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}

	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		String movie;

		do{
			System.out.print("Enter movie: ");

			try{
				movie = in.readLine();

				if(movie.length() < 0 || movie.length() > 128 ){
					throw new RuntimeException("Input can not be empty or exceed 128 characters");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		String cinema;

		do{
			System.out.print("Enter cinema: ");

			try{
				cinema = in.readLine();

				if(cinema.length() < 0 || cinema.length() > 64 ){
					throw new RuntimeException("Input can not be empty or exceed 64 characters");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		String date1;

		do{
			System.out.print("Enter starting date for range(MM/DD/YYYY): ");

			try{
				date1 = in.readLine();

				if(date1.length() < 0 || date1.length() > 10 ){
					throw new RuntimeException("Invalid date. Must not exceed 10 characters including '/''.");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		String date2;

		do{
			System.out.print("Enter ending date for range(MM/DD/YYYY): ");

			try{
				date2 = in.readLine();

				if(date2.length() < 0 || date2.length() > 10 ){
					throw new RuntimeException("Invalid date. Must not exceed 10 characters including '/''.");
				}
				break;
			}catch(Exception e){
				System.out.println(e);
				continue;
			}
		}while(true);

		//we have movie,cinema,date1,date2

		//SELECT m.title, s.sdate, s.sttime FROM Movies m, Shows s, Plays p WHERE 

	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
	
}
