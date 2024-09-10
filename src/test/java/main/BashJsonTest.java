package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.bash.model.Quote;

import java.io.IOException;

public class BashJsonTest {
	public static void main(String[] args) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		Quote[] quotes = objectMapper.readValue(data(), Quote[].class);
		System.out.println(quotes);
	}

	private static String data() {
		return """
				[
				  {
				    "id": 7,
				    "date": "30-08-2004T15:27",
				    "rating": 16731,
				    "text": "&lt;Hellcat&gt; Настояший программер пьёт один раз в день - с утра и до вечера"
				  },
				  {
				    "id": 6,
				    "date": "30-08-2004T15:26",
				    "rating": 26262,
				    "text": "&lt;Ohtori_Akio&gt; о чём ни спроси - все обычно советуют сменить операционку, потом железо, потом страну пребывания, ориентацию, всё, что угодно... вместо того, чтобы подсказать нужную настройку в софтине."
				  },
				  {
				    "id": 5,
				    "date": "30-08-2004T15:26",
				    "rating": 7261,
				    "text": "&lt;Ohtori_Akio&gt; мы - как разработчики - живём с субейзом под одбц. <br>&lt;Ohtori_Akio&gt; лучше бы мы жили в пещере с гоблинами."
				  }
				]""";
	}
}
