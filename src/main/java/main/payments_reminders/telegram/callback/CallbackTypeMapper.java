package main.payments_reminders.telegram.callback;

import main.payments_reminders.entity.RemindToSend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CallbackTypeMapper {
	@Mapping(target = "typeId", source = "callbackType.id")
	@Mapping(target = "remindId", source = "remind.id")
	RemindCallback map(CallbackType callbackType, RemindToSend remind);

	@Mapping(target = "typeId", source = "callbackType.id")
	@Mapping(target = "remindId", source = "remind.id")
	@Mapping(target = "days", source = "numberDays")
	RemindDaysCallback map(CallbackType callbackType, RemindToSend remind, int numberDays);

	@Mapping(target = "typeId", source = "id")
	SomeCallback map(CallbackType callbackType);
}
