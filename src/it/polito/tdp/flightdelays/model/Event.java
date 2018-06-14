package it.polito.tdp.flightdelays.model;

import java.time.LocalDateTime;

public class Event {

	private Passeggero passeggero;
	private Airport airport; // aeroporto in cui si trova il passegero in quella data
	private LocalDateTime data;
	
	public Event(Passeggero passeggero, Airport airport, LocalDateTime data) {
		super();
		this.passeggero = passeggero;
		this.airport = airport;
		this.data = data;
	}

	public Passeggero getPasseggero() {
		return passeggero;
	}

	public void setPasseggero(Passeggero passeggero) {
		this.passeggero = passeggero;
	}

	public Airport getAirport() {
		return airport;
	}

	public void setAirport(Airport airport) {
		this.airport = airport;
	}

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return String.format("%s --- %s --- %s", this.passeggero, this.airport, this.data);
	}
	
	
	
}
