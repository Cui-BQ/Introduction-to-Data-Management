package flightapp;

import java.io.IOException;
import java.security.*;
import java.security.spec.*;
import java.sql.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.*;


/**
 * Runs queries against a back-end database
 */
public class Query extends QueryAbstract {
  // Password hashing parameter constants
  private static final int HASH_STRENGTH = 65536;
  private static final int KEY_LENGTH = 128;
  private static final int ZERO = 0;
  private String currentUser = null;
  List<Flight> itineraries = new ArrayList<Flight>();

  // Canned queries
  private static final String CHECK_FLIGHT_CAPACITY = "SELECT capacity FROM Flights WHERE fid = ?";
  private PreparedStatement checkFlightCapacityStatement;

  // TODO: YOUR CODE HERE
  private static final String CLEAR_USERS = "DELETE FROM Users;";
  private PreparedStatement clearUsersStatement;

  private static final String CLEAR_RESERVATIONS = "DELETE FROM Reservations;";
  private PreparedStatement clearReservationsStatement;

  private static final String CREATE_CUSTOMER = "INSERT INTO Users VALUES (?, ?, ?, ?);";
  private PreparedStatement createCustomerStatement;

  private static final String CUSTOMER_INFO = "SELECT * FROM Users WHERE Users.username = ?;";
  private PreparedStatement CustomerINFOStatement;

  private static final String DIRECT_FLIGHT = "SELECT TOP (?) day_of_month,carrier_id,flight_num,origin_city,dest_city,actual_time,capacity,price,fid "
                                            + "FROM Flights " 
                                            + "WHERE origin_city = ? AND dest_city = ? AND day_of_month =  ? AND canceled = 0"
                                            + "ORDER BY actual_time ASC";
  private PreparedStatement DirectFlightStatement;

  private static final String INDIRECT_FILGHT = "SELECT TOP (?) f1.fid, f2.fid, f1.carrier_id, f2.carrier_id, f1.flight_num, f2.flight_num, "
                                              +                "f1.dest_city, f1.actual_time, f2.actual_time, f1.capacity, f2.capacity, f1.price, f2.price\n " 
                                              + "FROM Flights AS f1, Flights AS f2\n "
                                              + "WHERE f1.origin_city = ? AND f1.dest_city = f2.origin_city AND f2.dest_city = ? AND "
                                              +       "f1.day_of_month = ? AND f2.day_of_month = f1.day_of_month AND f1.canceled = 0 AND "
                                              +       "f2.canceled = 0\n"
                                              + "ORDER BY (f1.actual_time + f2.actual_time) ASC, f1.fid ASC, f2.fid ASC\n";
  private PreparedStatement IndirectFlightStatement;

  private static final String CHECK_RESERVATION_SAMEDAY = "SELECT COUNT(*) FROM Reservations WHERE username = ? AND day = ?";
  private PreparedStatement CheckReservationSAMEDAYStatement;

  private static final String CHECK_SEATS = "SELECT DISTINCT COUNT(*) " 
                                          + "FROM Reservations "
                                          + "WHERE fid1 = ? OR fid2 = ?";
  private PreparedStatement CheckSeatsStatement;
  
  private static final String CREATE_RESERVATION = "INSERT INTO Reservations VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
  private PreparedStatement createReservationStatement;

  private static final String GET_RESERVATION_ID = "SELECT TOP (1) reservationID FROM Reservations ORDER BY reservationID DESC";
  private PreparedStatement GetReservationIDStatement;

  private static final String RESERVATION_INFO = "SELECT * FROM Reservations WHERE reservationID = ?";
  private PreparedStatement ReservationINFOStatement;

  private static final String UPDATE_PAID = "UPDATE Reservations SET paid = ? WHERE reservationID = ?";
  private PreparedStatement UpdatePaidStatement;

  private static final String UPDATE_BALANCE = "UPDATE Users SET balance = ? WHERE username = ?";
  private PreparedStatement UpdateBalanceStatement;
  

  private static final String SEARCH_RESERVATIONS = "SELECT reservationID, paid, fid1, fid2, cancelled FROM Reservations WHERE username = ?;";
  private PreparedStatement SearchReservationsStatement;

  private static final String SEARCH_FLIGHT = "SELECT day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price " 
                                            + "FROM FLIGHTS " 
                                            + "WHERE fid = ?;";
  private PreparedStatement SearchFlightStatement;

