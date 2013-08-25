package freenet.darknetconnector.DarknetAppConnector;
/**
 * A static framework to hold properties of home node assuming each mobile 
 * can be associated with only one home freenet  at a time. 
 * @author Illutionist
 */
public class HomeNode {
	
	private static String name;
	public HomeNode(String nodeName) {
		name = nodeName;
	}
	
	public static String getName() {
		return name;
	}
	public static void setName(String nodeName) {
		name = nodeName;
	}
}
