
public class Crypto {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int key = 810;

		String text = "Würde der Literaturnobelpreisträger eine Figur seines Romans sein, würde er noch leben. Aber würde er das wollen? In einer Welt leben, in der keiner stirbt. Scheint dies dem Einzelnen zu Anfang als Segen, so wird der Stillstand zur Last, genauso wie die zu pflegenden Verwandten. Die Aussicht auf Unsterblichkeit wird zur Qual. Das Chaos kommt todsicher. In diesem Buch wendet sich Saramago philosophisch dem Tod, seinen Auswirkungen und dem Umgang der Menschen mit diesem zu. Nicht nur das es genial erscheint, den Tod weiblich werden zu lassen, auch schafft es Saramago dem Ende eine Wendung zu geben, die den Leser überrascht.";

		String encryptedText = encode(text,key);
		String decryptedText = decode(encryptedText,key);

		System.out.println("Decoded Text: "+decryptedText);
		System.out.println("\nCheck: "+text.equals(decryptedText));

		text = "abcde";
		encryptedText = encode(text,key);
		decryptedText = decode(encryptedText,key);

		System.out.println("Decoded Text: "+decryptedText);
		System.out.println("\nCheck: "+text.equals(decryptedText));


	}


	private static String decode(String text, int key){


		System.out.println("\n ### Decode ###\n");

		char[] encrypted = new char[text.length()];		

		for (int i = 0; i < text.length(); i++) {

			encrypted[i] = (char) (text.charAt(i)-key);

		}

		text = String.valueOf(encrypted);


		int division = 0;

		division = text.length()/2;

		String second = text.substring(0,division);
		String first = text.substring(division,text.length());

		first = shuffle(first);	
		first = invert(first);	

		second = shuffle(second);
		second = invert(second);

		return first + second;

	}

	private static String encode(String text, int key){

		System.out.println("\n ### Encode ###\n");

		int division = 0;

		if(text.length()%2==0){

			division = (text.length()/2);

		} else {

			division = (text.length()/2)+1;

		}

		String first = "";
		String second = "";
		first = text.substring(0,division);
		second = text.substring(division,text.length());

		first = invert(first);	
		first = shuffle(first);	

		second = invert(second);
		second = shuffle(second);	

		String finalText = second + first;

		char[] encrypted = new char[finalText.length()];		
		for (int i = 0; i < finalText.length(); i++) {

			encrypted[i] = (char) (finalText.charAt(i)+key);

		}

		System.out.println("Encoded text: " + String.valueOf(encrypted));
	
		return String.valueOf(encrypted);

	}


	private static String invert(String text){

		String result = "";

		for (int i = text.length()-1; i >= 0; i--) {

			result = result + text.charAt(i);

		}

		return result;
	}

	private static String shuffle(String text){

		String result = "";

		for (int i = 0; i < text.length(); i++) {

			if(i%2 == 0){

				if (i+1!=text.length()){

					result = result + text.charAt(i+1);			   

				}

				result = result + text.charAt(i);
			}

		}

		return result;
	}
}
