import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import de.ulb.marc2geo.core.GlobalSettings;


public class ParseCoordinates {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ArrayList<String> coordinates = new ArrayList<String>();

		coordinates.add("(E 013 20--E 014 20/N 051 30--N 051 00)."); //errado
		coordinates.add("E 012 40 -E 014 00 /N 051 12 -N 050 24");

		double east = 0.0;
		double west = 0.0;
		double north = 0.0;
		double south = 0.0;

		for (int i = 0; i < coordinates.size(); i++) {

			String coordinateString;

			coordinateString = coordinates.get(i).replace("--", " -");			
			coordinateString = coordinateString.toUpperCase();
			coordinateString = coordinateString.replace("/", " /");
			coordinateString = coordinateString.replace("/", "");
			coordinateString = coordinateString.replace("(", "");
			coordinateString = coordinateString.replace(")", "");
			coordinateString = coordinateString.replace("[", "");
			coordinateString = coordinateString.replace("]", "");
			coordinateString = coordinateString.replace(".", "");
			coordinateString = coordinateString.replace("  ", " ");

			String[] array = coordinateString.trim().split(" ");

			for (int j = 0; j < array.length; j++) {

				if (array[j].equals("W")) {			

					west = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
					west *= -1;						

				} else if (array[j].equals("-W")) {

					east = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
					east *= -1;

				} else if (array[j].equals("S")) {

					south = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
					south *= -1;

				} else if (array[j].equals("-S")) {

					north = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));
					north *= -1;

				} else if (array[j].equals("E")) {

					east = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));

				} else if (array[j].equals("-E")) {

					west = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));

				} else if (array[j].equals("N")) {

					north = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));

				} else if (array[j].equals("-N")) {

					south = (Double.parseDouble(array[j+1]) + (Double.parseDouble(array[j+2])/60));

				}

			}


			west = round(west,2); 
			east = round(east,2);
			south = round(south,2);
			north = round(north,2);

			String	wkt = "<" + GlobalSettings.getCRS() + ">POLYGON((" + west + " " + north + ", " + east + " " + north + ", " + east + " " + south + ", " + west + " " + south + ", " + west + " " + north + "))";
			System.out.println(wkt);

		}
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}

