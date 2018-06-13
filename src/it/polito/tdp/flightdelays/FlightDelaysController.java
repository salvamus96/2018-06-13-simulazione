package it.polito.tdp.flightdelays;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.flightdelays.model.Airline;
import it.polito.tdp.flightdelays.model.Model;
import it.polito.tdp.flightdelays.model.OriginDestination;
import it.polito.tdp.flightdelays.model.Passeggero;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FlightDelaysController {

	private Model model;
	
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea txtResult;

    @FXML
    private ComboBox<Airline> cmbBoxLineaAerea;

    @FXML
    private Button caricaVoliBtn;

    @FXML
    private Button btnSimula;
    
    @FXML
    private TextField numeroPasseggeriTxtInput;

    @FXML
    private TextField numeroVoliTxtInput;

    @FXML
    void doCaricaVoli(ActionEvent event) {
    	
    	this.txtResult.clear();
    	
    	Airline airline = this.cmbBoxLineaAerea.getValue();
    	if (airline == null) {
    		this.txtResult.appendText("ERRORE: selezionare una linea area!\n");
    		return;
    	}
    	
    	model.createGraph(airline);
    							// estrazione dei primi 10 peggiori
    	for (OriginDestination od : model.getWorstEdges().subList(0, 10))	
    		this.txtResult.appendText(od.toString() + "\n");
    
    	this.btnSimula.setDisable(false);
    }

    @FXML
    void doSimula(ActionEvent event) {
    	
    	this.txtResult.clear();
    	try {
    		
    		int numPasseggeri = Integer.parseInt(this.numeroPasseggeriTxtInput.getText());
    		int numVoli = Integer.parseInt(this.numeroVoliTxtInput.getText());
    		
    		Airline airline = this.cmbBoxLineaAerea.getValue();
        	if (airline == null) {
        		this.txtResult.appendText("ERRORE: selezionare una linea area!\n");
        		return;
        	}
        	
        	// evito di creare il grafo quella linea area (suppongo che ci clicchi prima su "Carica voli")
        	// model.createGraph(airline);
            
        	List <Passeggero> passeggeri = new ArrayList<>();
        	
        	for (int id = 0; id < numPasseggeri; id ++)
        		passeggeri.add(new Passeggero(id, numVoli));
    		
        	model.simula(passeggeri, airline);
        	
        	for (Passeggero p : model.getResulSim())
        		this.txtResult.appendText(p.toString() + "\n");
        	
    	}catch (NumberFormatException e) {
    		this.txtResult.appendText("ERRORE: inserire valori numeri validi!\n");
    	}
    }

    @FXML
    void initialize() {
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'FlightDelays.fxml'.";
        assert cmbBoxLineaAerea != null : "fx:id=\"cmbBoxLineaAerea\" was not injected: check your FXML file 'FlightDelays.fxml'.";
        assert caricaVoliBtn != null : "fx:id=\"caricaVoliBtn\" was not injected: check your FXML file 'FlightDelays.fxml'.";
        assert numeroPasseggeriTxtInput != null : "fx:id=\"numeroPasseggeriTxtInput\" was not injected: check your FXML file 'FlightDelays.fxml'.";
        assert numeroVoliTxtInput != null : "fx:id=\"numeroVoliTxtInput\" was not injected: check your FXML file 'FlightDelays.fxml'.";

     	this.txtResult.setStyle("-fx-font-family: monospace");
     	this.btnSimula.setDisable(true);
    }
    
	public void setModel(Model model) {
		this.model = model;
		
		// popolazione del menù a tendina
		this.cmbBoxLineaAerea.getItems().addAll(model.getAllAirlines());
	}
}
