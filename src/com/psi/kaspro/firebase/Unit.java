package com.psi.kaspro.firebase;

public class Unit {

	public static final String MESSAGE_KEY = "message";
			
	public static void main(String[] args) {
		FirebaseMessage message = new FirebaseMessage();
		message.setTopic("SamTopic");
		message.setTitle("Kaspro Test");
		message.setMessage("Testing Notification Only");
		message.send();
	}

}
