package it.polito.tdp.flightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.flightdelays.model.Airline;
import it.polito.tdp.flightdelays.model.Airport;
import it.polito.tdp.flightdelays.model.AirportIdMap;
import it.polito.tdp.flightdelays.model.Flight;
import it.polito.tdp.flightdelays.model.OriginDestination;

public class FlightDelaysDAO {

	/**
	 * Restituisce tutte le linee aree presenti nel DB
	 * @return
	 */
	public List<Airline> loadAllAirlines() {
		String sql = "SELECT id, airline from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getString("ID"), rs.getString("airline")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports() {
		String sql = "SELECT id, airport, city, state, country, latitude, longitude FROM airports";
		List<Airport> result = new ArrayList<Airport>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport airport = new Airport(rs.getString("id"), rs.getString("airport"), rs.getString("city"),
						rs.getString("state"), rs.getString("country"), rs.getDouble("latitude"), rs.getDouble("longitude"));
				result.add(airport);
			}
			
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT id, airline, flight_number, origin_airport_id, destination_airport_id, scheduled_dep_date, "
				+ "arrival_date, departure_delay, arrival_delay, air_time, distance FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("id"), rs.getString("airline"), rs.getInt("flight_number"),
						rs.getString("origin_airport_id"), rs.getString("destination_airport_id"),
						rs.getTimestamp("scheduled_dep_date").toLocalDateTime(),
						rs.getTimestamp("arrival_date").toLocalDateTime(), rs.getInt("departure_delay"),
						rs.getInt("arrival_delay"), rs.getInt("air_time"), rs.getInt("distance"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	/**
	 * Data una linea area, restituisce tutti gli aeroporti supportati da essa
	 * @param airline
	 * @param airportIdMap
	 * @return
	 */
	public List<Airport> getAllAirportFromAirline(Airline airline, AirportIdMap airportIdMap) {

		String sql = "SELECT DISTINCT a.id, a.airport, a.city, a.state, a.country, a.latitude, a.longitude " + 
				 	 "FROM airports AS a, flights AS f " + 
				 	 "WHERE (a.ID = f.ORIGIN_AIRPORT_ID or a.ID = f.DESTINATION_AIRPORT_ID) AND f.AIRLINE = ? ";
	
		List<Airport> result = new ArrayList<Airport>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, airline.getId());
			ResultSet rs = st.executeQuery();
	
			while (rs.next()) {
				Airport airport = new Airport(rs.getString("a.id"), rs.getString("a.airport"), rs.getString("a.city"),
						rs.getString("a.state"), rs.getString("a.country"), rs.getDouble("a.latitude"), rs.getDouble("a.longitude"));
				// se non sono presenti nella mappa, li crea e li aggiunge
				result.add(airportIdMap.get(airport));
			}
			
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}

	}

	/**
	 * Data una linea aerea, restituisce tutti i collegamenti tra un aeroporto di origine
	 * e uno di partenza, calcolando il peso dell'arco come (media di ritardo della tratta)/(distanza tratta)
	 * @param airline
	 * @param airportIdMap
	 * @return
	 */
	public List<OriginDestination> getAllEdges(Airline airline, AirportIdMap airportIdMap) {

		String sql = "SELECT DISTINCT ORIGIN_AIRPORT_ID as origin, DESTINATION_AIRPORT_ID as destination, AVG(ARRIVAL_DELAY) as AVG " + 
					 "FROM flights " + 
					 "WHERE AIRLINE = ? " +
					 "GROUP BY origin, destination";
		
		List <OriginDestination> result = new ArrayList<>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, airline.getId());
			ResultSet rs = st.executeQuery();
	
			while (rs.next()) {
				Airport origin = airportIdMap.get(rs.getString("origin"));
				Airport destination = airportIdMap.get(rs.getString("destination"));
				
				// controllo fondamentale per poter proseguire 
				if (origin != null && destination != null)
					result.add(new OriginDestination(origin, destination, rs.getDouble("AVG")));
			}
	
			conn.close();
			return result;
	
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	
	}
	
	/**
	 * Data una linea aerea, l'aeroporto di partenza e una data, restituisce il primo volo dispobinile
	 * @param airline
	 * @param partenza
	 * @param dataPartenza
	 * @return
	 */
	public Flight firstFlight(Airline airline, Airport partenza, LocalDateTime dataPartenza) {
		
		String sql = "SELECT * " + 
				 	 "FROM flights " + 
				 	 "WHERE AIRLINE = ? " +
				 	 "		AND ORIGIN_AIRPORT_ID = ? " + 
				 	 "      AND SCHEDULED_DEP_DATE > ? " + 
				 	 "ORDER BY SCHEDULED_DEP_DATE";

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setString(1, airline.getId());
			st.setString(2, partenza.getId());
			st.setString(3, dataPartenza.toString());
			ResultSet rs = st.executeQuery();
			
			Flight flight = null;
			if (rs.next()) {
			
				flight = new Flight(rs.getInt("id"), rs.getString("airline"), rs.getInt("flight_number"),
					rs.getString("origin_airport_id"), rs.getString("destination_airport_id"),
					rs.getTimestamp("scheduled_dep_date").toLocalDateTime(),
					rs.getTimestamp("arrival_date").toLocalDateTime(), rs.getInt("departure_delay"),
					rs.getInt("arrival_delay"), rs.getInt("air_time"), rs.getInt("distance"));
			}
			
			conn.close();
			return flight;
			
		} catch (SQLException e) {
		e.printStackTrace();
		System.out.println("Errore connessione al database");
		throw new RuntimeException("Error Connection Database");
		}
	
	}	


}
