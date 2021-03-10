package models;
public class Features {

	private Double numb_of_urls;
	private Double numb_of_hashtags;
	private Double numb_of_personal_pronouns;
	private Double numb_of_present_tenses;
	public Features() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Features(Double numb_of_urls, Double numb_of_hashtags, Double numb_of_personal_pronouns, Double numb_of_present_tenses) {
		super();
		this.numb_of_urls = numb_of_urls;
		this.numb_of_hashtags = numb_of_hashtags;
		this.numb_of_personal_pronouns = numb_of_personal_pronouns;
		this.numb_of_present_tenses = numb_of_present_tenses;
	}
	public Double getNumb_of_urls() {
		return numb_of_urls;
	}
	public void setNumb_of_urls(Double numb_of_urls) {
		this.numb_of_urls = numb_of_urls;
	}
	public Double getNumb_of_hashtags() {
		return numb_of_hashtags;
	}
	public void setNumb_of_hashtags(Double numb_of_hashtags) {
		this.numb_of_hashtags = numb_of_hashtags;
	}
	public Double getNumb_of_personal_pronouns() {
		return numb_of_personal_pronouns;
	}
	public void setNumb_of_personal_pronouns(Double numb_of_personal_pronouns) {
		this.numb_of_personal_pronouns = numb_of_personal_pronouns;
	}
	public Double getNumb_of_present_tenses() {
		return numb_of_present_tenses;
	}
	public void setNumb_of_present_tenses(Double numb_of_present_tenses) {
		this.numb_of_present_tenses = numb_of_present_tenses;
	}

	
}
