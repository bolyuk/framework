package bl0.bjs.common.core.relations.v2.xtra;

import bl0.bjs.common.core.relations.v2.Property;

public class BooleanProperty extends Property<Boolean> {
    public BooleanProperty() {
        this.value = false;
    }
    public BooleanProperty(boolean v) {
        this.value = v;
    }
}
