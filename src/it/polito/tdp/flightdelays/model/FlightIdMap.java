package it.polito.tdp.flightdelays.model;

import java.util.HashMap;
import java.util.Map;

public class FlightIdMap {

private Map <Integer, Flight> map;
	
	
	public FlightIdMap () {
		
		this.map = new HashMap <> ();
	}
	
	public Flight get (int flightId) {
		return map.get(flightId);
	}

	public Flight get (Flight flight) {
		Flight old = map.get(flight.getId());
		if (old == null) {
			map.put(flight.getId(), flight);
			return flight;
		}
		return old;
	}

	public void put (int flightId, Flight flight) {
		map.put(flightId, flight);
	}
	
}
