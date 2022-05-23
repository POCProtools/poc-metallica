package fr.insee.metallica.pocprotools.unite;

import java.util.Random;

public class UniteEnquete {
	private String nom;
	private String prenom;
	private String adresse;
	
	public UniteEnquete(String nom, String prenom, String adresse) {
		this.nom = nom;
		this.prenom = prenom;
		this.adresse = adresse;
	}
	
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public String getPrenom() {
		return prenom;
	}
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	public String getAdresse() {
		return adresse;
	}
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public static UniteEnquete random() {
		var random = new Random().nextDouble();
		return new UniteEnquete("nom-" + random, "prenom-" + random, "adresse-" + random);
	}
}