  private static final String UPDATE_CANCELLED = "UPDATE Reservations SET cancelled = ? WHERE reservationID = ?";
  private PreparedStatement UpdateCancelledStatement;

  private static final String BEGIN = "BEGIN TRANSACTION";
  private PreparedStatement BeginStatement;

  private static final String COMMIT = "COMMIT TRANSACTION";
  private PreparedStatement CommitStatement;

  private static final String ROLLBACK = "ROLLBACK TRANSACTION";
  private PreparedStatement RollbackStatement;



  
  

  public Query(Connection conn) throws SQLException {
    super(conn);
    prepareStatements();
  }

  public Query() throws SQLException, IOException {
    this(openConnectionFromDbConn());
  }

  protected Query(String serverURL, String dbName, String adminName, String password)
      throws SQLException {
    this(openConnectionFromCredential(serverURL, dbName, adminName, password));
  }

  /**
   * Clear the data in any custom tables created.
   * <p>
   * WARNING! Do not drop any tables and do not clear the flights table.
   */
  public void clearTables() throws SQLException{
    // TODO: YOUR CODE HERE
    // Note: since failing here break stuff, you don't want
    // to catch exception but instead throw it so your program will
    // be broken right away, easier to debug.
    clearReservationsStatement.executeUpdate();
    clearUsersStatement.executeUpdate();
  }

  /*
   * prepare all the SQL statements in this method.
   */
  private void prepareStatements() throws SQLException {
    checkFlightCapacityStatement = conn.prepareStatement(CHECK_FLIGHT_CAPACITY);
    // TODO: YOUR CODE HERE
    clearUsersStatement = conn.prepareStatement(CLEAR_USERS);
    clearReservationsStatement = conn.prepareStatement(CLEAR_RESERVATIONS);
    createCustomerStatement = conn.prepareStatement(CREATE_CUSTOMER);
    CustomerINFOStatement = conn.prepareStatement(CUSTOMER_INFO);
    DirectFlightStatement = conn.prepareStatement(DIRECT_FLIGHT);
    IndirectFlightStatement = conn.prepareStatement(INDIRECT_FILGHT);
    CheckReservationSAMEDAYStatement = conn.prepareStatement(CHECK_RESERVATION_SAMEDAY);
    CheckSeatsStatement = conn.prepareStatement(CHECK_SEATS);
    createReservationStatement = conn.prepareStatement(CREATE_RESERVATION);
    GetReservationIDStatement = conn.prepareStatement(GET_RESERVATION_ID);
    ReservationINFOStatement = conn.prepareStatement(RESERVATION_INFO);
    UpdatePaidStatement = conn.prepareStatement(UPDATE_PAID);
    UpdateBalanceStatement = conn.prepareStatement(UPDATE_BALANCE);
    SearchReservationsStatement = conn.prepareStatement(SEARCH_RESERVATIONS);
    SearchFlightStatement = conn.prepareStatement(SEARCH_FLIGHT);
    UpdateCancelledStatement = conn.prepareStatement(UPDATE_CANCELLED);
    BeginStatement = conn.prepareStatement(BEGIN);
    RollbackStatement = conn.prepareStatement(ROLLBACK);
    CommitStatement = conn.prepareStatement(COMMIT);
  }

