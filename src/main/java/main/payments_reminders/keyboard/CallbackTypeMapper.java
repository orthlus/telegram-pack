package main.payments_reminders.keyboard;

import art.aelaort.telegram.callback.CallbackType;
import art.aelaort.telegram.callback.models.RemindCallback;
import art.aelaort.telegram.callback.models.RemindDaysCallback;
import art.aelaort.telegram.callback.models.SomeCallback;
import art.aelaort.telegram.entity.RemindToSend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
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
