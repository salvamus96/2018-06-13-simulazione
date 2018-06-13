package it.polito.tdp.flightdelays.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.flightdelays.db.FlightDelaysDAO;

public class Simulatore {

	// Parametri di simulazione
	private int anno = 2015;
		
	// Coda di eventi
	private LinkedList<Event> queue;
	
	// Valori in output
	private List <Passeggero> result;
	
	public void init(List<Passeggero> passeggeri, List<Airport> airports) {
		this.queue = new LinkedList<>();
		this.result = new ArrayList<>();
		
		// Inizializzazione: posizionare i passeggeri tra gli aeroporti disponibili in modo casuale
		for (Passeggero p : passeggeri) {
			
			// per generare aeroporti di partenza casuale, assegno ogni passeggero
			// a una posizione casuale (compresa tra 0 e la dimensione della lista) di airports
			
			Airport partenza = airports.get((int) (Math.random() * airports.size() ));
			
			// la data di partenza viene assunta il 01/01/2015 
			this.queue.add(new Event (p, partenza, LocalDateTime.of(anno, 1, 1, 0, 0, 0)));
			
		}
		
//		System.out.println("Inizio simulazione\n\n");
//		for (Event e : this.queue)
//			System.out.println(e + "\n");
		
	}

	public void run(Airline airline, FlightDelaysDAO fdao, AirportIdMap airportIdMap) {

		Event e ;
		while ((e = this.queue.poll()) != null) {
			
			Passeggero passeggero = e.getPasseggero();
			Airport partenza = e.getAirport();
			LocalDateTime dataPartenza = e.getData();

			// ogni passeggero prende il primo volo disponibile dall'aeroporto in cui si trova
			Flight first = fdao.firstFlight(airline, partenza, dataPartenza);

			// se il passeggero ha ancora voli a disposizione e ci sono voli disponibili
			if (passeggero.getVoli() > 0 && first != null) {
					
				Airport destinazione = airportIdMap.get(first.getDestinationAirportId());
				LocalDateTime dataArrivo = first.getArrivalDate();
						
				// il passeggero viaggia, tale evento viene aggiunto alla coda
				Event e1 = new Event(passeggero, destinazione, dataArrivo);
				this.queue.add(e1);
					
				// decremento il numero di voli a disposizione per il passeggero, poichè il volo è stato registrato
				passeggero.voloEffettuato();
					
				// accumulo il ritardo del volo effettuato
				int ritardo = first.getArrivalDelay();
				passeggero.accumuloRitardo(ritardo);
					
			} else
				// il passeggero non può viaggiare
				this.result.add(passeggero);
		
		}
	}



	public List<Passeggero> getResult() {
		return result;
	}
	
	

}
