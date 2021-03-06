package fr.regate.project.controller;

import fr.regate.project.view.*;
import fr.regate.project.model.*;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.*;

public class Controller {
    private DTimer chrono;
    private LancementRegate runRegate;
    private LoadView views;
    private Manager manager;

    public Controller(LoadView views) {
    	chrono = new DTimer(views.showRunRegateView());
        runRegate = views.showRunRegateView();
        this.views = views;

        manager = Manager.getGestion();
    }

    public void runRegRunChrono() {
        if (chrono.isRunning()) {
            JOptionPane.showMessageDialog(null, "le chronomètre Tourne vous ne pouvez pas le lancer.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }else if (runRegate.getLblChrono().getText() != "00:00:00") {
            JOptionPane.showMessageDialog(null, "le chronomètre n'est pas à 0. Veuillez le réinitialiser.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }else {
            chrono.startDTimer();
        }
    }

    public void runRegStopChrono() {
        if (chrono.isRunning()) {
            chrono.stopDTimer();
        } else {
            JOptionPane.showMessageDialog(null, "le chronomètre ne tourne pas!", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void runRegReinitChrono() {
        if (chrono.isRunning()) {
            JOptionPane.showMessageDialog(null, "le chronomètre tourne! impossible de le réinitialiser.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }else {
            int option = JOptionPane.showConfirmDialog(null, "Etes-vous sur de vouloir réinitialiser le chronomètre?", "Reinitialisation chronomètre", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if(option == JOptionPane.OK_OPTION){
                chrono.reinitDTimer();
                runRegate.setLblChrono("00:00:00");
            }
        }
    }

    public void runRegValidate() {
        if (chrono.isRunning() && runRegate.regateIsLoad()) {
            JOptionPane.showMessageDialog(null, "Une régate est en cours. Impossible d'en selectionner une autre.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }else {
        	clearJtableRunRegate();
        }
    }

    private enum ViewName {
        ACCUEIL,
        ADD_PARTICIPANT,
        ADD_SHIP,
        MODIF_PARTICIPANT,
        ADD_REGATE,
        CLASSEMENT,
        RUN_REGATE,
        MODIF_REGATE,
        SUPPR_REGATE;
    }

    public void showView(String viewName) {
        switch (ViewName.valueOf(viewName)) {
            case ACCUEIL:
                views.showAccueilView();
            case ADD_PARTICIPANT:
                views.showAddParticipantView();
                break;
            case MODIF_PARTICIPANT :
            	views.showModifParticipantView(this.getAllNameParticipants());
            	break;
            case ADD_SHIP :
            	views.showAddShipView();
            	break;
            case ADD_REGATE:
                views.showAddRegateView(this.getAllNameParticipants(), this.getAllShip());
                break;
            case CLASSEMENT:
                views.showClassementView(getRunRegate());
                break;
            case RUN_REGATE:
                views.showRunRegateView(getAllRegate());
                break;
            case MODIF_REGATE:
                views.showModifRegateView(this.getAllNameParticipants(), this.getAllShip(), this.getAllRegate());
                break;
            case SUPPR_REGATE:
            	views.showDeletRegateView(getAllRegate());
            	break;
        }
    }
    
    public void bddAddParticipant() {
    	String nameParticipant = views.getAp().getNameParticipant();
    	String firstNameParticipant = views.getAp().getFirstName();
    	String phoneNumber = views.getAp().getPhoneNumber();
    	String email = views.getAp().getEmail();
    	
    	try {
			RequestBdd.reqAddParticipant(nameParticipant, firstNameParticipant, phoneNumber, email);
			JOptionPane.showMessageDialog(null, nameParticipant + " " + firstNameParticipant + " à bien été ajouté a la base de données", "information", JOptionPane.INFORMATION_MESSAGE);
			
			// Refresh all the text field
			views.getAp().setNameParticipant("");
			views.getAp().setFirstName("");
			views.getAp().setPhoneNumber("");
			views.getAp().setEmail("");

            this.refreshManagerInfo();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void bddAddRegate() {
    	String nameRegate = views.getAr().getNameRegate();
    	Date dateRegate =new java.sql.Date(views.getAr().getDateRegate().getTime());

    	String startPlace = views.getAr().getPlaceDeparture();
    	String endPlace = views.getAr().getPlaceArrival();
    	int distance = views.getAr().getDistance();
    	int status = 0;
    	
    	try {
			RequestBdd.reqAddRegate(nameRegate, dateRegate, startPlace, endPlace, distance, status);
			JOptionPane.showMessageDialog(null, nameRegate + " à bien été ajouté a la base de données", "information", JOptionPane.INFORMATION_MESSAGE);
			
			// Refresh all the text field
			views.getAr().setNameRegate("");
			views.getAr().setPlaceDeparture("");
			views.getAr().setPlaceArrival("");
			views.getAr().setDistance("");

			this.refreshManagerInfo();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void bddAddShip() {
    	String nameShip = views.getAb().getNameShip();
    	int categoryShip = Integer.parseInt(views.getAb().getCategory());
    	int rating = Integer.parseInt(views.getAb().getRating());
    	
    	try {
			RequestBdd.reqAddShip(nameShip, categoryShip, rating);
			JOptionPane.showMessageDialog(null,"le bateau " + nameShip + " à bien été ajouté a la base de données", "information", JOptionPane.INFORMATION_MESSAGE);
			
			this.refreshManagerInfo();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void bddLinkRegateToPart() {
    	this.refreshManagerInfo();
    	
    	int pos=0;
    	ArrayList<Integer> mesBateauxId = new ArrayList<Integer>();
    	ArrayList<Integer> mesParticipantsId = new ArrayList<Integer>();
    	Regate maRegate;
    	//Récupère la dernière régate enregistrer dans le manager
    	maRegate = manager.getAllRegates().get(manager.getAllRegates().size()-1);
    	for (int i = 0; i<20; i++) {
			if (views.getAr().getTableParticipants().getValueAt(i, 0) != null) {
				mesParticipantsId.add(Integer.parseInt(views.getAr().getTableParticipants().getValueAt(i, 0).toString()));
				mesBateauxId.add(Integer.parseInt(views.getAr().getTableParticipants().getValueAt(i, 3).toString()));
			}
		}
    	for(int i = 0; i < mesParticipantsId.size(); i++) {
    		try {
    			RequestBdd.reqLinkPartToRegate(mesParticipantsId.get(i), maRegate.getIdRegate(), mesBateauxId.get(i), maRegate.getDateRegate());
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	
    	}	
    }
    
    //Même fonctionnement que linkRegateToPart mais permet de modifier une régate en particulier et non la derniere ajouté 
    public void bddUpdateRegateToPart(int idRegate){
    	this.refreshManagerInfo();
    	
    	int pos=0;
    	ArrayList<Integer> mesBateauxId = new ArrayList<Integer>();
    	ArrayList<Integer> mesParticipantsId = new ArrayList<Integer>();
    	
    	for (int i = 0; i<20; i++) {
			if (views.getAr().getTableParticipants().getValueAt(i, 0) != null) {
				mesParticipantsId.add(Integer.parseInt(views.getAr().getTableParticipants().getValueAt(i, 0).toString()));
				mesBateauxId.add(Integer.parseInt(views.getAr().getTableParticipants().getValueAt(i, 3).toString()));
			}
		}
    	for(int i = 0; i < mesParticipantsId.size(); i++) {
    		try {
    			RequestBdd.reqLinkPartToRegate(mesParticipantsId.get(i), idRegate, mesBateauxId.get(i), null);
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}	
    	}	
    }
    
    public void bddUpdateRegate(){
	   
	   int idRegate = Integer.parseInt(views.getAr().getIdRegate());
	   String nameRegate = views.getAr().getNameRegate();
	   Date dateRegate = null;
	   String startPlace = views.getAr().getPlaceDeparture();
	   String endPlace = views.getAr().getPlaceArrival();
	   int distance = views.getAr().getDistance();
	   int status = 0;
	   	
	   try {
		RequestBdd.reqUpdateRegate(idRegate, nameRegate, startPlace, endPlace, distance, status);
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   bddUpdateRegateToPart(idRegate);
    }

    public void bddDeletRegate() {
   		Regate maRegate = manager.getAllRegates().get(views.getSr().getcboDelRegate().getSelectedIndex());
   		try {
   			int value = JOptionPane.showConfirmDialog(null,"êtes vous sûr de vouloir supprimer " + maRegate.getIdRegate() + " : " + maRegate.getNameRegate() + " de la base de données", "Alerte", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
			if(value == 0) {
				RequestBdd.reqDeletRegate(maRegate.getIdRegate());
				JOptionPane.showMessageDialog(null,"la regate " + maRegate.getNameRegate() + " a bien été supprimé de la base de données", "information", JOptionPane.INFORMATION_MESSAGE);
			}
   		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	}
   	
   	public void bddFinishRegate() {
   		int distanceRegate = 0;
   		try {
			for(Regate laRegate : RequestBdd.getListRegate()) {
				if(laRegate.getIdRegate() == Integer.valueOf(views.getLr().getIdRegate())) {
					distanceRegate = laRegate.getDistance();
				}
			} 
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   		ArrayList<Integer> mesParticipantsId = new ArrayList<Integer>();
   		ArrayList<Integer> ratingShip = new ArrayList<Integer>();
    	ArrayList<Time> tempsParticipants = new ArrayList<Time>();
    	for (int i = 0; i<20; i++) {
			if (views.getLr().getTableParticipants().getValueAt(i, 0) != null) {
				mesParticipantsId.add(Integer.parseInt(views.getLr().getTableParticipants().getValueAt(i, 0).toString()));
				ratingShip.add(Integer.valueOf(views.getLr().getTableParticipants().getValueAt(i, 3).toString()));
				if(views.getLr().getTableParticipants().getValueAt(i, 6) == "Abandon") {
					tempsParticipants.add(Time.valueOf("00:00:00"));
				}else {
					tempsParticipants.add(Time.valueOf(String.valueOf(views.getLr().getTableParticipants().getValueAt(i, 6))));
				}
				
			}
		}
    	for(int i = 0; i < mesParticipantsId.size(); i++) {
    		try {
    			if(tempsParticipants.get(i).getTime() == -3600000){
    				RequestBdd.reqUpdateTimePart(mesParticipantsId.get(i), Integer.parseInt(views.getLr().getIdRegate()), tempsParticipants.get(i).getTime(), 0);
    			}else {
    				long tempsCompense =(long) (Math.round(tempsParticipants.get(i).getTime()/1000+3600)+(5143/Math.sqrt(ratingShip.get(i)+3.5))*distanceRegate);
    				RequestBdd.reqUpdateTimePart(mesParticipantsId.get(i), Integer.parseInt(views.getLr().getIdRegate()), tempsParticipants.get(i).getTime(), tempsCompense);
    			}
    			
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if(mesParticipantsId.size() > 0) {
			try {
				RequestBdd.reqUpdateRegateFinish(Integer.parseInt(views.getLr().getIdRegate()));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
   	}
    
    public String[] getAllNameParticipants() {
        this.refreshManagerInfo();
    	ArrayList<String> mesParticipants = new ArrayList<String>();
    	for(Participant monParticipant : manager.getAllParticipants()) {
            mesParticipants.add(monParticipant.getIdParticipant() + " : " + monParticipant.getName() + " " + monParticipant.getFirstName());
        }
    		String[] stringArray = mesParticipants.toArray(new String[0]);
    	return stringArray;
    }
    
    public String[] getAllShip() {
    	this.refreshManagerInfo();
    	ArrayList<String> mesBateaux = new ArrayList<String>();
    	for (Ship monBateau : manager.getAllShip()) {
    		mesBateaux.add(monBateau.getIdShip() + " : " + monBateau.getNameShip()); 
    	}
    	String[] StringArray = mesBateaux.toArray(new String[0]);
    	return StringArray;
    }
    
    public String[] getAllRegate() {
    	this.refreshManagerInfo();
    	ArrayList<String> mesRegates = new ArrayList<String>();
    	for (Regate maRegate : manager.getAllRegates()) {
    		if(maRegate.getStatus() < 1) {    			
    			mesRegates.add(maRegate.getIdRegate() + " : " + maRegate.getNameRegate());
    		}
    	}
    	String[] StringArray = mesRegates.toArray(new String[0]);
    	return StringArray;
    }
    
    public String[] getRunRegate() {
    	this.refreshManagerInfo();
    	ArrayList<String> mesRegates = new ArrayList<String>();
    	for (Regate maRegate : manager.getRunRegates()) {			
    		mesRegates.add(maRegate.getIdRegate() + " : " + maRegate.getNameRegate());
    	}
    	String[] StringArray = mesRegates.toArray(new String[0]);
    	return StringArray;
    }
    
    public void ajoutParticipantTable(){
    	this.refreshManagerInfo();
		Participant monParticipant;
    	Ship monBateau;
		Boolean verif = false;
		if (views.getAr().getTableParticipants().getValueAt(19, 0) == null) {
			int pos = 0;
			monParticipant = manager.getAllParticipants().get(views.getAr().getCboSelParticipant().getSelectedIndex());
			monBateau = manager.getAllShip().get(views.getAr().getCboSelShip().getSelectedIndex());
			for (int i = 0; i<20; i++) {
				if (views.getAr().getTableParticipants().getValueAt(i, 0) != null) {
					pos += 1;
					// Compare if the participant is already registered in the regate
					if(views.getAr().getTableParticipants().getValueAt(i, 0).toString().contains(String.valueOf(monParticipant.getIdParticipant())) 
							&& views.getAr().getTableParticipants().getValueAt(i, 0).toString().contains(String.valueOf(monParticipant.getIdParticipant()))) {
						verif = true;
					}
				}
			}
			if (verif == false) {
				views.getAr().setTableParticipants(String.valueOf(monParticipant.getIdParticipant()), pos, 0);
				views.getAr().setTableParticipants(monParticipant.getName(), pos, 1);
				views.getAr().setTableParticipants(monParticipant.getFirstName(), pos, 2);
				views.getAr().setTableParticipants(String.valueOf(monBateau.getIdShip()), pos, 3);
				views.getAr().setTableParticipants(monBateau.getNameShip(), pos, 4);
				views.getAr().setTableParticipants(String.valueOf(monBateau.getCategoryShip() ), pos, 5);
				views.getAr().setTableParticipants(String.valueOf(monBateau.getRating()), pos, 6);
			}else {
				JOptionPane.showMessageDialog(null, monParticipant.getName() +" "+ monParticipant.getFirstName() + " est déjà inscrit !", "information", JOptionPane.INFORMATION_MESSAGE);
			}	
		}
    }
    
    public void infoClassement() {
    	refreshManagerInfo();
    	clearTableClassement();
    	
    	Regate maRegate = manager.getRunRegates().get(views.getCla().getCboSelRegate().getSelectedIndex());
    	Hashtable<String, String> participantAndShip;
    	Hashtable<String, String> participantAbandon = new Hashtable<String, String>();
    	int row =0;
    	int j = 0;
    	try {
			participantAndShip = RequestBdd.getParticipantsInscrit(maRegate.getIdRegate());
			for (int i= 0; i < participantAndShip.size()/9; i++) {
				
				if(participantAndShip.get("tempsReel"+i).equals("0")) {
					participantAbandon.put("idPart"+j, participantAndShip.get("idPart"+i));
					participantAbandon.put("lastName"+j, participantAndShip.get("lastName"+i));
					participantAbandon.put("firstName"+j, participantAndShip.get("firstName"+i));
					participantAbandon.put("nameShip"+j, participantAndShip.get("nameShip"+i));
					j++;
				}else {					
					views.getCla().setTableClassement(String.valueOf(row+1), row, 0);
					views.getCla().setTableClassement(String.valueOf(participantAndShip.get("idPart"+i)), row, 1);
					views.getCla().setTableClassement(participantAndShip.get("lastName"+i), row, 2);
					views.getCla().setTableClassement(participantAndShip.get("firstName"+i), row, 3);
					views.getCla().setTableClassement(participantAndShip.get("nameShip"+i), row, 4);
					views.getCla().setTableClassement(convertMinutes(Integer.valueOf(participantAndShip.get("tempsReel"+i))), row, 5);
					views.getCla().setTableClassement(convertMinutes(Integer.valueOf(participantAndShip.get("tempsCompense"+i))), row, 6);
					row ++;
				}
			}
			for (int i= 0; i < participantAbandon.size()/4; i++) {
				views.getCla().setTableClassement("Abandon", row, 0);
				views.getCla().setTableClassement(String.valueOf(participantAbandon.get("idPart"+i)), row, 1);
				views.getCla().setTableClassement(participantAbandon.get("lastName"+i), row, 2);
				views.getCla().setTableClassement(participantAbandon.get("firstName"+i), row, 3);
				views.getCla().setTableClassement(participantAbandon.get("nameShip"+i), row, 4);
				views.getCla().setTableClassement("Abandon", row, 5);
				views.getCla().setTableClassement("Abanbon", row, 6);
				row ++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public void selRegateToModif() {
    	this.refreshManagerInfo();
    	clearJtable();
    	
    	String selRegate = (String) views.getAr().getCboSelRegateToModif().getSelectedItem();
		String idRegate = "";

		int index = 0;
		while (selRegate.charAt(index) != ' ') {
			idRegate += selRegate.charAt(index);
			index ++;
		}
		Regate maRegate = manager.getAllRegates().get(0);
		for (int i=0; i <=  manager.getAllRegates().size(); i++) {
			if (manager.getAllRegates().get(i).getIdRegate() == Integer.parseInt(idRegate)) {
				maRegate = manager.getAllRegates().get(i);
				break;
			}

		}
    	Hashtable<String, String> participantAndShip;
    
    	try {
    		participantAndShip = RequestBdd.getParticipantsInscrit(maRegate.getIdRegate());
    		for (int i= 0; i < participantAndShip.size()/9; i++) {
    			views.getAr().setTableParticipants(String.valueOf(participantAndShip.get("idPart"+i)), i, 0);
    			views.getAr().setTableParticipants(participantAndShip.get("lastName"+i), i, 1);
    			views.getAr().setTableParticipants(participantAndShip.get("firstName"+i), i, 2);
    			views.getAr().setTableParticipants(String.valueOf(participantAndShip.get("idShip"+i)), i, 3);
    			views.getAr().setTableParticipants(participantAndShip.get("nameShip"+i), i, 4);
				views.getAr().setTableParticipants(String.valueOf(participantAndShip.get("categoryShip"+i)), i, 5);
				views.getAr().setTableParticipants(String.valueOf(participantAndShip.get("ratingShip"+i)), i, 6);
    			
    		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	views.getAr().setIdRegate(String.valueOf(maRegate.getIdRegate()));
    	views.getAr().setNameRegate(maRegate.getNameRegate());
    	views.getAr().setPlaceDeparture(maRegate.getStartPoint());
    	views.getAr().setPlaceArrival(maRegate.getEndPoint());
    	views.getAr().setDistance(String.valueOf(maRegate.getDistance()));
    	views.getAr().setDate((new java.util.Date(maRegate.getDateRegate().getTime())));
    	
    }
    
    public void selRegate() {
    	clearJtableRunRegate();

		String selRegate = (String) views.getLr().getCboSelRegate().getSelectedItem();
		String idRegate = "";

		int index = 0;
		while (selRegate.charAt(index) != ' ') {
			idRegate += selRegate.charAt(index);
			index ++;
		}
		Regate maRegate = manager.getAllRegates().get(0);
		for (int i=0; i <=  manager.getAllRegates().size(); i++) {
			if (manager.getAllRegates().get(i).getIdRegate() == Integer.parseInt(idRegate)) {
				maRegate = manager.getAllRegates().get(i);
				break;
			}

		}

    	Hashtable<String, String> participantAndShip;
    
    	try {
    		participantAndShip = RequestBdd.getParticipantsInscrit(maRegate.getIdRegate());
    		for (int i= 0; i < participantAndShip.size()/9; i++) {
    			views.getLr().setTableParticipants(String.valueOf(participantAndShip.get("idPart"+i)), i, 0);
    			views.getLr().setTableParticipants(participantAndShip.get("lastName"+i), i, 1);
    			views.getLr().setTableParticipants(participantAndShip.get("nameShip"+i), i, 2);
    			views.getLr().setTableParticipants(participantAndShip.get("ratingShip"+i), i, 3);
    		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	views.getLr().setIdRegate(String.valueOf(maRegate.getIdRegate()));
    	views.getLr().setNameRegate(maRegate.getNameRegate());
    	views.getLr().setPlaceDeparture(maRegate.getStartPoint());
    	views.getLr().setPlaceArrival(maRegate.getEndPoint());
    	views.getLr().setDistance(String.valueOf(maRegate.getDistance()));
    	views.getLr().setDate(maRegate.getDateRegate());
    }
    
    public void clearJtable() {
    	
		for (int i = 0; i<20; i++) {
			if (views.getAr().getTableParticipants().getValueAt(i, 0) != null) {
				for(int j = 0; j < 7; j++ ) {
					views.getAr().setTableParticipants(null, i, j);					
				}
			}
    	}
    }
    
    public void clearTableClassement(){
    	for (int i = 0; i<20; i++) {
			if (views.getCla().getTableClassement().getValueAt(i, 0) != null) {
				for(int j = 0; j < 7; j++ ) {
					views.getCla().setTableClassement(null, i, j);					
				}
			}
    	}
    }
    
    public void clearJtableRunRegate() {
        for (int i = 0; i<20; i++) {
        	views.getLr().setTableParticipants(null, i, 0);
        	views.getLr().setTableParticipants(null, i, 1);
        	views.getLr().setTableParticipants(null, i, 2);
        	views.getLr().setTableParticipants(null, i, 3);
        	views.getLr().setTableParticipants(null, i, 6);
        }
    }
    
    public void refreshManagerInfo() {
        try {
            manager.setAllParticipants(RequestBdd.getListParticipant());
            manager.setAllRegates(RequestBdd.getListRegate());
            manager.setAllShip(RequestBdd.getListShip());
            manager.setRunRegates(RequestBdd.getListRunRegate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String convertMinutes(int duree) {
    	int heures= duree / 3600;
    	int minutes= (duree % 3600) / 60;
    	int secondes= (((duree % 3600) % 60));
    	String time ="";
    	
    	if(heures < 10) {
    		time = "0" + heures +":" ;
    	}else {
    		time = heures +":";
    	}
    	if(minutes < 10) {
    		time += "0"+minutes +":";
    	}else {
    		time += minutes+ ":";
    	}
    	if (secondes < 10) {
    		time+= "0"+secondes;
    	}else {
    		time += secondes;
    	}
    	
    	return time;
    }

    public void completeTabLineLr(Boolean abandon, int row) {
		if ((chrono.isRunning()) && (views.getLr().getTableParticipants().getValueAt(row, 6) == null) &&
				views.getLr().getTableParticipants().getValueAt(row, 0) != null)
		{
			if (abandon)
			{
				views.getLr().getTableParticipants().setValueAt("Abandon",row,6);

			}else
			{
				views.getLr().getTableParticipants().setValueAt(new SimpleDateFormat("HH:mm:ss").format(
						chrono.getTime()*1000- 3.6 * Math.pow(10,6)),row,6);
			}
		}else if  (views.getLr().getTableParticipants().getValueAt(row, 6) != null)
		{
			JOptionPane.showMessageDialog(null, "Ce participant est déjà arrivé ou a abandoné", "Erreur", JOptionPane.ERROR_MESSAGE);
		}else if  (!chrono.isRunning())
		{
			JOptionPane.showMessageDialog(null, "Le chronomètre n'est pas lancé", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}
}
