package org.vaadin.addon.daterangefield;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.Field;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by jnash on 5/21/2014.
 */
public class ErrorfulUtil implements Serializable {

    public static void addErrorChangeListener(final Field<?> f, final ErrorfulFieldLayout efl) {
        f.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                try {
                    f.validate();
                    f.removeStyleName(efl.getErrorStyleName());
                } catch (Validator.InvalidValueException ive) {
                    f.addStyleName(efl.getErrorStyleName());
                }
            }
        });
    }

    // this enables an initial validation before the fields have changed
    public static Validator.InvalidValueException highlightInvalidFields(Collection<Field<?>> fieldsCollection,
                                                                         final ErrorfulFieldLayout efl) {
        Validator.InvalidValueException first = null;
        for (Field<?> f : fieldsCollection) {
            try {
                f.validate();
                f.removeStyleName(efl.getErrorStyleName());
            } catch (Validator.InvalidValueException ive) {
                if (null == first) {
                    first = ive;
                }
                f.addStyleName(efl.getErrorStyleName());
            }
        }
        return first;
    }

    public static void discardInvalidBufferedValues(Collection<Field<?>> fieldsCollection) {
        for (Field<?> f : fieldsCollection) {
            try {
                f.validate();
            } catch (Validator.InvalidValueException ive) {
                f.discard();
            }
        }
    }

}
