package id.co.hanoman.tapcash.model;

import javax.validation.constraints.NotNull;

public class TrxPembayaran {
	@NotNull
	String tapcashNum;

	@NotNull
	String amount;
	
	@NotNull
	String accountNum;

	public String getTapcashNum() {
		return tapcashNum;
	}

	public void setTapcashNum(String tapcashNum) {
		this.tapcashNum = tapcashNum;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAccountNum() {
		return accountNum;
	}

	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}
}