  /**
   * Takes a user's username and password and attempts to log the user in.
   *
   * @param username user's username
   * @param password user's password
   * @return If someone has already logged in, then return "User already logged in\n" For all other
   * errors, return "Login failed\n". Otherwise, return "Logged in as [username]\n".
   */
  public String transaction_login(String username, String password) {
    // TODO: YOUR CODE HERE
    if (currentUser != null) {
      return "User already logged in\n";
    }

    // Access user info 
    try {
      CustomerINFOStatement.clearParameters();
      CustomerINFOStatement.setString(1, username);
      ResultSet result = CustomerINFOStatement.executeQuery();
      result.next();
      // check password
      byte[] salt = result.getBytes(3);
      byte[] computedHash = hashPassword(password, salt);
      byte[] actualHash = result.getBytes(2);
      result.close();
      if (!Arrays.equals(computedHash, actualHash)) {
        return "Login failed\n";
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return "Login failed\n";
    }

    currentUser = username;
    // empty the itineraries from previous user.
    itineraries = new ArrayList<Flight>();
    return "Logged in as " + username + "\n";
  }

  /**
   * Implement the create user function.
   *
   * @param username   new user's username. User names are unique the system.
   * @param password   new user's password.
   * @param initAmount initial amount to deposit into the user's account, should be >= 0 (failure
   *                   otherwise).
   * @return either "Created user {@code username}\n" or "Failed to create user\n" if failed.
   */
  public String transaction_createCustomer(String username, String password, int initAmount) {
    // TODO: YOUR CODE HERE
    // Check if initAmount < 0 
    if (initAmount < 0) {
      return "Failed to create user\n";
    }

    // Generate a random cryptographic salt
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);
    byte[] hash = hashPassword(password, salt);
    try {
      beginMulti();
      createCustomerStatement.clearParameters();
      createCustomerStatement.setString(1, username);
      createCustomerStatement.setBytes(2, hash);
      createCustomerStatement.setBytes(3, salt);
      createCustomerStatement.setInt(4, initAmount);
      createCustomerStatement.executeUpdate();
      commit();
      return "Created user " + username + "\n";
    } catch (SQLException e){
      if (isDeadLock(e)) {
        return transaction_createCustomer(username, password, initAmount);
      }
      rollback();
      e.printStackTrace();
      return "Failed to create user\n";
    } 
      
  }

