package fr.regate.project.model;


import java.util.ArrayList;
import java.util.Hashtable;
import java.sql.*;

/**
 * 
 * @author thomas
 *
 */
public class RequestBdd {


	private static ArrayList<Participant> listParticipant = new ArrayList<Participant>();
	private static ArrayList<Ship> listShip = new ArrayList<Ship>();
	
	
	/**
	 * Récupère la liste des participants inscris dans la bdd
	 * 
	 * @return liste de tout les participants
	 * @throws SQLException
	 */
	public static ArrayList<Participant> getListParticipant() throws SQLException{
		listParticipant.clear();
		String requestGetAllParticipant = "SELECT * FROM participant";
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestGetAllParticipant));
		while(BddConnection.getRs().next()) {
			Participant participant = new Participant(BddConnection.getRs().getInt(1), BddConnection.getRs().getString(2),
					BddConnection.getRs().getString(3), BddConnection.getRs().getString(4), BddConnection.getRs().getString(5));
			
			listParticipant.add(participant);
		}
		return listParticipant;
	}
/*
	public static Participant getParticipant(int idParticipant) throws SQLException {
		String requestParticipant = "Select * FROM participant where P_ID = "+ idParticipant;
		Participant participant = new Participant();
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestParticipant));
		while(BddConnection.getRs().next()) {
			participant = new Participant(BddConnection.getRs().getInt(1), BddConnection.getRs().getString(2),
					BddConnection.getRs().getString(3), BddConnection.getRs().getString(4), BddConnection.getRs().getString(5));
			
			
		}
		return participant;
		
	}
*/
	/**
	 * Récupère la lise des régates dans la bdd
	 * 
	 * @return liste des différentes régates
	 * @throws SQLException
	 */
	public static ArrayList<Regate> getListRegate() throws SQLException {
		ArrayList<Regate> listRegate = new ArrayList<Regate>();

		String requestGetAllRegate = "SELECT * FROM regate";
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestGetAllRegate));
		while(BddConnection.getRs().next()) {
			Regate regate = new Regate(BddConnection.getRs().getInt(1), BddConnection.getRs().getString(2), BddConnection.getRs().getDate(3), BddConnection.getRs().getString(4),
					BddConnection.getRs().getString(5), BddConnection.getRs().getInt(6), BddConnection.getRs().getInt(7));
			
			listRegate.add(regate);
		}
		return listRegate;
	}
	
	/**
	 * Récupère la liste des bateaux dans la bdd
	 * 
	 * @return ArrayList<Ship> 
	 * @throws SQLException
	 */
	public static ArrayList<Ship> getListShip() throws SQLException {
		String requestGetAllShip = "SELECT * FROM ship";
		listShip.clear();
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestGetAllShip));
		while(BddConnection.getRs().next()) {
			Ship ship = new Ship(BddConnection.getRs().getInt(1), BddConnection.getRs().getString(2), BddConnection.getRs().getInt(3), BddConnection.getRs().getInt(4));
			
			listShip.add(ship);
		}
		return listShip;
	}
	

	public static ArrayList<Regate> getListRunRegate() throws SQLException {
		String requestGetRunRegate = "SELECT * FROM regate WHERE R_STATUS = 1;";
		ArrayList<Regate> listRunRegate = new ArrayList<Regate>();
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestGetRunRegate));
		while(BddConnection.getRs().next()) {
			Regate regate = new Regate(BddConnection.getRs().getInt(1), BddConnection.getRs().getString(2), BddConnection.getRs().getDate(3), BddConnection.getRs().getString(4),
					BddConnection.getRs().getString(5), BddConnection.getRs().getInt(6), BddConnection.getRs().getInt(7));
			
			listRunRegate.add(regate);
		}
		return listRunRegate;
	}
	
	public static Hashtable<String, String> getParticipantsInscrit(int idRegate) throws SQLException {
		String requestGetALLInscription = "SELECT * FROM participant AS p INNER JOIN ship AS s INNER JOIN inscription AS i ON i.P_ID = p.P_ID AND i.S_ID = s.S_ID AND i.R_ID = " + idRegate +" ORDER BY I.I_REALTIME";
		BddConnection.setRs(BddConnection.getSt().executeQuery(requestGetALLInscription));
		Hashtable<String, String> ParticipantAndShip = new Hashtable<String, String>();
		int i = 0;
		
		while(BddConnection.getRs().next()) {
			String idParticipant = String.valueOf(BddConnection.getRs().getInt(1)); 
			String lastNamePart = BddConnection.getRs().getString(2);
			String firstNamePart = BddConnection.getRs().getString(3);
			String idShip = String.valueOf(BddConnection.getRs().getInt(6));
			String nameShip = BddConnection.getRs().getString(7);
			String categoryShip = String.valueOf(BddConnection.getRs().getInt(8));
			String RatingShip = String.valueOf(BddConnection.getRs().getInt(9));
			String tempsReel = String.valueOf(BddConnection.getRs().getInt(17));
			String tempsCompense = String.valueOf(BddConnection.getRs().getInt(16));
			
			ParticipantAndShip.put("idPart"+ i, idParticipant);
			ParticipantAndShip.put("lastName"+ i, lastNamePart);
			ParticipantAndShip.put("firstName"+ i, firstNamePart);
			ParticipantAndShip.put("idShip"+ i, idShip);
			ParticipantAndShip.put("nameShip"+ i, nameShip);
			ParticipantAndShip.put("categoryShip"+ i, categoryShip);
			ParticipantAndShip.put("ratingShip"+ i, RatingShip);
			ParticipantAndShip.put("tempsReel"+ i, tempsReel);
			ParticipantAndShip.put("tempsCompense"+i, tempsCompense);
			i++;
		}
		return ParticipantAndShip;
	}
	
	/**
	 * Ajout d'un participant dans la base de données 
	 * 
	 * @param name
	 * @param firstName
	 * @param phone
	 * @param email
	 * @throws SQLException
	 */
	public static void reqAddParticipant(String name, String firstName, String phone, String email) throws SQLException {
		PreparedStatement prepare = BddConnection.getCon().prepareStatement("INSERT INTO `eole`.`participant` (`P_NAME`, `P_FIRSTNAME`, `P_PHONE`, `P_EMAIL`)"
				+ "VALUES (?, ?, ?, ?); ");
		prepare.setString (1, name);
	    prepare.setString (2, firstName);
	    prepare.setString (3, phone);
	    prepare.setString (4, email);
		
	    prepare.executeUpdate();
	    System.out.println("request add participant send !");
	}
	
	
	/**
	 * Ajout d'une regate dans la base de données
	 * 
	 * @param nameRegate
	 * @param dateRegate
	 * @param startPlace
	 * @param endPlace
	 * @param distance
	 * @param status
	 * @throws SQLException
	 */
	public static void reqAddRegate(String nameRegate, Date dateRegate, String startPlace, String endPlace, int distance, int status) throws SQLException {
		PreparedStatement prepare = BddConnection.getCon().prepareStatement("INSERT INTO `eole`.`regate` (`R_NAME`,`R_DATE`, `R_STARTPLACE`, `R_ENDPLACE`, `R_DISTANCE`, `R_STATUS`)"
				+ "VALUES (?, ?, ?, ?, ?,?); ");
		prepare.setString (1, nameRegate);
	    prepare.setDate (2, dateRegate);
	    prepare.setString (3, startPlace);
	    prepare.setString (4, endPlace);
	    prepare.setInt (5, distance);
	    prepare.setInt (6, status);
		
	    prepare.executeUpdate();
	    System.out.println("request add régate send !");
	}
	
	/**
	 * Ajout d'un bateau dans la base de données 
	 * 
	 * @param nameShip
	 * @param dateRegate
	 * @param Category
	 * @param rating
	 * @throws SQLException
	 */
	public static void reqAddShip(String nameShip, int Category, int rating) throws SQLException {
		PreparedStatement prepare = BddConnection.getCon().prepareStatement("INSERT INTO `eole`.`ship` (`S_NAME`, `S_CATEGORY`, `S_RATING`)"
				+ "VALUES (?, ?, ?); ");
		prepare.setString (1, nameShip);
	    prepare.setInt (2, Category);
	    prepare.setInt (3, rating);
		
	    prepare.executeUpdate();
	    System.out.println("request add ship send !");
	}
	
	/**
	 * Request for link participant/ship/regate in classement table 
	 * 
	 * @param idParticipant
	 * @param idRegate
	 * @param idShip
	 * @param date
	 * @throws SQLException
	 */
	public static void reqLinkPartToRegate(int idParticipant, int idRegate, int idShip, Date date) throws SQLException{
		 PreparedStatement prepare = BddConnection.getCon().prepareStatement("INSERT INTO inscription (S_ID, P_ID, R_ID) SELECT ?, ?, ? FROM dual WHERE NOT EXISTS (SELECT 1 FROM inscription WHERE P_ID = ? AND R_ID = ?);");
		 prepare.setInt(1, idShip);
		 prepare.setInt(2, idParticipant);
		 prepare.setInt (3, idRegate);
		 prepare.setInt (4, idParticipant);
		 prepare.setInt (5, idRegate);
	     prepare.executeUpdate();
	     System.out.println("Request inscription send !");
	}
	
	
	public static void reqUpdateRegate(int idRegate, String nameRegate, String startPlace, String endPlace, int distance, int status) throws SQLException {
		PreparedStatement prepare =BddConnection.getCon().prepareStatement(" UPDATE `regate` SET `R_NAME` = ?, `R_STARTPLACE` = ?, `R_ENDPLACE` = ?, `R_DISTANCE` = ?, `R_STATUS` = ? WHERE `regate`.`R_ID` = ?;");
		prepare.setString(1, nameRegate);
		prepare.setString(2, startPlace);
		prepare.setString(3, endPlace);
		prepare.setInt(4, distance);
		prepare.setInt(5, status);
		prepare.setInt(6, idRegate);
		
		prepare.executeUpdate();
		System.out.println("Request Update Regate send !");
		
	}
	
	public static void reqDeletRegate(int idRegate) throws SQLException {
		PreparedStatement deletInscription = BddConnection.getCon().prepareStatement("DELETE FROM `inscription` WHERE `inscription`.`R_ID` = ? ");
		deletInscription.setInt(1, idRegate);
		
		deletInscription.executeUpdate();
		System.out.println("inscription supprimé");
		
		PreparedStatement prepare = BddConnection.getCon().prepareStatement("DELETE FROM `regate` WHERE `regate`.`R_ID` = ? ");
		prepare.setInt(1, idRegate);
		
		prepare.executeUpdate();
		System.out.println("request Delet Send !");
	}
	
	public static void reqUpdateTimePart(int idParticipant, int idRegate, long time, double tempsCompense) throws SQLException {
		PreparedStatement updateParticipant = BddConnection.getCon().prepareStatement("UPDATE `inscription` SET `I_REALTIME` = ?, `I_COMPTIME` = ? WHERE P_ID = ? AND R_ID = ?;");
		updateParticipant.setDouble(1, time/1000 + 3600);
		updateParticipant.setDouble(2, tempsCompense);
		updateParticipant.setInt(3, idParticipant);
		updateParticipant.setInt(4, idRegate);
		
		updateParticipant.executeUpdate();
		System.out.println("Participant update");
		
	}
	public static void reqUpdateRegateFinish(int idRegate) throws SQLException {
		PreparedStatement updateRegate = BddConnection.getCon().prepareStatement("UPDATE `regate` SET `R_STATUS` = ? WHERE `regate`.`R_ID` = ?;");
		updateRegate.setInt(1, 1);
		updateRegate.setInt(2, idRegate);
		
		updateRegate.executeUpdate();
		System.out.println("Regate was finished");
	}
	
	
	
	
}
