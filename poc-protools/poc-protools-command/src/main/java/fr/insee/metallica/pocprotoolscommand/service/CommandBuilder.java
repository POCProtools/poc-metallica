package fr.insee.metallica.pocprotoolscommand.service;

import java.time.LocalDateTime;

import fr.insee.metallica.pocprotoolscommand.domain.Command;
import fr.insee.metallica.pocprotoolscommand.domain.Command.Status;
import fr.insee.metallica.pocprotoolscommand.service.CommandEventListener.Type;

public class CommandBuilder extends CommandBuilderBase<CommandBuilder> {
	private AsyncResultFetcherBuilder asyncResult;

	public CommandBuilder(CommandService commandService, String type) {
		super(commandService, type);
	}
	
	@Override
	protected CommandBuilder getThis() {
		return this;
	}
	
	public AsyncResultFetcherBuilder asyncResult(String type) {
		return this.asyncResult = new AsyncResultFetcherBuilder(commandService, this, type);
	}
	
	public Command saveAndSend() {
		command.setStatus(Status.Pending);
		if (command.getNextScheduledTime() == null)
			command.setNextScheduledTime(LocalDateTime.now());
		
		return this.commandService.transactionTemplate.execute((status) -> {
			if (asyncResult != null) {
				asyncResult.command.setStatus(Status.WaitingToBeScheduled);
				this.commandService.commandRepository.save(asyncResult.command);
				command.setResultFetcher(asyncResult.command);
			}
					
			command = this.commandService.commandRepository.save(command);
			this.commandService.publish(Type.Added,command, null);
			return command;
		});
	}

	public Command saveAndSendWithLimit(int limit, String limitKey) {
		command.setStatus(Status.Pending);
		command.setConcurrencyLimit(limit);
		command.setLimitKey(limitKey);
		if (command.getNextScheduledTime() == null)
			command.setNextScheduledTime(LocalDateTime.now());
		
		return this.commandService.transactionTemplate.execute((status) -> {
			command = this.commandService.commandRepository.save(command);
			this.commandService.publish(Type.Added,command, null);
			return command;
		});
	}

	public Command saveNoSend() {
		command.setStatus(Status.Pending);
		if (command.getNextScheduledTime() == null)
			command.setNextScheduledTime(LocalDateTime.now());
		return this.commandService.transactionTemplate.execute((status) -> {
			command = this.commandService.commandRepository.save(command);
			return command;
		});
	}
}