  /**
   * Implement the search function.
   * <p>
   * Searches for flights from the given origin city to the given destination city, on the given day
   * of the month. If {@code directFlight} is true, it only searches for direct flights, otherwise
   * is searches for direct flights and flights with two "hops." Only searches for up to the number
   * of itineraries given by {@code numberOfItineraries}.
   * <p>
   * The results are sorted based on total flight time.
   *
   * @param originCity
   * @param destinationCity
   * @param directFlight        if true, then only search for direct flights, otherwise include
   *                            indirect flights as well
   * @param dayOfMonth
   * @param numberOfItineraries number of itineraries to return
   * @return If no itineraries were found, return "No flights match your selection\n". If an error
   * occurs, then return "Failed to search\n".
   * <p>
   * Otherwise, the sorted itineraries printed in the following format:
   * <p>
   * Itinerary [itinerary number]: [number of flights] flight(s), [total flight time]
   * minutes\n [first flight in itinerary]\n ... [last flight in itinerary]\n
   * <p>
   * Each flight should be printed using the same format as in the {@code Flight} class.
   * Itinerary numbers in each search should always start from 0 and increase by 1.
   * @see Flight#toString()
   */
  public String transaction_search(String originCity, String destinationCity, boolean directFlight,
                                   int dayOfMonth, int numberOfItineraries) {
    // WARNING the below code is unsafe and only handles searches for direct flights
    // You can use the below code as a starting reference point or you can get rid
    // of it all and replace it with your own implementation.
    //
    // It will not pass test because it try to create a statement, but you
    // can still run FlightService
    //
    // TODO: YOUR CODE HERE

    StringBuffer sb = new StringBuffer();
    itineraries = new ArrayList<Flight>();
    try {
      // one hop itineraries
      
      DirectFlightStatement.clearParameters();
      DirectFlightStatement.setInt(1, numberOfItineraries);
      DirectFlightStatement.setString(2, originCity);
      DirectFlightStatement.setString(3, destinationCity);
      DirectFlightStatement.setInt(4, dayOfMonth);
      ResultSet oneHopResults = DirectFlightStatement.executeQuery();
      // extract info
      while (oneHopResults.next()) {
        int result_fid = oneHopResults.getInt("fid");
        String result_carrierId = oneHopResults.getString("carrier_id");
        String result_flightNum = oneHopResults.getString("flight_num");
        int result_time = oneHopResults.getInt("actual_time");
        int result_capacity = oneHopResults.getInt("capacity");
        int result_price = oneHopResults.getInt("price");
        // make return data
        itineraries.add(new Flight(dayOfMonth, result_fid, ZERO, result_carrierId, null, result_flightNum, 
                                  null, originCity, null, destinationCity, result_time, ZERO, 
                                  result_capacity, ZERO, result_price, ZERO));
      }
      oneHopResults.close();
      
      if (itineraries.size() < numberOfItineraries && directFlight == false) {
        // TODO: write non directFlight quary.
        IndirectFlightStatement.clearParameters();
        IndirectFlightStatement.setInt(1, numberOfItineraries - itineraries.size());
        IndirectFlightStatement.setString(2, originCity);
        IndirectFlightStatement.setString(3, destinationCity);
        IndirectFlightStatement.setInt(4, dayOfMonth);
        ResultSet twoHopResults = IndirectFlightStatement.executeQuery();
        // extract info
        while (twoHopResults.next()) {
          int fid1 = twoHopResults.getInt(1);
          int fid2 = twoHopResults.getInt(2);
          String carrierId1 = twoHopResults.getString(3);
          String carrierId2 = twoHopResults.getString(4);
          String flightNum1 = twoHopResults.getString(5);
          String flightNum2 = twoHopResults.getString(6);    
          String stopoverCity = twoHopResults.getString(7);
          int time1 = twoHopResults.getInt(8);
          int time2 = twoHopResults.getInt(9);
          int capacity1 = twoHopResults.getInt(10);
          int capacity2 = twoHopResults.getInt(11);
          int price1 = twoHopResults.getInt(12);
          int price2 = twoHopResults.getInt(13);
          // make return data
          itineraries.add(new Flight(dayOfMonth, fid1, fid2, carrierId1, carrierId2, flightNum1, 
                                    flightNum2, originCity, stopoverCity, destinationCity, time1, time2, 
                                    capacity1, capacity2, price1, price2));
                                    
        }
        twoHopResults.close();
      }
      Collections.sort(itineraries, new Comparator<Flight>(){
        @Override
        public int compare(Flight f1, Flight f2) {
          if (f1.time1 + f1.time2 == f2.time1 + f2.time2) {
            return f1.fid1 - f2.fid1;
          } else {
            return (f1.time1 + f1.time2) - (f2.time1 + f2.time2);
          }
        }
        
      });
      Iterator<Flight> iter = itineraries.iterator();
      int i = 0;
      while (iter.hasNext()) {
        Flight current = iter.next();
        if (current.stopoverCity == null) {
          sb.append("Itinerary " + i +": 1 flight(s), " + current.time1 + " minutes\n" 
                    + "ID: " + current.fid1 + " Day: " + current.dayOfMonth + " Carrier: " 
                    + current.carrierId1 + " Number: " + current.flightNum1 + " Origin: " 
                    + current.originCity + " Dest: " + current.destCity + " Duration: " 
                    + current.time1 + " Capacity: " + current.capacity1 + " Price: " 
                    + current.price1 + "\n");
        } else {
          sb.append("Itinerary " + i +": 2 flight(s), " + (current.time1 + current.time2) + " minutes\n" 
                    + "ID: " + current.fid1 + " Day: " + current.dayOfMonth + " Carrier: " 
                    + current.carrierId1 + " Number: " + current.flightNum1 + " Origin: " 
                    + current.originCity + " Dest: " + current.stopoverCity + " Duration: " 
                    + current.time1 + " Capacity: " + current.capacity1 + " Price: " 
                    + current.price1 + "\n" 

                    + "ID: " + current.fid2 + " Day: " + current.dayOfMonth + " Carrier: " 
                    + current.carrierId2 + " Number: " + current.flightNum2 + " Origin: " 
                    + current.stopoverCity+ " Dest: " + current.destCity + " Duration: " 
                    + current.time2 + " Capacity: " + current.capacity2 + " Price: " 
                    + current.price2 + "\n");
        }
        i++;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

  /**
   * Implements the book itinerary function.
   *
   * @param itineraryId ID of the itinerary to book. This must be one that is returned by search in
   *                    the current session.
   * @return If the user is not logged in, then return "Cannot book reservations, not logged in\n".
   * If the user is trying to book an itinerary with an invalid ID or without having done a
   * search, then return "No such itinerary {@code itineraryId}\n". If the user already has
   * a reservation on the same day as the one that they are trying to book now, then return
   * "You cannot book two flights in the same day\n". For all other errors, return "Booking
   * failed\n".
   * <p>
   * And if booking succeeded, return "Booked flight(s), reservation ID: [reservationId]\n"
   * where reservationId is a unique number in the reservation system that starts from 1 and
   * increments by 1 each time a successful reservation is made by any user in the system.
   */
  public String transaction_book(int itineraryId) {
    // TODO: YOUR CODE HERE
    if (currentUser == null) {
      return "Cannot book reservations, not logged in\n";
    }
    if (itineraries.size() == 0 || itineraryId < 0 || itineraryId > itineraries.size()-1) {
      return "No such itinerary "+ itineraryId +"\n";
    }
    Flight target = itineraries.get(itineraryId);
    int ID = 1;
    try {
      beginMulti();
      // Check if this user already has a reservation on the same day with target.
      CheckReservationSAMEDAYStatement.clearParameters();
      CheckReservationSAMEDAYStatement.setString(1, currentUser);
      CheckReservationSAMEDAYStatement.setInt(2, target.dayOfMonth);
      ResultSet sameDay = CheckReservationSAMEDAYStatement.executeQuery();
      sameDay.next();
      if (sameDay.getInt(1) != 0) {
        rollback();
        return "You cannot book two flights in the same day\n";
      }
      // Check remaining set for first flight
      CheckSeatsStatement.clearParameters();
      CheckSeatsStatement.setInt(1, target.fid1);
      CheckSeatsStatement.setInt(2, target.fid1);
      ResultSet seat = CheckSeatsStatement.executeQuery();
      seat.next();
      checkFlightCapacityStatement.clearParameters();
      checkFlightCapacityStatement.setInt(1, target.fid1);
      ResultSet capacity = checkFlightCapacityStatement.executeQuery();
      capacity.next();
      // if target is indirect and first flight still has remaining seat.
      if (target.fid2 != 0 && capacity.getInt(1) - seat.getInt(1) > 0) {
        // Check remaining set for second flight
        CheckSeatsStatement.clearParameters();
        CheckSeatsStatement.setInt(1, target.fid2);
        CheckSeatsStatement.setInt(2, target.fid2);
        seat = CheckSeatsStatement.executeQuery();
        seat.next();
        checkFlightCapacityStatement.clearParameters();
        checkFlightCapacityStatement.setInt(1, target.fid1);
        capacity = checkFlightCapacityStatement.executeQuery();
        capacity.next();
      }
      if (capacity.getInt(1) - seat.getInt(1) == 0) {
        rollback();
        return "Booking failed\n";
      }

      // get Reservation ID
      GetReservationIDStatement.clearParameters();
      ResultSet reservationID = GetReservationIDStatement.executeQuery();
      if (reservationID.next()) {
        ID = reservationID.getInt(1) + 1;
      }
      
      // Book the itinerary
      createReservationStatement.clearParameters();
      createReservationStatement.setInt(1, ID);
      createReservationStatement.setString(2, currentUser);
      createReservationStatement.setInt(3, 0);
      createReservationStatement.setInt(4, 0);
      createReservationStatement.setInt(5, target.dayOfMonth);
      createReservationStatement.setInt(6, target.price1 + target.price2);
      createReservationStatement.setInt(7, target.fid1);
      createReservationStatement.setInt(8, target.fid2);
      createReservationStatement.executeUpdate();

    } catch (SQLException e) {
      if (isDeadLock(e)) {
        return transaction_book(itineraryId);
      }
      e.printStackTrace();
      rollback();
      return "Booking failed\n";
    }
    commit();
    return "Booked flight(s), reservation ID: " + ID +"\n";
  }

  /**
   * Implements the pay function.
   *
   * @param reservationId the reservation to pay for.
   * @return If no user has logged in, then return "Cannot pay, not logged in\n" If the reservation
   * is not found / not under the logged in user's name, then return "Cannot find unpaid
   * reservation [reservationId] under user: [username]\n" If the user does not have enough
   * money in their account, then return "User has only [balance] in account but itinerary
   * costs [cost]\n" For all other errors, return "Failed to pay for reservation
   * [reservationId]\n"
   * <p>
   * If successful, return "Paid reservation: [reservationId] remaining balance:
   * [balance]\n" where [balance] is the remaining balance in the user's account.
   */
  public String transaction_pay(int reservationId) {
    // TODO: YOUR CODE HERE
    if (currentUser == null) {
      return "Cannot pay, not logged in\n";
    }
    try {
      beginMulti();
      // Check if this reservation is not found / not under the logged in user's name
      ReservationINFOStatement.clearParameters();
      ReservationINFOStatement.setInt(1, reservationId);
      ResultSet result = ReservationINFOStatement.executeQuery();

      // if this reservation is found
      if (result.next()) {
        String userName = result.getString(2);
        // if not under the logged in user's name
        if (!currentUser.equals(userName) || result.getInt(3) == 1) {
          rollback();
          return "Cannot find unpaid reservation " + reservationId + " under user: " + currentUser + "\n";
        }
      } else {
        rollback();
        return "Cannot find unpaid reservation " + reservationId + " under user: " + currentUser + "\n";
      }

      // check user balance
      int flightPrice = result.getInt(6);
      CustomerINFOStatement.clearParameters();
      CustomerINFOStatement.setString(1, currentUser);
      result = CustomerINFOStatement.executeQuery();
      result.next();
      int balance = result.getInt(4);
      if (flightPrice > balance) {
        rollback();
        return "User has only " + balance + " in account but itinerary costs " + flightPrice + "\n";
      }

      // If user has enough balance, update user balance and reservation paid statu.
      UpdateBalanceStatement.clearParameters();
      UpdateBalanceStatement.setInt(1, balance - flightPrice);
      UpdateBalanceStatement.setString(2, currentUser);
      UpdateBalanceStatement.executeUpdate();

      UpdatePaidStatement.clearParameters();
      UpdatePaidStatement.setInt(1, 1);
      UpdatePaidStatement.setInt(2, reservationId);
      UpdatePaidStatement.executeUpdate();
      commit();
      return "Paid reservation: " + reservationId + " remaining balance: " + (balance - flightPrice) + "\n";
    } catch (SQLException e) {
      if (isDeadLock(e)) {
        return transaction_pay(reservationId);
      }
      e.printStackTrace();
      rollback();
      return "Failed to pay for reservation " + reservationId + "\n";
    }
  }

  /**
   * Implements the reservations function.
   *
   * @return If no user has logged in, then return "Cannot view reservations, not logged in\n" If
   * the user has no reservations, then return "No reservations found\n" For all other
   * errors, return "Failed to retrieve reservations\n"
   * <p>
   * Otherwise return the reservations in the following format:
   * <p>
   * Reservation [reservation ID] paid: [true or false]:\n [flight 1 under the
   * reservation]\n [flight 2 under the reservation]\n Reservation [reservation ID] paid:
   * [true or false]:\n [flight 1 under the reservation]\n [flight 2 under the
   * reservation]\n ...
   * <p>
   * Each flight should be printed using the same format as in the {@code Flight} class.
   * @see Flight#toString()
   */
  public String transaction_reservations() {
    // TODO: YOUR CODE HERE
    if (currentUser == null) {
      return "Cannot view reservations, not logged in\n";
    }
    StringBuffer sb = new StringBuffer();
    // lookup user's reservations
    try {
      SearchReservationsStatement.clearParameters();
      SearchReservationsStatement.setString(1, currentUser);
      ResultSet reservations = SearchReservationsStatement.executeQuery();
      // If no current user doesn't have reservations.
      if (!reservations.isBeforeFirst()) {
        return "No reservations found\n";
      }
      while (reservations.next()) {
        // Skip if this reservation is cancelled.
        if (reservations.getInt(5) == 1 ) {
          continue;
        }
        int reservationID = reservations.getInt(1);
        String paid = (reservations.getInt(2) == 1)?"true:":"false:";
        int fid1 = reservations.getInt(3);
        int fid2 = reservations.getInt(4);
        sb.append("Reservation " + reservationID + " paid: " + paid + "\n");
        sb.append(searchFlight(fid1));
        if (fid2 != 0) {
          sb.append(searchFlight(fid2));
        }
      }
      return sb.toString();
    } catch (SQLException e) {
      e.printStackTrace();
      return "Failed to retrieve reservations\n";
    }
  }

  /**
   * Implements the cancel operation.
   *
   * @param reservationId the reservation ID to cancel
   * @return If no user has logged in, then return "Cannot cancel reservations, not logged in\n" For
   * all other errors, return "Failed to cancel reservation [reservationId]\n"
   * <p>
   * If successful, return "Canceled reservation [reservationId]\n"
   * <p>
   * Even though a reservation has been canceled, its ID should not be reused by the system.
   */
  public String transaction_cancel(int reservationId) {
    // TODO: YOUR CODE HERE
    if (currentUser == null) {
      return "Cannot cancel reservations, not logged in\n";
    }
    // Chech reservationId
    try {
      beginMulti();
      ReservationINFOStatement.clearParameters();
      ReservationINFOStatement.setInt(1, reservationId);
      ResultSet result = ReservationINFOStatement.executeQuery();
      result.next();
      String userName = result.getString(2);
      // If this reservationId isn't belong to the current user.
      if (!userName.equals(currentUser)) {
        rollback();
        return "Failed to cancel reservation " + reservationId + "\n";
      }
      // If this reservation isn't already cancelled.
      if (result.getInt(4) == 0) {
        UpdateCancelledStatement.clearParameters();
        UpdateCancelledStatement.setInt(1, 1);
        UpdateCancelledStatement.setInt(2, reservationId);
        UpdateCancelledStatement.executeUpdate();
      } else {
        rollback();
        return "Failed to cancel reservation " + reservationId + "\n";
      }
      // If this reservation is paid, the refund.
      if (result.getInt(3) == 1) {
        int price = result.getInt(6);
        UpdateBalanceStatement.clearParameters();
        UpdateBalanceStatement.setString(1, ("balance - " + price));
        UpdateBalanceStatement.setString(2, currentUser);
        UpdateBalanceStatement.executeUpdate();
      }
      commit();
      return "Canceled reservation " + reservationId + "\n";

    } catch (SQLException e) {
      if (isDeadLock(e)) {
        return transaction_cancel(reservationId);
      }
      e.printStackTrace();
      rollback();
      return "Failed to cancel reservation " + reservationId + "\n";
    }
  }

  public static boolean isDeadLock(SQLException ex) {
    return ex.getErrorCode() == 1205;
  }

  /**
   * A class to store flight information.
   */
  class Flight {
    public int dayOfMonth;
    public int fid1;
    public int fid2;
    public String carrierId1;
    public String carrierId2;
    public String flightNum1;
    public String flightNum2;
    public String originCity;
    public String stopoverCity;
    public String destCity;
    public int time1;
    public int time2;
    public int capacity1;
    public int capacity2;
    public int price1;
    public int price2;

    public Flight (int dayOfMonth, int fid1, int fid2, String carrierId1, String carrierId2, 
                  String flightNum1, String flightNum2, String originCity, String stopoverCity, 
                  String destCity, int time1, int time2, int capacity1, int capacity2, int price1, 
                  int price2) {
      
      this.dayOfMonth = dayOfMonth;
      this.fid1 = fid1;
      this.fid2 = fid2;
      this.carrierId1 = carrierId1;
      this.carrierId2 = carrierId2;
      this.flightNum1 = flightNum1;
      this.flightNum2 = flightNum2;
      this.originCity = originCity;
      this.stopoverCity = stopoverCity;
      this.destCity = destCity;
      this.time1 = time1;
      this.time2 = time2;
      this.capacity1 = capacity1;
      this.capacity2 = capacity2;
      this.price1 = price1;
      this.price2 = price2;
    }

  }

  /*
    Helper method to compute hash password.
  */
  private byte[] hashPassword (String password, byte[] salt) {
    // Specify the hash parameters
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, HASH_STRENGTH,KEY_LENGTH);

    // Generate the hash
    SecretKeyFactory factory = null;
    byte[] hash = null;

    try {
      factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      hash = factory.generateSecret(spec).getEncoded();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
        throw new IllegalStateException();
    }
    return hash;
  }

  private String searchFlight (int fid) {
    try {
      SearchFlightStatement.clearParameters();
      SearchFlightStatement.setInt(1, fid);
      ResultSet result = SearchFlightStatement.executeQuery();
      result.next();
      int day = result.getInt(1);
      String carrierID = result.getString(2);
      int flightNum = result.getInt(3);
      String originCity = result.getString(4);
      String destCity = result.getString(5);
      int time = result.getInt(6);
      int capacity = result.getInt(7);
      int price = result.getInt(8);
      result.close();
      return ("ID: " + fid + " Day: " + day + " Carrier: " + carrierID + " Number: " + flightNum + 
              " Origin: " + originCity + " Dest: " + destCity + " Duration: " + time + 
              " Capacity: " + capacity + " Price: " + price + "\n");
    } catch (SQLException e) {
      // day_of_month, carrier_id, flight_num, origin_city, dest_city, actual_time, capacity, price
      e.printStackTrace();
      return null;
    }
  }

  private void beginMulti() {
    try {
      conn.setAutoCommit(false);
      BeginStatement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
  }

  private void rollback() {
    try {
      RollbackStatement.executeUpdate();
      conn.setAutoCommit(true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void commit() {
    try {
      CommitStatement.executeUpdate();
      conn.setAutoCommit(true);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
