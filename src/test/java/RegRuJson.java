import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.regru.common.dto.AddAndDeleteDomainResponse;

public class RegRuJson {
	public static void main(String[] args) throws JsonProcessingException {
		String jsonStr = """
				{
				   "answer" : {
				      "domains" : [
				         {
				            "dname" : "test1.test2",
				            "result" : "success",
				            "service_id" : "00000"
				         }
				      ]
				   },
				   "charset" : "utf-8",
				   "messagestore" : null,
				   "result" : "success"
				}
				""";
		AddAndDeleteDomainResponse value = new ObjectMapper().readValue(jsonStr, AddAndDeleteDomainResponse.class);
		System.out.println(value);
	}
}