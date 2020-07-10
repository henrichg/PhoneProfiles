package sk.henrichg.phoneprofiles;

class Shortcut {
    long _id;
    String _intent;
    String _name;

    Shortcut() {}

    Shortcut(long id,
             String intent,
             String name) {
        this._id = id;
        this._intent = intent;
        this._name = name;
    }

}
