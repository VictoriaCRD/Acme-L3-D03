
package acme.features.auditor.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.Audit;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Auditor;

@Service
public class AuditorAuditShowService extends AbstractService<Auditor, Audit> {

	@Autowired
	protected AuditorAuditRepository repository;


	@Override
	public void check() {
		boolean status;

		status = super.getRequest().hasData("id", int.class);
		super.getResponse().setChecked(status);

	}

	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Audit object;
		int id;

		id = super.getRequest().getData("id", int.class);
		object = this.repository.findOneAuditById(id);

		super.getBuffer().setData(object);
	}

	@Override
	public void unbind(final Audit object) {
		assert object != null;

		Tuple tuple;
		tuple = super.unbind(object, "code", "conclusion", "strongPoint", "weakPoint", "mark", "lini");

		super.getResponse().setData(tuple);
	}

}
