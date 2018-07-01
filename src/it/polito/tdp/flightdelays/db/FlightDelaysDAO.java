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
import it.polito.tdp.flightdelays.model.AirlineIdMap;
import it.polito.tdp.flightdelays.model.Airport;
import it.polito.tdp.flightdelays.model.AirportIdMap;
import it.polito.tdp.flightdelays.model.Flight;
import it.polito.tdp.flightdelays.model.FlightIdMap;
import it.polito.tdp.flightdelays.model.OriginDestination;

public class FlightDelaysDAO {

	/**
	 * Restituisce tutte le linee aree presenti nel DB
	 * @return
	 */
	public List<Airline> loadAllAirlines(AirlineIdMap map) {
		String sql = "SELECT id, iata_code, airline from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airline airline = new Airline(rs.getInt("id"), rs.getString("iata_code"), rs.getString("airline"));
				result.add(map.get(airline));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Airport> loadAllAirports(AirportIdMap map) {
		String sql = "SELECT id, iata_code, airport, city, state, country, latitude, longitude, timezone_offset FROM airports";
		List<Airport> result = new ArrayList<Airport>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport airport = new Airport(rs.getInt("id"), rs.getString("iata_code"), rs.getString("airport"), rs.getString("city"),
						rs.getString("state"), rs.getString("country"), rs.getDouble("latitude"), rs.getDouble("longitude"), rs.getDouble("timezone_offset"));
				result.add(map.get(airport));
			}
			
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights(AirlineIdMap airlineMap, AirportIdMap airportMap, FlightIdMap flightMap) {
		String sql = "SELECT id, airline_id, flight_number, tail_number, origin_airport_id, destination_airport_id, scheduled_departure_date, "
				+ "departure_delay, elapsed_time, distance, arrival_date, arrival_delay FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				
				Airport origin = airportMap.get(rs.getInt("origin_airport_id"));
				Airport destination = airportMap.get(rs.getInt("destination_airport_id"));
				Airline airline = airlineMap.get(rs.getInt("airline_id"));
				
				if (origin != null && destination != null && airline != null) {
				
					Flight flight = new Flight(rs.getInt("id"), rs.getInt("airline_id"), rs.getInt("flight_number"), rs.getString("tail_number"),
							rs.getInt("origin_airport_id"), rs.getInt("destination_airport_id"),
							rs.getTimestamp("scheduled_departure_date").toLocalDateTime(), rs.getDouble("departure_delay"),
							rs.getDouble("elapsed_time"), rs.getInt("distance"), 
							rs.getTimestamp("arrival_date").toLocalDateTime(), 
							rs.getInt("arrival_delay"));
					
					result.add(flightMap.get(flight));
					
					// occorre salvare i rifermenti di flight nelle varie liste
					origin.getFlights().add(flightMap.get(flight));
					destination.getFlights().add(flightMap.get(flight));
					airline.getFlights().add(flightMap.get(flight));
				}			
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
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

		String sql = "SELECT DISTINCT a.id, iata_code, airport, city, state, country, latitude, longitude, timezone_offset " + 
				 	 "FROM airports AS a, flights AS f " + 
				 	 "WHERE (a.ID = f.ORIGIN_AIRPORT_ID or a.ID = f.DESTINATION_AIRPORT_ID) " +
				 	 "  	AND f.AIRLINE_ID = ? ";
	
		List<Airport> result = new ArrayList<Airport>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, airline.getId());
			ResultSet rs = st.executeQuery();
	
			while (rs.next()) {
				Airport airport = new Airport(rs.getInt("a.id"), rs.getString("iata_code"), rs.getString("airport"), rs.getString("city"),
						rs.getString("state"), rs.getString("country"), rs.getDouble("latitude"), rs.getDouble("longitude"), rs.getDouble("timezone_offset"));
				
				// se non sono presenti nella mappa, li crea e li aggiunge
				result.add(airportIdMap.get(airport));
			}
			
			conn.close();
			return result;
			
		} catch (SQLException e) {
			e.printStackTrace();
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
					 "WHERE AIRLINE_ID = ? " +
					 "GROUP BY origin, destination";
		
		List <OriginDestination> result = new ArrayList<>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, airline.getId());
			ResultSet rs = st.executeQuery();
	
			while (rs.next()) {
				Airport origin = airportIdMap.get(rs.getInt("origin"));
				Airport destination = airportIdMap.get(rs.getInt("destination"));
				
				// controllo fondamentale per poter proseguire 
				if (origin != null && destination != null)
					result.add(new OriginDestination(origin, destination, rs.getDouble("AVG")));
			}
	
			conn.close();
			return result;
	
		} catch (SQLException e) {
			e.printStackTrace();
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
				 	 "WHERE AIRLINE_ID = ? " +
				 	 "		AND ORIGIN_AIRPORT_ID = ? " + 
				 	 "      AND SCHEDULED_DEPARTURE_DATE > ? " + 
				 	 "ORDER BY SCHEDULED_DEPARTURE_DATE";

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, airline.getId());
			st.setInt(2, partenza.getId());
			st.setString(3, dataPartenza.toString());
			ResultSet rs = st.executeQuery();
			
			Flight flight = null;
			if (rs.next()) {
			
				flight = new Flight(rs.getInt("id"), rs.getInt("airline_id"), rs.getInt("flight_number"), rs.getString("tail_number"),
						rs.getInt("origin_airport_id"), rs.getInt("destination_airport_id"),
						rs.getTimestamp("scheduled_departure_date").toLocalDateTime(), rs.getDouble("departure_delay"),
						rs.getDouble("elapsed_time"), rs.getInt("distance"), 
						rs.getTimestamp("arrival_date").toLocalDateTime(), 
						rs.getInt("arrival_delay"));
			}
			
			conn.close();
			return flight;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Error Connection Database");
		}
	
	}	


}
