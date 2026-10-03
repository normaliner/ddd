package microarch.delivery.adapters.in.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.AssignOrderCommand;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssignOrdersJob implements Job {

    private final AssignOrderCommandHandler assignOrderCommandHandler;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.debug("Assign orders job started");

        var commandResult = AssignOrderCommand.create();

        if (commandResult.isFailure()) {
            log.warn("Failed to create assign order command: {}", commandResult.getError());
            throw new JobExecutionException(String.valueOf(commandResult.getError()));
        }

        var handlerResult = assignOrderCommandHandler.handle(commandResult.getValue());

        if (handlerResult.isFailure()) {
            var error = handlerResult.getError();
            switch (error.getCode()) {
            case "assign.no.created.order", "dispatch.couriers.empty", "dispatch.no.available.courier" -> {
                log.debug("Assign orders job finished without assignment: {}", error);
                return;
            }
            default -> throw new JobExecutionException(String.valueOf(error));
            }
        }
        log.debug("Assign orders job completed successfully");
    }

}
