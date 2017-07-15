package org.licket.demo.view;

import org.licket.core.module.application.LicketComponentModelReloader;
import org.licket.core.module.application.LicketRemote;
import org.licket.core.view.form.AbstractLicketForm;
import org.licket.core.view.form.LicketInput;
import org.licket.core.view.ComponentActionCallback;
import org.licket.core.view.link.AbstractLicketActionLink;
import org.licket.core.view.list.AbstractLicketList;
import org.licket.demo.model.Contact;
import org.licket.demo.model.EmailAddress;
import org.licket.demo.service.ContactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.IdGenerator;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.function.BiConsumer;

import static org.licket.core.model.LicketComponentModel.emptyComponentModel;
import static org.licket.core.model.LicketComponentModel.ofString;
import static org.licket.core.view.LicketComponentView.internalTemplateView;

/**
 * @author activey
 */
public class AddContactForm extends AbstractLicketForm<Contact> {

    @Autowired
    private ContactsService contactsService;

    @Autowired
    private LicketComponentModelReloader modelReloader;

    @Autowired
    private LicketRemote remote;

    @Autowired
    private IdGenerator idGenerator;

    private BiConsumer<Contact, ComponentActionCallback> callback;

    public AddContactForm(String id) {
        super(id, Contact.class, emptyComponentModel(), internalTemplateView());
    }

    @PostConstruct
    private void postConstruct() {
        setComponentModelObject(contactsService.emptyContact());
    }

    public final void onContactAdded(BiConsumer<Contact, ComponentActionCallback> callback) {
        this.callback = callback;
    }

    @Override
    protected void onInitializeContainer() {
        add(new LicketInput("name"));
        add(new LicketInput("description"));
        add(new AbstractLicketList("email", ofString("emails")) {
            @Override
            protected LicketComponentModelReloader getModelReloader() {
                return modelReloader;
            }

            @Override
            protected void onInitializeContainer() {
                add(new LicketInput("value"));
                add(new AbstractLicketActionLink<EmailAddress>("delete_email", EmailAddress.class, remote, modelReloader) {

                    @Override
                    protected void onClick(EmailAddress emailAddress) {
                        AddContactForm.this.getComponentModel().patch(contact -> contact.removeEmailById(emailAddress.getId()));
                    }

                    @Override
                    protected void onAfterClick(ComponentActionCallback componentActionCallback) {
                        componentActionCallback.patch(AddContactForm.this);
                    }
                });
            }

            @Override
            protected Optional<String> keyPropertyName() {
                return Optional.of("id");
            }
        });
    }

    @Override
    protected void onSubmit() {
        contactsService.addContact(getComponentModel().get());
        clearInput();
    }

    public void addEmail() {
        getComponentModel().patch(contact -> contact.addEmail(idGenerator.generateId().toString(), ""));
    }

    public void generateRandomData() {
        setComponentModelObject(contactsService.randomContact());
    }

    private void clearInput() {
        setComponentModelObject(contactsService.emptyContact());
    }

    @Override
    protected LicketComponentModelReloader getModelReloader() {
        return modelReloader;
    }

    @Override
    protected final LicketRemote getRemote() {
        return remote;
    }

    @Override
    protected final void onAfterSubmit(ComponentActionCallback componentActionCallback) {
        if (callback == null) {
            return;
        }
        callback.accept(getComponentModel().get(), componentActionCallback);
    }
}
