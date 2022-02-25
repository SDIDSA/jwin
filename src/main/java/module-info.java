module jwin {
	requires org.json;
	requires javafx.base;
	requires java.desktop;
	requires javafx.swing;
	requires javafx.graphics;
	requires javafx.controls;
	requires com.sun.jna.platform;
	requires com.sun.jna;
	
	opens org.luke.jwin.app to javafx.graphics;
	
	exports org.luke.jwin;
}