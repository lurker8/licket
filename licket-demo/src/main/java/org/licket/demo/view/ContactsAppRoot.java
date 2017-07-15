package org.licket.demo.view;

import org.licket.core.module.application.LicketComponentModelReloader;
import org.licket.core.module.application.LicketRemote;
import org.licket.core.view.ComponentActionCallback;
import org.licket.core.view.ComponentFunctionCallback;
import org.licket.core.view.container.AbstractLicketMultiContainer;
import org.licket.core.view.hippo.vue.annotation.LicketMountPoint;
import org.licket.core.view.link.AbstractLicketActionLink;
import org.licket.core.view.link.AbstractLicketLink;
import org.licket.core.view.mount.params.MountingParams;
import org.springframework.beans.factory.annotation.Autowired;

import static org.licket.core.model.LicketComponentModel.emptyComponentModel;
import static org.licket.core.view.LicketComponentView.fromComponentClass;

@LicketMountPoint("/")
public class ContactsAppRoot extends AbstractLicketMultiContainer<Void> {

    @Autowired
    private ContactsPanel contactsPanel;

    @Autowired
    private AddContactPanel addContactPanel;

    @Autowired
    private LicketComponentModelReloader modelReloader;

    @Autowired
    private LicketRemote remoteCommunication;

    public ContactsAppRoot(String id) {
        super(id, Void.class, emptyComponentModel(), fromComponentClass(ContactsAppRoot.class));
    }

    @Override
    protected void onInitializeContainer() {
        addContactPanel.onContactAdded((contact, componentActionCallback) -> {
            contactsPanel.reloadList();
            componentActionCallback.patch(contactsPanel);
        });
        add(contactsPanel);
        add(addContactPanel);

        add(new AbstractLicketActionLink<Void>("reload", Void.class, remoteCommunication, modelReloader()) {

            protected void onClick(Void modelObject) {
                contactsPanel.reloadList();
            }

            @Override
            protected void onAfterClick(ComponentActionCallback componentActionCallback) {
                componentActionCallback.reload(contactsPanel);
            }
        });
        add(new AbstractLicketLink("add-contact") {

            @Override
            protected void onClick(ComponentFunctionCallback componentActionCallback) {
                addContactPanel.showAddContactModal(componentActionCallback, this);
            }
        });
    }

    @Override
    protected final LicketComponentModelReloader getModelReloader() {
        return modelReloader;
    }

    @Override
    protected final void onComponentMounted(MountingParams componentMountingParams) {
        contactsPanel.reloadList();
    }

    @Override
    protected void onAfterComponentMounted(ComponentActionCallback componentActionCallback) {
        componentActionCallback.patch(contactsPanel);
    }
}
