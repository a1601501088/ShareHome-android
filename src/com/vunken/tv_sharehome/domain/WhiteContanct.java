package com.vunken.tv_sharehome.domain;

public class WhiteContanct {

		
		private String username;
		private String moblie;
		public WhiteContanct(String username, String moblie) {
		
			this.username = username;
			this.moblie = moblie;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getMoblie() {
			return moblie;
		}
		public void setMoblie(String moblie) {
			this.moblie = moblie;
		}
		@Override
		public String toString() {
			return "WhiteContanct [username=" + username + ", moblie=" + moblie
					+ "]";
		};
		
		
}
