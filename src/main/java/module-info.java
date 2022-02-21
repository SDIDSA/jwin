module jwin {
	requires javafx.graphics;
	requires org.json;
	requires java.desktop;
	requires javafx.swing;
	requires javafx.controls;
	
	opens org.luke.jwin.app to javafx.graphics;
	
	exports org.luke.jwin;
}