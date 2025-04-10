package art.aelaort.payments_reminders.keyboard;

import art.aelaort.telegram.callback.CallbackType;
import art.aelaort.telegram.callback.models.RemindCallback;
import art.aelaort.telegram.callback.models.RemindDaysCallback;
import art.aelaort.telegram.callback.models.SomeCallback;
import art.aelaort.telegram.entity.RemindToSend;
import org.springframework.stereotype.Component;

@Component
public class CallbackTypeMapper {
	public RemindCallback map(CallbackType callbackType, RemindToSend remind) {
		if (callbackType == null && remind == null) {
			return null;
		}
		int typeId = callbackType != null ? callbackType.getId() : 0;
		long remindId = remind != null ? remind.getId() : 0L;
		return new RemindCallback(typeId, remindId);
	}

	public RemindDaysCallback map(CallbackType callbackType, RemindToSend remind, int numberDays) {
		if (callbackType == null && remind == null) {
			return null;
		}
		int typeId = callbackType != null ? callbackType.getId() : 0;
		long remindId = remind != null ? remind.getId() : 0L;
		return new RemindDaysCallback(typeId, remindId, numberDays);
	}

	public SomeCallback map(CallbackType callbackType) {
		if (callbackType == null) {
			return null;
		}
		return new SomeCallback(callbackType.getId());
	}
}
