package bl0.bjs.common.core.relations.v2.xtra;

import bl0.bjs.common.core.relations.v2.Property;

public class StringProperty extends Property<String> {
    public StringProperty() {
        value = null;
    }

    public StringProperty(String value) {
        this.value = value;
    }
}
