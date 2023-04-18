
package folder;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.EnumType;
import acme.entities.Tutorial;
import acme.entities.TutorialSession;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Assistant;

@Service
public class AssistantTutorialSessionCreateService extends AbstractService<Assistant, TutorialSession> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected AssistantTutorialSessionRepository repository;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		boolean status;
		status = super.getRequest().hasData("masterId", int.class);
		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		boolean status;
		int masterId;
		Tutorial tutorial;

		masterId = super.getRequest().getData("masterId", int.class);

		tutorial = this.repository.findOneTutorialById(masterId);
		status = tutorial != null && //
			tutorial.getNotPublished() && //
			super.getRequest().getPrincipal().hasRole(tutorial.getAssistant());
		super.getResponse().setAuthorised(status);
	}

	@Override
	public void load() {
		TutorialSession object;
		Tutorial tutorial;
		int masterId;

		masterId = super.getRequest().getData("masterId", int.class);
		tutorial = this.repository.findOneTutorialById(masterId);

		object = new TutorialSession();
		object.setTutorial(tutorial);

		super.getBuffer().setData(object);

	}

	@Override
	public void bind(final TutorialSession object) {
		assert object != null;

		super.bind(object, "title", "abstraction", "sessionType", "startDate", "endDate", "link");
	}

	@Override
	public void validate(final TutorialSession object) {
		assert object != null;

		if (!(super.getBuffer().getErrors().hasErrors("startDate") || super.getBuffer().getErrors().hasErrors("endDate"))) {
			final boolean endDateIsAfter = object.getEndDate().after(object.getStartDate());
			final Date currentDate = MomentHelper.getCurrentMoment();
			final Long durationSinceCurrentDate = MomentHelper.computeDuration(currentDate, object.getStartDate()).toDays();
			final boolean startDateIsOneDayAhead = durationSinceCurrentDate.doubleValue() >= 1.;
			super.state(endDateIsAfter, "endDate", "assistant.session.form.error.endDate");
			super.state(startDateIsOneDayAhead, "startDate", "assistant.session.form.error.startDate");
			super.state(object.getDurationInHours() >= 1. && // 
				object.getDurationInHours() <= 5., "endDate", "assistant.session.form.error.period");

		}
	}

	@Override
	public void perform(final TutorialSession object) {
		assert object != null;

		this.repository.save(object);
	}

	@Override
	public void unbind(final TutorialSession object) {
		assert object != null;

		Tuple tuple;
		SelectChoices choices;

		choices = SelectChoices.from(EnumType.class, object.getSessionType());

		tuple = super.unbind(object, "title", "abstractm", "sessionType", "startDate", "endDate", "link");
		tuple.put("masterId", super.getRequest().getData("masterId", int.class));
		tuple.put("types", choices);
		tuple.put("notPublished", object.getTutorial().getNotPublished());

		super.getResponse().setData(tuple);
	}

}
