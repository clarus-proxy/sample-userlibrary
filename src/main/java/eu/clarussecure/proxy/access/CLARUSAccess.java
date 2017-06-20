package eu.clarussecure.proxy.access;

public interface CLARUSAccess {
	public boolean authenticate(String username, String password);
	
	public boolean identify(String username);
	
	// For a simpler implementation, this method was not implemented
	//public boolean autheticate(org.apache.http.auth.UsernamePasswordCredentials cr);
}
